package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.util.CaptureCustomFieldsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by niravshah on 8/28/17.
 */
@Service
public class CaptureContextIssueFieldsServiceImpl implements CaptureContextIssueFieldsService {

    @Autowired
    private IssueService issueService;

    @Autowired
    private JiraRestClient jiraRestClient;

    @Autowired
    private CaptureCustomFieldsUtils captureCustomFieldsUtils;

    /**
     * Make a best effort to populate these fields for an issue. If the value for a field cannot be found, then don't fill it in.
     *
     * @param request
     */
    public void populateContextFields(HttpServletRequest req, String issueKey, Map<String, String> context) {
        // Get the issue from the issue key
        Issue issue = issueService.getIssueObject(issueKey);
        if (issue != null) {
            populateContextFields(req, issueKey, context);
        }
    }

    private void populateContextFields(HttpServletRequest req, Issue issue, Map<String, String> context) {
        boolean sendContext = true;
        if (context != null) {
            sendContext = Boolean.valueOf(context.get("send"));
        }

        if (sendContext) {
            String userAgent = req.getHeader("user-agent");
            if (StringUtils.isNotBlank(userAgent)) {
                // Get fields
//                CustomField userAgentField = getBonfireUserAgentCustomField();
//                CustomField browserField = getBonfireBrowserCustomField();
//                CustomField osField = getBonfireOSCustomField();
//                CustomField urlField = getBonfireURLCustomField();
//                CustomField screenResField = getBonfireScreenResCustomField();
//                CustomField jQueryVersionField = getBonfirejQueryVersionCustomField();
//                CustomField documentModeField = getBonfireDocumentModeCustomField();
//

//                // Update user agent
//                userAgentField.getCustomFieldType().updateValue(userAgentField, issue, userAgent);
//
//                testingStatusCustomFieldService.updateTestingStatus(issue);
//
//                // Update browser
//                UserAgentSniffer.SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
//                if (StringUtils.isNotBlank(browser.browser)) {
//                    StringBuilder sb = new StringBuilder().append(browser.browser).append(" ").append(browser.version);
//                    browserField.getCustomFieldType().updateValue(browserField, issue, sb.toString());
//                }
//                // Update OS
//                UserAgentSniffer.SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
//                if (StringUtils.isNotBlank(os.OS)) {
//                    // If we found a prettyname show that, otherwise show the raw OS string
//                    StringBuilder sb = new StringBuilder();
//                    if (StringUtils.isNotBlank(os.prettyName)) {
//                        sb.append(os.prettyName);
//                        sb.append(" (");
//                    }
//                    sb.append(os.OS);
//                    if (StringUtils.isNotBlank(os.prettyName)) {
//                        sb.append(")");
//                    }
//                    osField.getCustomFieldType().updateValue(osField, issue, sb.toString());
//                }
//                // Update context fields
//                if (context != null) {
//                    String url = context.get("url");
//                    if (StringUtils.isNotBlank(url)) {
//                        urlField.getCustomFieldType().updateValue(urlField, issue, url);
//                    }
//                    String screenRes = context.get("screenRes");
//                    if (StringUtils.isNotBlank(screenRes)) {
//                        screenResField.getCustomFieldType().updateValue(screenResField, issue, screenRes);
//                    }
//                    String jQueryVersion = context.get("jQueryVersion");
//                    if (StringUtils.isNotBlank(jQueryVersion)) {
//                        jQueryVersionField.getCustomFieldType().updateValue(jQueryVersionField, issue, jQueryVersion);
//                    }
//                    String documentMode = context.get("documentMode");
//                    if (StringUtils.isNotBlank(documentMode)) {
//                        documentModeField.getCustomFieldType().updateValue(documentModeField, issue, documentMode);
//                    }
//                }
            }
        }
    }

