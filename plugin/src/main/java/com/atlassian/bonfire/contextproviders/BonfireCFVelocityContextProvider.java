package com.atlassian.bonfire.contextproviders;

import com.atlassian.bonfire.customfield.*;
import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.bonfire.service.TestingStatusService;
import com.atlassian.bonfire.service.TestingStatusService.TestingStatus;
import com.atlassian.bonfire.service.TestingStatusService.TestingStatusBar;
import com.atlassian.bonfire.util.SessionDisplayUtils;
import com.atlassian.bonfire.util.model.SessionDisplayHelper;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Hmm lots of constants... but I suppose it's better than hardcoding... maybe
public class BonfireCFVelocityContextProvider implements ContextProvider {
    private static final String ISSUE_KEY = "issue";
    private static final String USER_KEY = "user";

    private static final String TEXT_UTILS = "textutils";
    private static final String HAS_RAISED_IN = "hasRaisedIn";
    private static final String RAISED_IN = "raisedIn";
    private static final String RAISED_IN_FIELD = "raisedInField";
    private static final String HAS_RELATED_TO = "hasRelatedTo";
    private static final String RELATED_TO = "relatedTo";
    private static final String RELATED_TO_FIELD = "relatedInField";
    private static final String TESTING_STATUS = "testingStatus";
    private static final String TESTING_STATUS_FIELD = "testingStatusField";
    private static final String TESTING_STATUS_CLASS = "testingStatusClass";
    private static final String TESTING_BAR = "testingBar";
    private static final String BASE_URL = "baseurl";
    private static final String XSRF_TOKEN = "xsrfToken";
    private static final String REMOVE_RAISED_IN_PERMISSION = "canUnraiseInSession";

    private static final String HAS_CONTEXT = "hasContext";
    private static final String USER_AGENT = "bfUserAgent";
    private static final String BROWSER = "bfBrowser";
    private static final String OS = "bfOS";
    // This variable needs a Html suffix to prevent auto-escaping in velocity
    private static final String URL = "bfURLHtml";
    private static final String HAS_USER_AGENT = "hasbfUserAgent";
    private static final String HAS_BROWSER = "hasbfBrowser";
    private static final String HAS_OS = "hasbfOS";
    private static final String HAS_URL = "hasbfURL";
    private static final String SCREEN_RES = "bfScreenRes";
    private static final String HAS_SCREEN_RES = "hasbfScreenRes";
    private static final String JQUERY_VERSION = "bfjQueryVersion";
    private static final String HAS_JQUERY_VERSION = "hasbfjQueryVersion";
    private static final String DOCUMENT_MODE = "bfDocumentMode";
    private static final String HAS_DOCUMENT_MODE = "hasbfDocumentMode";

    private static final String BROWSER_ICON = "browserIcon";

    private static final String OS_ICON = "osIcon";
    private static final String BROWSER_CHROME = "chrome";
    private static final String BROWSER_FIREFOX = "firefox";
    private static final String BROWSER_MSIE = "msie";
    private static final String BROWSER_MSIE_ALT = "trident/"; // IE11 browser has name Mozilla and can be identified by the keyword

    private static final String BROWSER_SAFARI = "safari";
    private static final String OS_LINUX = "linux";
    private static final String OS_WINDOWS = "windows";

    private static final String OS_MAC = " mac ";
    private static final String BLUE = "blue";
    private static final String YELLOW = "yellow";

