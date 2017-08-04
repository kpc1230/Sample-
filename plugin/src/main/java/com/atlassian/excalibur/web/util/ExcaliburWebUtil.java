package com.atlassian.excalibur.web.util;

import com.atlassian.bonfire.renderer.BonfireWikiRenderer;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfireUserService;
import com.atlassian.bonfire.service.TimeZoneService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.mime.MimeManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.util.FileIconBean;
import com.atlassian.jira.web.util.FileIconBean.FileIcon;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Service(ExcaliburWebUtil.SERVICE)
public class ExcaliburWebUtil {
    public static final String SERVICE = "excalibur-excaliburwebutil";

    private static final String DEFAULT_ICON = "file.gif";

    @JIRAResource
    private JiraDurationUtils jiraDurationUtils;

    @JIRAResource
    private JiraAuthenticationContext jiraAuthenticationContext;

    @JIRAResource
    private ApplicationProperties jiraApplicationProperties;

    @JIRAResource
    private ProjectManager jiraProjectManager;

    @JIRAResource
    private MimeManager jiraMimeManager;

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService bonfireUserService;

    @Resource(name = BonfireWikiRenderer.SERVICE)
    private BonfireWikiRenderer bonfireWikiRenderer;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = TimeZoneService.SERVICE)
    protected TimeZoneService timeZoneService;

    @JIRAResource
    private AvatarService avatarService;

    @JIRAResource
    private VelocityRequestContextFactory velocityRequestContextFactory;

    /**
     * There are two cases, remote icon or within jira. The remote icon can be returned as is while the jira one will need the baseURL added to the
     * front
     *
     * @param i issue
     * @return full url to the image
     */
    public String getFullIconUrl(Issue i) {
        return getFullIconUrl(i.getIssueTypeObject());
    }

    public String getFullIconUrl(IssueType it) {
        String iconUrl = it.getIconUrl();
        String imgSrc = "";
        if (iconUrl.indexOf("http") >= 0) {
            // In the case of remote issue icons
            imgSrc = iconUrl.substring(iconUrl.indexOf("http"));
        } else {
            String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
            if (!StringUtils.isEmpty(iconUrl)) {
                imgSrc = baseUrl + iconUrl;
            } else {
                // To prevent empty img tags
                imgSrc = baseUrl + "/images/icons/undefined.gif";
            }
        }
        return imgSrc;
    }

    public String getProjectIconUrl(Project project) {
        String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        return baseUrl + "/secure/projectavatar?size=small&avatarId=" + project.getAvatar().getId().toString() + "&pid=" + project.getId().toString();
    }

    public Project getProjectFromId(Long id) {
        return jiraProjectManager.getProjectObj(id);
    }

    public JiraAuthenticationContext getJiraAuthenticationContext() {
        return jiraAuthenticationContext;
    }

    public String htmlEncode(final String s) {
        return TextUtils.htmlEncode(s);
    }

    public String formatTimeSpentWJira(Duration time) {
        return time == null ? jiraDurationUtils.getShortFormattedDuration(0L) : jiraDurationUtils.getShortFormattedDuration(time.getStandardSeconds());
    }

    private DateTime getDateWithUserTimezone(DateTime time) {
        DateTimeZone userTimeZone = timeZoneService.getLoggedInUserTimeZone();
        if (userTimeZone != null) {
            return time.withZone(userTimeZone);
        }
        return time;
    }

    public String formatDateWithUserTimeZone(DateTime time) {
        return formatDate(getDateWithUserTimezone(time));
    }

    public String formatTimeWithUserTimeZone(DateTime time) {
        return formatTime(getDateWithUserTimezone(time));

    }

    public String formatTime(DateTime time) {
        // Hardcoded for now.
        return time.toString("h:mm a");
    }

    public String formatDateTime(DateTime time) {
        String format = jiraApplicationProperties.getDefaultBackedString(APKeys.JIRA_DATE_TIME_PICKER_JAVA_FORMAT);
        return time.toString(format);
    }

    public String formatDate(DateTime time) {
        String format = jiraApplicationProperties.getDefaultBackedString(APKeys.JIRA_DATE_PICKER_JAVA_FORMAT);
        return time.toString(format);
    }

    public String formatShortTimeSpent(Duration time) {
        String zeroMinutes = jiraDurationUtils.getShortFormattedDuration(0L);
        return time == null ? zeroMinutes : StringUtils.defaultIfEmpty(shortFormat(time.getStandardSeconds()), zeroMinutes);
    }

    public String renderWikiContent(String content, Issue issue) {
        return bonfireWikiRenderer.renderWikiContent(content, issue);
    }

    public String renderWikiContent(String content) {
        return bonfireWikiRenderer.renderWikiContent(content);
    }

    /**
     * Returns the display name of the passed in user name
     *
     * @param userName the user to find their display name
     * @return the display name of the passed in user name
     */
    public String getDisplayName(String userName) {
        ApplicationUser user = bonfireUserService.safeGetUser(userName);
        return user == null ? "Anonymous" : user.getDisplayName();
    }

    public String getLargeAvatarUrl(String username) {
        ApplicationUser user = bonfireUserService.safeGetUser(username);
        return avatarService.getAvatarAbsoluteURL(user, user, Avatar.Size.LARGE).toString();
    }

    public String getSmallAvatarUrl(String username) {
        ApplicationUser user = bonfireUserService.safeGetUser(username);
        return avatarService.getAvatarAbsoluteURL(user, user, Avatar.Size.SMALL).toString();
    }

    public String getLargeAvatarUrl(ApplicationUser user) {
        return avatarService.getAvatarAbsoluteURL(user, user, Avatar.Size.LARGE).toString();
    }

    public String getSmallAvatarUrl(ApplicationUser user) {
        return avatarService.getAvatarAbsoluteURL(user, user, Avatar.Size.SMALL).toString();
    }

    /**
     * Detects the browser being used through sniffing the user agent header
     *
     * @param request used to detect the browser
     * @return browser of the request
     */
    public Browser detectBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("user-agent");

        if (userAgent == null) {
            return Browser.UNKNOWN;
        } else if (userAgent.contains("Chrome")) {
            return Browser.CHROME;
        } else if (userAgent.contains("Firefox")) {
            return Browser.FIREFOX;
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) {
            if (userAgent.contains("Win64") || userAgent.contains("IA64") || userAgent.contains("x64") || userAgent.contains("WOW64")) {
                return Browser.IE_WIN64;
            } else {
                return Browser.IE;
            }
        } else if (userAgent.contains("Safari")) {
            if (userAgent.contains("Windows") || userAgent.contains("Macintosh")) {
                return Browser.SAFARI;
            }
            return Browser.UNKNOWN;
        } else {
            return Browser.UNKNOWN;
        }
    }

    public enum Browser {
        CHROME, FIREFOX, SAFARI, IE, IE_WIN64, UNKNOWN
    }

    ;

    public String formatBrowserString(Browser browser) {
        switch (browser) {
            case CHROME:
                return "Chrome";
            case FIREFOX:
                return "Firefox";
            case SAFARI:
                return "Safari";
            case IE:
                return "Internet Explorer - Windows 32-bit";
            case IE_WIN64:
                return "Internet Explorer - Windows 64-bit";
            case UNKNOWN:
                return "Unknown";
            default:
                return null;
        }
    }

    public String getFileIconName(Attachment attachment) {
        FileIconBean bean = new FileIconBean(FileIconBean.DEFAULT_FILE_ICONS, jiraMimeManager);
        FileIcon icon = bean.getFileIcon(attachment.getFilename(), attachment.getMimetype());
        return icon != null ? icon.getIcon() : DEFAULT_ICON;
    }

    public String getText(String key, Object... params) {
        return i18n.getText(key, params);
    }

    /**
     * Copied from JiraDurationUtils and adapted to suit our needs
     */
    private String shortFormat(final Long duration) {
        BigDecimal hoursPerDay = BigDecimal.valueOf(24);
        BigDecimal daysPerWeek = BigDecimal.valueOf(7);

        final BigDecimal secondsPerHour = BigDecimal.valueOf(DateUtils.Duration.HOUR.getSeconds());
        final long secondsPerDay = hoursPerDay.multiply(secondsPerHour).longValueExact();
        final long secondsPerWeek = daysPerWeek.multiply(hoursPerDay).multiply(secondsPerHour).longValueExact();
        return DateUtils.getDurationStringSeconds(duration, secondsPerDay, secondsPerWeek);
    }
}
