package com.atlassian.excalibur.web;

import com.atlassian.bonfire.service.*;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import webwork.action.ActionContext;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@SuppressWarnings("serial")
public class ExcaliburWebActionSupport extends JiraWebActionSupport {

    private static final String X_ATLASSIAN_DIALOG_MSG_HTML_PRE = "X-Atlassian-Dialog-Msg-Html-Pre";
    private static final String X_ATLASSIAN_DIALOG_MSG_HTML_POST = "X-Atlassian-Dialog-Msg-Html-Post";
    private static final String X_ATLASSIAN_DIALOG_MSG_CLOSEABLE = "X-Atlassian-Dialog-Msg-Closeable";
    private static final String X_ATLASSIAN_DIALOG_MSG_TYPE = "X-Atlassian-Dialog-Msg-Type";
    private static final String X_ATLASSIAN_DIALOG_MSG_TARGET = "X-Atlassian-Dialog-Msg-Target";

    private static final String LOGIN_URL = "/login.jsp?permissionViolation=true";
    private static final String OS_DESTINATION_PARAM = "&os_destination=";
    private static final String DECORATOR_PARAM = "&decorator=";
    private static final String RENEW_URL = "/secure/BonfireLicenseError.jspa";

    @Resource
    protected ExcaliburWebUtil excaliburWebUtil;

    @Resource(name = TimeZoneService.SERVICE)
    protected TimeZoneService timeZoneService;

    @Resource(name = BuildPropertiesService.SERVICE)
    protected BuildPropertiesService buildPropertiesService;

    @Resource(name = BonfireLicenseService.SERVICE)
    protected BonfireLicenseService licenseService;

    @Resource(name = CaptureAdminSettingsService.SERVICE)
    private CaptureAdminSettingsService captureAdminSettingsService;

    @Resource
    private WebResourceManager webResourceManager;

    @JIRAResource
    private AvatarService jiraAvatarService;

    @JIRAResource
    private BuildUtilsInfo jiraBuildUtilsInfo;

    private ErrorCollection errorCollection = new ErrorCollection();

    protected HttpServletResponse response = ExecutingHttpRequest.getResponse();

    public ExcaliburWebUtil getUtil() {
        return excaliburWebUtil;
    }

    /**
     * @return the coarse browser in play
     */
    public String getBrowser() {
        return excaliburWebUtil.formatBrowserString(excaliburWebUtil.detectBrowser(request));
    }

    /**
     * This plugs a gap in JIRA whereby the top of the webwork stack is not exposed during action chaining.  In a Velocity sense
     * this equals the last action invoked before the velocity template was executed
     *
     * @return the object in the top of the stack
     */
    public Object getTopOfStack() {
        return ActionContext.getValueStack().findValue(".");
    }

    /**
     * By default JIRA will redirect to the return URL regardless of whether you give it a URL or not
     * so we set the return URL to be the desired URL and then make the call.
     *
     * @param url the URL to redirect to
     * @return not sure but a redirect happens
     */
    protected String redirectTo(String url) {
        setReturnUrl(url);
        return getRedirect(url);
    }

    /**
     * All Bonfire actions have a GreenHopper style {@link ErrorCollection}
     *
     * @return the error collection
     */
    public ErrorCollection getErrorCollection() {
        return errorCollection;
    }

    /**
     * @return true if there are errors in the error collection
     */
    public boolean hasErrors() {
        return getErrorCollection().hasErrors();
    }


    /**
     * Allows you to generate a error block handler for the errors on given field
     *
     * @param fieldName the name of the field in play
     * @return AUI HTML if there is errors or empty string if not
     */
    public String auiErrorsForFieldHTML(String fieldName) {
        return auiErrorsForFieldHTML(getErrorCollection(), fieldName);
    }

