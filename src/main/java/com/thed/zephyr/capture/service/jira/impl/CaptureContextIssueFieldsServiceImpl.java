package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.jira.CaptureContextIssueFieldsService;
import com.thed.zephyr.capture.util.CaptureCustomFieldsUtils;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.JiraConstants;
import com.thed.zephyr.capture.util.UserAgentSniffer;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by niravshah on 8/28/17.
 */
@Service
public class CaptureContextIssueFieldsServiceImpl implements CaptureContextIssueFieldsService {

    @Autowired
    private Logger log;
    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;


    /**
     * Make a best effort to populate these fields for an issue. If the value for a field cannot be found, then don't fill it in.
     *
     * @param req
     * @param context
     * @param context
     */
    @Override
    public void populateContextFields(HttpServletRequest req, Issue issue, Map<String, String> context) {
        log.debug("populateContextFields: issueKey:{}, contextParam:{}", issue.getKey(), context != null ? context.toString() : "");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String baseUri = host.getHost().getBaseUrl();

        String userAgent = CaptureUtil.getUserAgent(req);

        StringBuilder sbUserAgent = new StringBuilder().append(userAgent);
        if (StringUtils.isNotBlank(userAgent)) {
            UserAgentSniffer.SniffedBrowser browser = UserAgentSniffer.sniffBrowser(userAgent);
            StringBuilder sb = new StringBuilder().append(browser.browser).append(" ").append(browser.version);
            String userAgentPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_USERAGENT_NAME.toLowerCase().replace(" ","_");
            try {
                setEntityPropertiesByThread(sbUserAgent, baseUri, userAgentPath);

                if (StringUtils.isNotBlank(browser.browser)) {
                    sb = new StringBuilder().append(browser.browser).append(" ").append(browser.version);
                    String browserPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_BROWSER_NAME.toLowerCase().replace(" ","_");
                    setEntityPropertiesByThread(sb, baseUri, browserPath);
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
                    setEntityPropertiesByThread(sb, baseUri, osPath);
                }

                if (context != null) {
                    String url = context.get("url");
                    if (StringUtils.isNotBlank(url)) {
                        String urlPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_URL_NAME.toLowerCase().replace(" ","_");
                        setEntityPropertiesByThread(new StringBuilder(url), baseUri, urlPath);
                    }
                    String screenRes = context.get("screenRes");
                    if (StringUtils.isNotBlank(screenRes)) {
                        String screenPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_SCREEN_RES_NAME.toLowerCase().replace(" ","_");
                        setEntityPropertiesByThread(new StringBuilder(screenRes), baseUri, screenPath);
                    }
                    String jQueryVersion = context.get("jQueryVersion");
                    if (StringUtils.isNotBlank(jQueryVersion)) {
                        String jQueryPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_JQUERY_VERSION_NAME.toLowerCase().replace(" ","_");
                        setEntityPropertiesByThread(new StringBuilder(jQueryVersion), baseUri, jQueryPath);
                    }
                    String documentMode = context.get("documentMode");
                    if (StringUtils.isNotBlank(documentMode)) {
                        String documentPath = JiraConstants.REST_API_BASE_ISSUE  + "/" + issue.getKey() + "/properties"+ "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_DOCUMENT_MODE.toLowerCase().replace(" ","_");
                        setEntityPropertiesByThread(new StringBuilder(documentMode), baseUri, documentPath);
                    }
                }
            } catch(Exception e) {
                log.error("Error populating Context Parameters", e);
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
            String response = atlassianHostRestClients.authenticatedAsAddon().getForObject(targetUrl, String.class);
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
            //do nothing
            //log.warn("Error Retrieving Context Parameters", e.getMessage());
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void addRaisedInIssueField(String user, List<Long> listOfIssueIds, Session session) {
        if(session != null) {
            log.trace("addRaisedInIssueField request from user:{} , sessionId: {} jiraPropIndex:{}", user, session.getId(), session.getJiraPropIndex());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
            AcHostModel acHostModel = (AcHostModel)host.getHost();
            listOfIssueIds.stream().forEach(issueId -> {
                addSessionContextIntoRaisedIssue(acHostModel, user, issueId, session);
            });
        }
    }

    @Override
    public void addSessionContextIntoRaisedIssue(AcHostModel acHostModel, String user, Long issueId, Session session) {
        log.trace("Add Session context into raised Issue user:{} , sessionId: {} jiraPropIndex:{} issueId:{}", user, session.getId(), session.getJiraPropIndex(), issueId);
        String raisedInPath = JiraConstants.REST_API_BASE_ISSUE + "/" + issueId + "/properties" + "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_RAISEDIN_NAME.toLowerCase().replace(" ", "_");
        try {
            StringBuilder sb = new StringBuilder(session.getJiraPropIndex());
            setEntityProperties(sb, acHostModel.getBaseUrl(), raisedInPath);
        } catch (Exception e) {
            log.error("Error during adding Session context into RaisedIn Issie user:{} sessionId: {} jiraPropIndex:{} issueId:{}", user, session.getId(), session.getJiraPropIndex(), issueId);
        }
    }


    @Override
    public void removeRaisedIssue(Session loadedSession, String issueKey) {
        log.debug("removeRaisedIssue request for SessionId: {}", loadedSession.getId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String baseUrl = host.getHost().getBaseUrl();
        String raisedInPath = JiraConstants.REST_API_BASE_ISSUE + "/" + issueKey + "/properties" + "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_RAISEDIN_NAME.toLowerCase().replace(" ", "_");
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            URI targetUrl= UriComponentsBuilder.fromUriString(baseUrl)
                    .path(raisedInPath)
                    .build()
                    .encode()
                    .toUri();

            atlassianHostRestClients.authenticatedAsAddon().delete(targetUrl);
        } catch (Exception e) {
            log.error("Error removeRaisedIssue to Session:{}", loadedSession.getId());
        }
    }


    @Override
    public void populateIssueTestStatusAndTestSessions(String issueKey, String testStatus, String jiraPropIndexs, String baseUrl) {
        log.debug("populateIssueTestStatusAndTestSessions started: issueKey:{}, testSessions:{}, testStatus:{}, baseUrl:{}", issueKey, jiraPropIndexs, testStatus, baseUrl);
        String testStatusPath = JiraConstants.REST_API_BASE_ISSUE + "/" + issueKey + "/properties" + "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_TEST_STATUS.toLowerCase().replace(" ", "_");
        try {
            StringBuilder sb = new StringBuilder(testStatus);
            setEntityProperties(sb, baseUrl, testStatusPath);
        } catch (Exception e) {
            log.error("Error during setting TestStatus Jira issue entity properties",e);
        }
        String testSessionsPath = JiraConstants.REST_API_BASE_ISSUE + "/" + issueKey + "/properties" + "/" + CaptureCustomFieldsUtils.ENTITY_CAPTURE_TEST_SESSIONS.toLowerCase().replace(" ", "_");
        try {
            if(StringUtils.isNotBlank(jiraPropIndexs)){
                StringBuilder sb = new StringBuilder(jiraPropIndexs);
                setEntityProperties(sb, baseUrl, testSessionsPath);
            }else {
                removeEntityProperties(baseUrl, testSessionsPath);
            }


        } catch (Exception e) {
            log.error("Error populateIssueTestStatusAndTestSessions",e);
        }
        log.debug("populateIssueTestStatusAndTestSessions Completed");
    }
    private void setEntityPropertiesByThread(StringBuilder sb, String baseUrl, String path) {
        CompletableFuture.runAsync(() -> {
            log.debug("Adding JIRA Issue context field Started.. baseUri :{}, path : {} , value : {} ", baseUrl, path, sb);
            try {
                setEntityProperties(sb, baseUrl, path);
            } catch (Exception e) {
                log.error("Error populating Context Parameters", e);
            }
            log.debug("Adding JIRA Issue context field Completed.. baseUri :{}, path : {} , value : {} ", baseUrl, path, sb);
        });
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
        ResponseEntity<Void> exchange = atlassianHostRestClients.authenticatedAsAddon().exchange(resourceUrl, HttpMethod.PUT, requestUpdate, Void.class);
    }
    private void removeEntityProperties(String baseUrl, String path) throws JSONException {
        try {
            URI targetUrl= UriComponentsBuilder.fromUriString(baseUrl).path(path).build().encode().toUri();
            log.debug("removeEntityProperties --> {}",targetUrl);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            JSONObject request = new JSONObject();
            //request.put("content",sb.toString());
            String resourceUrl = targetUrl.toString();
            HttpEntity<String> requestUpdate = new HttpEntity<>(request.toString(),httpHeaders);
            atlassianHostRestClients.authenticatedAsAddon().exchange(resourceUrl, HttpMethod.DELETE,requestUpdate,Void.class);
        } catch (HttpClientErrorException restException) {
            if (StringUtils.equals(restException.getMessage(), "404 Not Found")){
                log.warn("Error during remove property. The Jira issue entity property wasn't found, hence can't be removed. Which is probably fine :) baseUrl:{} path:{}", baseUrl, path);
            } else{
                throw restException;
            }
        }
    }
}
