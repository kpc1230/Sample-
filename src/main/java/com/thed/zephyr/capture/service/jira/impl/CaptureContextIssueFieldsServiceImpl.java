package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.util.CaptureCustomFieldsUtils;
import com.thed.zephyr.capture.util.JiraConstants;
import com.thed.zephyr.capture.util.UserAgentSniffer;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;

/**
 * Created by niravshah on 8/28/17.
 */
@Service
public class CaptureContextIssueFieldsServiceImpl implements CaptureContextIssueFieldsService {


    @Autowired
    private JwtSigningRestTemplate restTemplate;


    /**
     * Make a best effort to populate these fields for an issue. If the value for a field cannot be found, then don't fill it in.
     *
     * @param issueKey
     * @param req
     * @param issueInputBuilder
     */
    public void populateContextFields(HttpServletRequest req, Issue issue, Map<String, String> context) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String baseUri = host.getHost().getBaseUrl();

        boolean sendContext = true;
        if (context != null) {
            sendContext = Boolean.valueOf(context.get("send"));
        }

        if (sendContext) {
            String userAgent = req.getHeader("user-agent");
            if (StringUtils.isNotBlank(userAgent)) {
                UserAgentSniffer.SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
                StringBuilder sb = new StringBuilder().append(browser.browser).append(" ").append(browser.version);
                String userAgentPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/tasks" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase().replace(" ","_");
                try {
                    setEntityProperties(sb, baseUri, userAgentPath);

                    if (StringUtils.isNotBlank(browser.browser)) {
                        sb = new StringBuilder().append(browser.browser).append(" ").append(browser.version);
                        String browserPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase().replace(" ","_");
                        setEntityProperties(sb, baseUri, browserPath);
                    }
                    // Update OS
                    UserAgentSniffer.SniffedOS os = UserAgentSniffer.sniffOS(userAgent);
                    if (StringUtils.isNotBlank(os.OS)) {
                        sb = new StringBuilder();
                        if (StringUtils.isNotBlank(os.prettyName)) {
                            sb.append(os.prettyName);
                            sb.append(" (");
                        }
                        sb.append(os.OS);
                        if (StringUtils.isNotBlank(os.prettyName)) {
                            sb.append(")");
                        }

                        String osPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTUREE_OS_NAME.toLowerCase().replace(" ","_");
                        setEntityProperties(sb, baseUri, osPath);
                    }

                    if (context != null) {
                        String url = context.get("url");
                        if (StringUtils.isNotBlank(url)) {
                            String urlPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ","_");
                            setEntityProperties(new StringBuilder(url), baseUri, urlPath);
                        }
                        String screenRes = context.get("screenRes");
                        if (StringUtils.isNotBlank(screenRes)) {
                            String screenPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase().replace(" ","_");
                            setEntityProperties(new StringBuilder(screenRes), baseUri, screenPath);
                        }
                        String jQueryVersion = context.get("jQueryVersion");
                        if (StringUtils.isNotBlank(jQueryVersion)) {
                            String jQueryPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ","_");
                            setEntityProperties(new StringBuilder(jQueryVersion), baseUri, jQueryPath);
                        }
                        String documentMode = context.get("documentMode");
                        if (StringUtils.isNotBlank(documentMode)) {
                            String documentPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase().replace(" ","_");
                            setEntityProperties(new StringBuilder(documentPath), baseUri, documentPath);
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setEntityProperties(StringBuilder sb, String baseUrl, String path) throws JSONException {
        URI targetUrl= UriComponentsBuilder.fromUriString(baseUrl)
                .path(path)
                .build()
                .encode()
                .toUri();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        JSONObject request = new JSONObject();
        request.put("content",sb.toString());

        String resourceUrl = targetUrl.toString();
        HttpEntity<String> requestUpdate = new HttpEntity<>(request.toString(),httpHeaders);
        restTemplate.exchange(resourceUrl, HttpMethod.PUT,requestUpdate,Void.class);
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
//
//        // Check that they exist
//        boolean hasUserAgent = StringUtils.isNotBlank(userAgent);
//        boolean hasBrowser = StringUtils.isNotBlank(browser);
//        boolean hasOS = StringUtils.isNotBlank(os);
//        boolean hasUrl = StringUtils.isNotBlank(url);
//        boolean hasScreenRes = StringUtils.isNotBlank(screenRes);
//        boolean hasjQueryVersion = StringUtils.isNotBlank(jQueryVersion);

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
//
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