    /**
     * Checks to see if this issue contains any values in any of the context fields
     */
    // This isn't one line of code because its easier to read and debug...
    public boolean hasContextValues(Issue issue) {
        return false;
        // Get the values
//        String userAgent = getUserAgentValue(issue);
//        String browser = getBrowserValue(issue);
//        String os = getOSValue(issue);
//        String url = getUrlValue(issue);
//        String screenRes = getBonfireScreenResValue(issue);
//        String jQueryVersion = getBonfirejQueryVersionValue(issue);

//        // Check that they exist
//        boolean hasUserAgent = StringUtils.isNotBlank(userAgent);
//        boolean hasBrowser = StringUtils.isNotBlank(browser);
//        boolean hasOS = StringUtils.isNotBlank(os);
//        boolean hasUrl = StringUtils.isNotBlank(url);
//        boolean hasScreenRes = StringUtils.isNotBlank(screenRes);
//        boolean hasjQueryVersion = StringUtils.isNotBlank(jQueryVersion);
//
//        return hasUserAgent || hasBrowser || hasOS || hasUrl || hasScreenRes || hasjQueryVersion;
    }

//    public String getUserAgentValue(Issue issue) {
//        CustomField userAgentField = getBonfireUserAgentCustomField();
//        return userAgentField.getValueFromIssue(issue);
//    }
//
//    public String getBrowserValue(Issue issue) {
//        CustomField browserField = getBonfireBrowserCustomField();
//        return browserField.getValueFromIssue(issue);
//    }
//
//    public String getOSValue(Issue issue) {
//        CustomField osField = getBonfireOSCustomField();
//        return osField.getValueFromIssue(issue);
//    }
//
//    public String getUrlValue(Issue issue) {
//        CustomField urlField = getBonfireURLCustomField();
//        return urlField.getValueFromIssue(issue);
//    }
//
//    public String getBonfireScreenResValue(Issue issue) {
//        CustomField screenResField = getBonfireScreenResCustomField();
//        return screenResField.getValueFromIssue(issue);
//    }
//
//    public String getBonfirejQueryVersionValue(Issue issue) {
//        CustomField jQueryVersionField = getBonfirejQueryVersionCustomField();
//        return jQueryVersionField.getValueFromIssue(issue);
//    }
//
//    public String getBonfireDocumentModeValue(Issue issue) {
//        CustomField documentModeField = getBonfireDocumentModeCustomField();
//        return documentModeField.getValueFromIssue(issue);
//    }

//    private CustomField getBonfirejQueryVersionCustomField() {
//        return getBonfireCustomField(CaptureCustomFieldsUtils.JQUERY_VERSION_METADATA, BONFIRE_JQUERY_VERSION_PROPERTY_SET_KEY);
//    }
//
//    private CustomField getBonfireUserAgentCustomField() {
//        return getBonfireCustomField(USERAGENT_METADATA, BONFIRE_USERAGENT_PROPERTY_SET_KEY);
//    }
//
//    private CustomField getBonfireDocumentModeCustomField() {
//        return getBonfireCustomField(DOCUMENT_MODE_METADATA, BONFIRE_DOCUMENT_MODE_PROPERTY_SET_KEY);
//    }
//
//    private CustomField getBonfireBrowserCustomField() {
//        return getBonfireCustomField(BROWSER_METADATA, BONFIRE_BROWSER_PROPERTY_SET_KEY);
//    }
//
//    private CustomField getBonfireOSCustomField() {
//        return getBonfireCustomField(OS_METADATA, BONFIRE_OS_PROPERTY_SET_KEY);
//    }
//
//    private CustomField getBonfireURLCustomField() {
//        return getBonfireCustomField(URL_METADATA, BONFIRE_URL_PROPERTY_SET_KEY);
//    }
//
//    private CustomField getBonfireScreenResCustomField() {
//        return getBonfireCustomField(SCREEN_RES_METADATA, BONFIRE_SCREEN_RES_PROPERTY_SET_KEY);
//    }
//
//    private CustomField getBonfireCustomField(CustomFieldMetadata meta, String propertyKey) {
//        Long fieldId = persistenceService.getLong(KEY_BON_PROPS, GLOBAL_ENTITY_ID, propertyKey);
//        CustomField field = null;
//        if (fieldId != null) {
//            field = customFieldService.getCustomField(fieldId);
//        }
//        // If no field could be retrieved from the custom field service then make one and save it for next time
//        if (field == null) {
//            field = createCustomField(meta);
//            persistenceService.setLong(KEY_BON_PROPS, GLOBAL_ENTITY_ID, propertyKey, field.getIdAsLong());
//        }
//
//        return field;
//    }
//
//    private CustomField createCustomField(CustomFieldMetadata meta) {
//        CustomField field = customFieldService.createCustomField(meta);
//        return field;
//    }
}
