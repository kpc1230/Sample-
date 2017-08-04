package com.atlassian.bonfire.customfield;

import com.atlassian.bonfire.service.TestingStatusService;
import com.atlassian.bonfire.web.util.UserAgentSniffer;
import com.atlassian.bonfire.web.util.UserAgentSniffer.SniffedBrowser;
import com.atlassian.bonfire.web.util.UserAgentSniffer.SniffedOS;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.customfield.CustomFieldMetadata;
import com.atlassian.borrowed.greenhopper.customfield.CustomFieldService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.ExecutingHttpRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service(BonfireContextCustomFieldsService.SERVICE)
public class BonfireContextCustomFieldsService {
    public static final String SERVICE = "bonfire-environmentCustomFieldService";

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource(name = CustomFieldService.SERVICE)
    private CustomFieldService customFieldService;

    @Resource(name = TestingStatusCustomFieldService.SERVICE)
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @JIRAResource
    private IssueManager jiraIssueManager;

    private static final String CAPTURE_CUSTOM_FIELD_DESCRIPTION = "This custom field is created programatically by Capture for JIRA.";

    /**
     * Move to properties file when internationalisation is implemented *
     */
    // All Environment related custom fields should be prefixed with "Bonfire"
    private static final String CAPTURE_USERAGENT_NAME = "Capture for JIRA user agent";
    private static final String CAPTURE_BROWSER_NAME = "Capture for JIRA browser";
    private static final String CAPTURE_OS_NAME = "Capture for JIRA operating system";
    private static final String CAPTURE_URL_NAME = "Capture for JIRA URL";
    private static final String CAPTURE_SCREEN_RES_NAME = "Capture for JIRA screen resolution";
    private static final String CAPTURE_JQUERY_VERSION_NAME = "Capture for JIRA jQuery version";
    private static final String CAPTURE_DOCUMENT_MODE = "Capture for JIRA document mode";

    // IMPORTANT - if we change the plugin key, this will need to change too.
    private static final String BONFIRE_TEXT_FIELD_KEY = "com.atlassian.bonfire.plugin:bonfire-text";
    private static final String TEXT_SEARCHER_KEY = "com.atlassian.jira.plugin.system.customfieldtypes:textsearcher";

    // Keys for storing custom fields. These values need to be maintained for legacy reasons
    private static final String KEY_BON_PROPS = "Bonfire.properties";
    private static final long GLOBAL_ENTITY_ID = 1l;
    private static final String BONFIRE_USERAGENT_PROPERTY_SET_KEY = "bonfire.useragent.custom.field";
    private static final String BONFIRE_BROWSER_PROPERTY_SET_KEY = "bonfire.browser.custom.field";
    private static final String BONFIRE_OS_PROPERTY_SET_KEY = "bonfire.os.custom.field";
    private static final String BONFIRE_URL_PROPERTY_SET_KEY = "bonfire.url.custom.field";
    private static final String BONFIRE_SCREEN_RES_PROPERTY_SET_KEY = "bonfire.screenres.custom.field";
    private static final String BONFIRE_JQUERY_VERSION_PROPERTY_SET_KEY = "bonfire.jqueryversion.custom.field";
    private static final String BONFIRE_DOCUMENT_MODE_PROPERTY_SET_KEY = "bonfire.documentmode.custom.field";

