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
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
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
     * @param req
     * @param context
     * @param context
     */
    @Override
    public void populateContextFields(HttpServletRequest req, Issue issue, Map<String, String> context) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String baseUri = host.getHost().getBaseUrl();

        String userAgent = req.getHeader("user-agent");
        if (StringUtils.isNotBlank(userAgent)) {
            UserAgentSniffer.SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
            StringBuilder sb = new StringBuilder().append(browser.browser).append(" ").append(browser.version);
            String userAgentPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase().replace(" ","_");
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

    @Override
    public String getContextFields(String baseUrl, String path,String key) {
        URI targetUrl= UriComponentsBuilder.fromUriString(baseUrl)
                .path(path)
                .build()
                .encode()
                .toUri();

        try {
            String response = restTemplate.getForObject(targetUrl, String.class);
            if (StringUtils.isNotBlank(response)) {
                JSONObject jsonObject = new JSONObject(response);
                String value = null;
                value = jsonObject.getString("value");

                if (StringUtils.isNotBlank(response)) {
                    JSONObject jsonContentObject = new JSONObject(value);
                    return jsonContentObject.getString("content");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
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
}