    private static final String GREEN = "green";

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = TestingStatusCustomFieldService.SERVICE)
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = BonfireContextCustomFieldsService.SERVICE)
    private BonfireContextCustomFieldsService bonfireContextCustomFieldsService;

    @Resource(name = TestingStatusService.SERVICE)
    private TestingStatusService testingStatusService;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = SessionDisplayUtils.SERVICE)
    private SessionDisplayUtils displayUtils;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;
    @JIRAResource
    private VelocityRequestContextFactory velocityRequestContextFactory;

    @Override
    public void init(Map<String, String> params) throws PluginParseException {
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context) {
        Issue issue = (Issue) context.get(ISSUE_KEY);
        final Object user = context.get(USER_KEY);
        ApplicationUser appUser = user instanceof ApplicationUser ? (ApplicationUser) user : ApplicationUsers.from((User) user);
        Map<String, Object> params = new HashMap<String, Object>(context);
        CustomField raisedInField = bonfireSessionCustomFieldService.getRaisedInSessionCustomField();
        CustomField relatedToField = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        CustomField testingStatusField = testingStatusCustomFieldService.getTestingStatusSessionCustomField();
        SessionCFTTransportObject raisedIn = getRaisedInSession(appUser, raisedInField.getValueFromIssue(issue));
        List<SessionCFTTransportObject> relatedTo = getRelatedToSessions(appUser, relatedToField.getValueFromIssue(issue));
        TestingStatus testingStatus = testingStatusService.getTestingStatus(issue);
        TestingStatusBar testingBar = testingStatusService.getTestingStatusBar(issue);
        String userAgent = bonfireContextCustomFieldsService.getUserAgentValue(issue);
        String browser = bonfireContextCustomFieldsService.getBrowserValue(issue);
        String os = bonfireContextCustomFieldsService.getOSValue(issue);
        String url = bonfireContextCustomFieldsService.getUrlValue(issue);
        String screenRes = bonfireContextCustomFieldsService.getBonfireScreenResValue(issue);
        String jQueryVersion = bonfireContextCustomFieldsService.getBonfirejQueryVersionValue(issue);
        String documentMode = bonfireContextCustomFieldsService.getBonfireDocumentModeValue(issue);

        params.put(XSRF_TOKEN, new JiraWebActionSupport().getXsrfToken());
        params.put(RAISED_IN_FIELD, raisedInField.getName());
        params.put(HAS_RAISED_IN, raisedIn != null);
        params.put(RAISED_IN, raisedIn);
        params.put(RELATED_TO_FIELD, relatedToField.getName());
        params.put(HAS_RELATED_TO, !relatedTo.isEmpty());
        params.put(RELATED_TO, relatedTo);
        params.put(BASE_URL, velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl());
        params.put(USER_AGENT, userAgent);
        params.put(HAS_USER_AGENT, StringUtils.isNotBlank(userAgent));
        params.put(BROWSER, browser);
        params.put(HAS_BROWSER, StringUtils.isNotBlank(browser));
        params.put(OS, os);
        params.put(HAS_OS, StringUtils.isNotBlank(os));
        params.put(URL, excaliburWebUtil.renderWikiContent(url));
        params.put(HAS_URL, StringUtils.isNotBlank(url));
        params.put(SCREEN_RES, screenRes);
        params.put(HAS_SCREEN_RES, StringUtils.isNotBlank(screenRes));
        params.put(JQUERY_VERSION, jQueryVersion);
        params.put(HAS_JQUERY_VERSION, StringUtils.isNotBlank(jQueryVersion));
        params.put(DOCUMENT_MODE, documentMode);
        params.put(HAS_DOCUMENT_MODE, StringUtils.isNotBlank(documentMode));
        params.put(HAS_CONTEXT, bonfireContextCustomFieldsService.hasContextValues(issue));
        params.put(BROWSER_ICON, getBrowserIcon(browser));
        params.put(OS_ICON, getOSIcon(os));
        params.put(TESTING_STATUS, i18n.getText(testingStatus.getI18nKey()));
        params.put(TESTING_STATUS_FIELD, testingStatusField.getName());
        params.put(TESTING_STATUS_CLASS, getTestingStatusCssClass(testingStatus));
        params.put(TESTING_BAR, testingBar);
        params.put(ISSUE_KEY, issue);
        params.put(REMOVE_RAISED_IN_PERMISSION, bonfirePermissionService.canUnraiseIssueInSession(appUser, issue));

        // For escaping
        params.put(TEXT_UTILS, new TextUtils());

        return params;
    }

    private List<SessionCFTTransportObject> getRelatedToSessions(ApplicationUser user, String rawSessionIds) {
        List<SessionCFTTransportObject> toReturn = Lists.newArrayList();
        if (!StringUtils.isEmpty(rawSessionIds)) {
            String[] split = rawSessionIds.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
            for (String s : split) {
                LightSession session = sessionController.getLightSession(s);
                if (session != null) {
                    ApplicationUser assignee = session.getAssignee();
                    SessionDisplayHelper flags = displayUtils.getDisplayHelper(user, session);
                    toReturn.add(new SessionCFTTransportObject(session.getId().toString(), BonfireConstants.SESSION_PAGE + s, session.getName(),
                            getUserAvatarUrl(assignee), assignee.getName(), getPrettyStatus(session.getStatus()), flags));
                }
            }
        }
        return toReturn;
    }

    private SessionCFTTransportObject getRaisedInSession(ApplicationUser user, String sessionId) {
        if (!StringUtils.isEmpty(sessionId)) {
            LightSession session = sessionController.getLightSession(sessionId);
            if (session != null) {
                if (bonfirePermissionService.canSeeSession(user, session)) {
                    ApplicationUser assignee = session.getAssignee();
                    return new SessionCFTTransportObject(session.getId().toString(), BonfireConstants.SESSION_PAGE + sessionId, session.getName(),
                            getUserAvatarUrl(assignee), assignee.getName(), getPrettyStatus(session.getStatus()));
                }
            }
        }
        return null;
    }

    private String getUserAvatarUrl(ApplicationUser user) {
        return excaliburWebUtil.getSmallAvatarUrl(user);
    }

    private String getPrettyStatus(Status status) {
        return i18n.getText("session.status.pretty." + status);
    }

    private String getOSIcon(String os) {
        if (StringUtils.isNotBlank(os)) {
            if (os.toLowerCase().contains(OS_LINUX)) {
                return OS_LINUX;
            } else if (os.toLowerCase().contains(OS_WINDOWS)) {
                return OS_WINDOWS;
            } else if (os.toLowerCase().contains(OS_MAC)) {
                return OS_MAC.trim();// yeah osx
            }
        }
        return "none";
    }

    private String getBrowserIcon(String browser) {
        if (StringUtils.isNotBlank(browser)) {
            if (browser.toLowerCase().contains(BROWSER_FIREFOX)) {
                return BROWSER_FIREFOX;
            } else if (browser.toLowerCase().contains(BROWSER_MSIE) || browser.toLowerCase().contains(BROWSER_MSIE_ALT)) {
                return BROWSER_MSIE;
            } else if (browser.toLowerCase().contains(BROWSER_CHROME)) {
                return BROWSER_CHROME;
            } else if (browser.toLowerCase().contains(BROWSER_SAFARI)) {
                return BROWSER_SAFARI;
            }
        }
        return "none";
    }

    private String getTestingStatusCssClass(TestingStatus status) {
        switch (status) {
            case NOT_STARTED:
                return BLUE;
            case IN_PROGRESS:
                return YELLOW;
            case COMPLETED:
                return GREEN;
            default:
                return "";
        }
    }
}