    /**
     * the metadata needed to define the custom field in JIRA
     */
    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata USERAGENT_METADATA = new CustomFieldMetadata(CAPTURE_USERAGENT_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
            BONFIRE_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata BROWSER_METADATA = new CustomFieldMetadata(CAPTURE_BROWSER_NAME,
            CAPTURE_CUSTOM_FIELD_DESCRIPTION, BONFIRE_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata OS_METADATA = new CustomFieldMetadata(CAPTURE_OS_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
            BONFIRE_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata URL_METADATA = new CustomFieldMetadata(CAPTURE_URL_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
            BONFIRE_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata SCREEN_RES_METADATA = new CustomFieldMetadata(CAPTURE_SCREEN_RES_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
            BONFIRE_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata JQUERY_VERSION_METADATA = new CustomFieldMetadata(CAPTURE_JQUERY_VERSION_NAME,
            CAPTURE_CUSTOM_FIELD_DESCRIPTION, BONFIRE_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata DOCUMENT_MODE_METADATA = new CustomFieldMetadata(CAPTURE_DOCUMENT_MODE, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
            BONFIRE_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

    /**
     * Make a best effort to populate these fields for an issue. If the value for a field cannot be found, then don't fill it in.
     *
     * @param key - Issue key
     */
    public void populateContextFields(String key, Map<String, String> context) {
        // Get the issue from the issue key
        Issue issue = jiraIssueManager.getIssueObject(key);
        if (issue != null) {
            populateContextFields(issue, context);
        }
    }

    private void populateContextFields(Issue issue, Map<String, String> context) {
        boolean sendContext = true;
        if (context != null) {
            sendContext = Boolean.valueOf(context.get("send"));
        }

        if (sendContext) {
            HttpServletRequest req = ExecutingHttpRequest.get();
            String userAgent = req.getHeader("user-agent");
            if (StringUtils.isNotBlank(userAgent)) {
                // Get fields
                CustomField userAgentField = getBonfireUserAgentCustomField();
                CustomField browserField = getBonfireBrowserCustomField();
                CustomField osField = getBonfireOSCustomField();
                CustomField urlField = getBonfireURLCustomField();
                CustomField screenResField = getBonfireScreenResCustomField();
                CustomField jQueryVersionField = getBonfirejQueryVersionCustomField();
                CustomField documentModeField = getBonfireDocumentModeCustomField();

                // Update user agent
                userAgentField.getCustomFieldType().updateValue(userAgentField, issue, userAgent);

                testingStatusCustomFieldService.updateTestingStatus(issue);

                // Update browser
                SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
                if (StringUtils.isNotBlank(browser.browser)) {
                    StringBuilder sb = new StringBuilder().append(browser.browser).append(" ").append(browser.version);
                    browserField.getCustomFieldType().updateValue(browserField, issue, sb.toString());
                }
                // Update OS
                SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
                if (StringUtils.isNotBlank(os.OS)) {
                    // If we found a prettyname show that, otherwise show the raw OS string
                    StringBuilder sb = new StringBuilder();
                    if (StringUtils.isNotBlank(os.prettyName)) {
                        sb.append(os.prettyName);
                        sb.append(" (");
                    }
                    sb.append(os.OS);
                    if (StringUtils.isNotBlank(os.prettyName)) {
                        sb.append(")");
                    }
                    osField.getCustomFieldType().updateValue(osField, issue, sb.toString());
                }
                // Update context fields
                if (context != null) {
                    String url = context.get("url");
                    if (StringUtils.isNotBlank(url)) {
                        urlField.getCustomFieldType().updateValue(urlField, issue, url);
                    }
                    String screenRes = context.get("screenRes");
                    if (StringUtils.isNotBlank(screenRes)) {
                        screenResField.getCustomFieldType().updateValue(screenResField, issue, screenRes);
                    }
                    String jQueryVersion = context.get("jQueryVersion");
                    if (StringUtils.isNotBlank(jQueryVersion)) {
                        jQueryVersionField.getCustomFieldType().updateValue(jQueryVersionField, issue, jQueryVersion);
                    }
                    String documentMode = context.get("documentMode");
                    if (StringUtils.isNotBlank(documentMode)) {
                        documentModeField.getCustomFieldType().updateValue(documentModeField, issue, documentMode);
                    }
                }
            }
        }
    }

    /**
     * Checks to see if this issue contains any values in any of the context fields
     */
    // This isn't one line of code because its easier to read and debug...
    public boolean hasContextValues(Issue issue) {
        // Get the values
        String userAgent = getUserAgentValue(issue);
        String browser = getBrowserValue(issue);
        String os = getOSValue(issue);
        String url = getUrlValue(issue);
        String screenRes = getBonfireScreenResValue(issue);
        String jQueryVersion = getBonfirejQueryVersionValue(issue);

        // Check that they exist
        boolean hasUserAgent = StringUtils.isNotBlank(userAgent);
        boolean hasBrowser = StringUtils.isNotBlank(browser);
        boolean hasOS = StringUtils.isNotBlank(os);
        boolean hasUrl = StringUtils.isNotBlank(url);
        boolean hasScreenRes = StringUtils.isNotBlank(screenRes);
        boolean hasjQueryVersion = StringUtils.isNotBlank(jQueryVersion);

        return hasUserAgent || hasBrowser || hasOS || hasUrl || hasScreenRes || hasjQueryVersion;
    }

    public String getUserAgentValue(Issue issue) {
        CustomField userAgentField = getBonfireUserAgentCustomField();
        return userAgentField.getValueFromIssue(issue);
    }

    public String getBrowserValue(Issue issue) {
        CustomField browserField = getBonfireBrowserCustomField();
        return browserField.getValueFromIssue(issue);
    }

    public String getOSValue(Issue issue) {
        CustomField osField = getBonfireOSCustomField();
        return osField.getValueFromIssue(issue);
    }

    public String getUrlValue(Issue issue) {
        CustomField urlField = getBonfireURLCustomField();
        return urlField.getValueFromIssue(issue);
    }

    public String getBonfireScreenResValue(Issue issue) {
        CustomField screenResField = getBonfireScreenResCustomField();
        return screenResField.getValueFromIssue(issue);
    }

    public String getBonfirejQueryVersionValue(Issue issue) {
        CustomField jQueryVersionField = getBonfirejQueryVersionCustomField();
        return jQueryVersionField.getValueFromIssue(issue);
    }

    public String getBonfireDocumentModeValue(Issue issue) {
        CustomField documentModeField = getBonfireDocumentModeCustomField();
        return documentModeField.getValueFromIssue(issue);
    }

    private CustomField getBonfirejQueryVersionCustomField() {
        return getBonfireCustomField(JQUERY_VERSION_METADATA, BONFIRE_JQUERY_VERSION_PROPERTY_SET_KEY);
    }

    private CustomField getBonfireUserAgentCustomField() {
        return getBonfireCustomField(USERAGENT_METADATA, BONFIRE_USERAGENT_PROPERTY_SET_KEY);
    }

    private CustomField getBonfireDocumentModeCustomField() {
        return getBonfireCustomField(DOCUMENT_MODE_METADATA, BONFIRE_DOCUMENT_MODE_PROPERTY_SET_KEY);
    }

    private CustomField getBonfireBrowserCustomField() {
        return getBonfireCustomField(BROWSER_METADATA, BONFIRE_BROWSER_PROPERTY_SET_KEY);
    }

    private CustomField getBonfireOSCustomField() {
        return getBonfireCustomField(OS_METADATA, BONFIRE_OS_PROPERTY_SET_KEY);
    }

    private CustomField getBonfireURLCustomField() {
        return getBonfireCustomField(URL_METADATA, BONFIRE_URL_PROPERTY_SET_KEY);
    }

    private CustomField getBonfireScreenResCustomField() {
        return getBonfireCustomField(SCREEN_RES_METADATA, BONFIRE_SCREEN_RES_PROPERTY_SET_KEY);
    }

    private CustomField getBonfireCustomField(CustomFieldMetadata meta, String propertyKey) {
        Long fieldId = persistenceService.getLong(KEY_BON_PROPS, GLOBAL_ENTITY_ID, propertyKey);
        CustomField field = null;
        if (fieldId != null) {
            field = customFieldService.getCustomField(fieldId);
        }
        // If no field could be retrieved from the custom field service then make one and save it for next time
        if (field == null) {
            field = createCustomField(meta);
            persistenceService.setLong(KEY_BON_PROPS, GLOBAL_ENTITY_ID, propertyKey, field.getIdAsLong());
        }

        return field;
    }

    private CustomField createCustomField(CustomFieldMetadata meta) {
        CustomField field = customFieldService.createCustomField(meta);
        return field;
    }
}