    /**
     * Allows you to generate a error block handler for the errors on given field
     *
     * @param errorCollection the {}@link ErrorCollection} to use
     * @param fieldName       the name of the field in play
     * @return AUI HTML if there is errors or empty string if not
     */
    public String auiErrorsForFieldHTML(ErrorCollection errorCollection, String fieldName) {
        if (errorCollection.getFieldErrors(fieldName).isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"aui-message warning shadowed\">\n");
        for (ErrorCollection.ErrorItem errorItem : errorCollection.getFieldErrors(fieldName)) {
            sb.append("<span>").append(errorItem.getMessageKeyEscaped()).append("</span><br/>\n");
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    /**
     * Allows you to generate a error block handler for the errors in the base collection
     *
     * @return AUI HTML if there is errors or empty string if not
     */
    public String auiErrorsHTML() {
        return auiErrorsHTML(getErrorCollection());
    }

    /**
     * Allows you to generate a error block handler for the errors in a named collection
     *
     * @param errorCollection the {}@link ErrorCollection} to use
     * @return AUI HTML if there is errors or empty string if not
     */
    public String auiErrorsHTML(ErrorCollection errorCollection) {
        if (!errorCollection.hasErrors()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"aui-message warning shadowed\">\n");
        sb.append("<span class=\"aui-icon icon-warning\"></span>\n");
        for (ErrorCollection.ErrorItem errorItem : errorCollection.getErrors()) {
            //
            // only show errors that are not attributed to a specific field in this case or the well known
            // 'data' field
            //
            if ("data".equals(errorItem.getField()) || StringUtils.isBlank(errorItem.getField())) {
                sb.append("<span>").append(errorItem.getMessageKeyEscaped()).append("</span><br/>\n");
            }
        }
        sb.append("</div>\n");
        return sb.toString();
    }

    public String formatWithUserTimeZone(DateTime time) {
        if (time != null) {
            DateTimeZone userTimeZone = timeZoneService.getLoggedInUserTimeZone();
            if (userTimeZone != null) {
                return time.withZone(userTimeZone).toString("dd/MM/yy hh:mma");
            }
            return time.toString("dd/MM/yy hh:mma");
        }
        return "";
    }

    public String getCurrentBrowser() {
        ExcaliburWebUtil.Browser browser = excaliburWebUtil.detectBrowser(request);
        return excaliburWebUtil.formatBrowserString(browser);
    }

    public boolean isFeedbackEnabled() {
        return captureAdminSettingsService.isFeedbackEnabled();
    }

    public boolean isAnonymousUserInPlay() {
        return getLoggedInApplicationUser() == null;
    }


    public String getBuildVersion() {
        return buildPropertiesService.getVersion();
    }

    public String getBuildNumber() {
        return buildPropertiesService.getBuildNumber();
    }

    public String getBuildDate() {
        return buildPropertiesService.getBuildDate().toString();
    }

    public String getLicenseStatus() {
        return licenseService.getLicenseStatus().toString();
    }

    /**
     * Get error redirect.
     */
    public String getErrorRedirect(boolean inline, boolean setRedirectUrl) {
        return getErrorRedirect(inline, setRedirectUrl, null);
    }

    // Call this redirect method if you want to return to the destination after redirect (Only GET should use this)
    public String getErrorRedirect(boolean inline, boolean setRedirectUrl, String destinationUrl) {
        return getErrorRedirect(inline, setRedirectUrl, destinationUrl, null);
    }

    public String getErrorRedirect(boolean inline, boolean setRedirectUrl, String destinationUrl, String decorator) {
        ApplicationUser user = getLoggedInApplicationUser();
        if (user == null) {
            if (!StringUtils.isEmpty(destinationUrl)) {
                String decoratorString = StringUtils.isNotBlank(decorator) ? DECORATOR_PARAM + decorator : "";
                return getErrorRedirectHelper(getLoginRedirectUrl(destinationUrl) + decoratorString, inline, setRedirectUrl);
            }
            return getErrorRedirectHelper(LOGIN_URL, inline, setRedirectUrl);
        }
        if (!licenseService.isBonfireActivated()) {
            return getErrorRedirectHelper(RENEW_URL, inline, setRedirectUrl);
        }
        return null;
    }

    protected String getLoginRedirectUrl(String destinationUrl) {
        String encodedUrl = encodeURI(destinationUrl);
        return LOGIN_URL + OS_DESTINATION_PARAM + encodedUrl;
    }

    private String getErrorRedirectHelper(String url, boolean inline, boolean setRedirectUrl) {
        if (setRedirectUrl) {
            setReturnUrl(url);
        }
        if (inline) {
            return returnCompleteWithInlineRedirect(url);
        } else {
            return getRedirect(url);
        }
    }

    /**
     * Returns the default avatar in JIRA
     */
    public String getDefaultAvatar() {
        final ApplicationUser user = getLoggedInUser();
        return jiraAvatarService.getAvatarURL(user, (String) null, Avatar.Size.SMALL).toString();
    }

    /**
     * <p>
     * This will populate the the custom Atlassian header with the details of a pop-up message.
     * </p>
     * <p>
     * This has been take from JIRA 5.0 and replicated into Bonfire while live in 5.0 no mans land.
     * </p>
     */
    public void addClientMessageToResponse(String pre, String post) {
        addClientMessageToResponse(pre, post, "success", false, null);
    }

    /**
     * <p>
     * This will populate the the custom Atlassian header with the details of a pop-up message.
     * </p>
     * <p>
     * This has been take from JIRA 5.0 and replicated into Bonfire while live in 5.0 no mans land.
     * </p>
     *
     * @param type      type of message, see JIRA.Messages.Types
     * @param closeable if true, message pop-up has an 'X' button, otherwise pop-up fades away automatically
     * @param target    the target to prepend the message pop-up to. If null, the message is shown in a global spot.
     */
    public void addClientMessageToResponse(String preMsg, String postMsg, String type, boolean closeable, String target) {
        HttpServletResponse response = ExecutingHttpRequest.getResponse();
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_HTML_PRE, preMsg);
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_HTML_POST, postMsg);
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_TYPE, type);
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_CLOSEABLE, String.valueOf(closeable));
        response.setHeader(X_ATLASSIAN_DIALOG_MSG_TARGET, target);
    }

    /**
     * Helper method for display purposes
     */
    public String getPrettyStatus(String status) {
        return getI18nHelper().getText("session.status.pretty." + status);
    }

    protected String encodeURI(String s) {
        try {
            String encoded = URLEncoder.encode(s, "UTF-8");
            return encoded;
        } catch (UnsupportedEncodingException e) {
            // Gulp, Yummy Exception
            return "";
        }
    }
}
