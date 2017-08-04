package com.atlassian.excalibur.topnav;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.service.BonfireBuildCheckService;
import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.bonfire.service.BuildPropertiesService;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.jira.plugin.navigation.DefaultPluggableTopNavigation;
import com.atlassian.jira.plugin.navigation.TopNavigationModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Bonfire headsup display.
 *
 * @since v4.4
 */
public class BonfireHeadsupDisplay extends DefaultPluggableTopNavigation {
    private static final Logger log = Logger.getLogger(BonfireHeadsupDisplay.class);

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    private JiraAuthenticationContext authContext;

    private Object topNavigationModuleDescriptor;

    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    @Resource(name = BuildPropertiesService.SERVICE)
    private BuildPropertiesService buildPropertiesService;

    @Resource(name = BonfireBuildCheckService.SERVICE)
    private BonfireBuildCheckService buildCheckService;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    public BonfireHeadsupDisplay(JiraAuthenticationContext authenticationContext) {
        super();
        this.authContext = authenticationContext;
    }

    @Override
    public void init(TopNavigationModuleDescriptor descriptor) {
        this.topNavigationModuleDescriptor = descriptor;
    }

    @Override
    public String getHtml(HttpServletRequest request) {
        try {
            if (shouldNotShow(request)) {
                return dontShow(request);
            }
            return implementation(request);
        } catch (Throwable unexpected) {
            log.error("Capture for JIRA top navigation threw an unexpected exception", unexpected);
            return "<div class='errMsg'>Capture for JIRA threw an unexpected exception : " + unexpected + "</div>";
        }
    }

    private String dontShow(HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("showTopNav", false);
        return getHtmlReflectively(request, params);
    }

    private String implementation(HttpServletRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        BonfireLicenseService.Status bonfireLicenseStatus = bonfireLicenseService.getLicenseStatus();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("showTopNav", true);
        params.put("bonfireBuildVersion", buildPropertiesService.getVersion());
        params.put("bonfireBuildNumber", buildPropertiesService.getBuildNumber());
        params.put("bonfireBuildDate", buildPropertiesService.getBuildDate().toString());
        params.put("bonfireLicenseStatus", bonfireLicenseStatus.toString());

        params.put("showGetBonfire", false);

        // We only want to show the bonfire tab if the user is logged in
        ApplicationUser loggedInUser = authContext.getUser();
        if (loggedInUser == null) {
            // If there is no user, then its essentially hidden
            return getHtmlReflectively(request, params);
        }

        ErrorCollection upgradeTasksErrors = buildCheckService.checkTheStateOfTheNation();
        if (upgradeTasksErrors.hasErrors()) {
            params.put("upgradeTasksErrors", upgradeTasksErrors);
        }

        if (!bonfireLicenseStatus.equals(BonfireLicenseService.Status.activated)) {
            // If Bonfire isn't activated, don't display much top nav.
            return getHtmlReflectively(request, params);
        }

        final String browserString = getCurrentBrowser(request);
        params.put("currentBrowser", browserString);

        Long activeSessionId = sessionController.getActiveSessionId(loggedInUser);
        LightSession session = sessionController.getLightSession(activeSessionId);
        if (session == null) {
            // If the user doesn't have an active session, then its essentially hidden
            return getHtmlReflectively(request, params);
        }

        params.put("session", session);

        return getHtmlReflectively(request, params);
    }

    private String getHtmlReflectively(HttpServletRequest request, Map<String, Object> params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        //
        // Hmm lets go and meditate by the 'reflection' pool!.....Ommmmmm.......Ommmmmmmm
        //
        return ReflectionKit.method(topNavigationModuleDescriptor, "getTopNavigationHtml", HttpServletRequest.class, Map.class).call(request, params);
    }

    public String getCurrentBrowser(HttpServletRequest request) {
        ExcaliburWebUtil.Browser browser = excaliburWebUtil.detectBrowser(request);
        return excaliburWebUtil.formatBrowserString(browser);
    }

    private boolean shouldNotShow(HttpServletRequest request) {
        String url = request.getRequestURL().toString().toLowerCase();
        boolean isLoginPage = url.contains("login.jsp");

        return isLoginPage;
    }
}
