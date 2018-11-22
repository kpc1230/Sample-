package com.thed.zephyr.capture.service.extension.impl;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.be.BEAuthToken;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.JiraConstants;
import com.thed.zephyr.capture.util.PlainRestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

/**
 * Created by snurulla on 8/17/2017.
 */
@Service
public class JiraAuthServiceImpl implements JiraAuthService {

    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    @Autowired
    private Logger log;
    @Autowired
    private PlainRestTemplate restTemplate;
    @Autowired
    private AtlassianHostRepository atlassianHostRepository;

    @Override
    public BEAuthToken authenticateWithJira(String userKey, String password, String baseURL,String userAgent) {
        AcHostModel host = (AcHostModel)atlassianHostRepository.findFirstByBaseUrl(baseURL).get();
        CaptureUser captureUser = getUserDetails(userKey, password, baseURL);
        ResponseEntity<JsonNode> response = callJiraForAuthenticationAndCookie(userKey, password, baseURL);
        if (response != null && response.getStatusCode() == HttpStatus.OK && captureUser != null) {
            JsonNode sessionNode = response.getBody().get(ApplicationConstants.SESSION);
            String jiraToken = sessionNode.get(NAME).asText() + "=" + sessionNode.get(VALUE).asText();

            return createBEAuthTokenWithCookies(host.getCtId(), captureUser.getKey(), captureUser.getAccountId(), System.currentTimeMillis(), jiraToken, userAgent);
        } else if(captureUser != null) {

            return createBEAuthTokenWithApiToken(host.getCtId(), captureUser.getKey(), captureUser.getAccountId(), System.currentTimeMillis(), password, userAgent);
        }

        return null;
    }

    @Override
    public CaptureUser getUserDetails(String userKey, String password, String baseURL) {
        ResponseEntity<CaptureUser> responseEntity = getUserFromJira(userKey, password, baseURL);

        return responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK?responseEntity.getBody():null;
    }


    @Override
    public BEAuthToken createBEAuthTokenWithCookies(String clientKey, String userKey, String userAccountId, long timestamp, String jiraToken, String userAgent){
        BEAuthToken beAuthToken = new BEAuthToken(clientKey, userKey, userAccountId, timestamp, userAgent);
        beAuthToken.setJiraToken(jiraToken);

        return beAuthToken;
    }

    @Override
    public BEAuthToken createBEAuthTokenWithApiToken(String clientKey, String userKey, String userAccountId, long timestamp, String apiToken, String userAgent){
        BEAuthToken beAuthToken = new BEAuthToken(clientKey, userKey, userAccountId, timestamp, userAgent);
        beAuthToken.setApiToken(apiToken);

        return beAuthToken;
    }

    @Override
    public BEAuthToken createBEAuthTokenFromString(String token){
        String[] tokenParts = token.split("__");
        String clientKey = tokenParts[0];
        String userKey = tokenParts[1];
        String userAccountId = tokenParts[2];
        long timestamp = Long.valueOf(tokenParts[3]);
        String jiraToken = !StringUtils.equals(tokenParts[4], "null")?tokenParts[4]:null;
        String userAgent = tokenParts[5];
        String apiToken = null;
        if(tokenParts.length > 6){
            apiToken = !StringUtils.equals(tokenParts[6], "null")?tokenParts[6]:null;
        }

        return new BEAuthToken(clientKey, userKey, userAccountId, timestamp, jiraToken, userAgent, apiToken);
    }

    @Override
    public String createStringTokenFromBEAuthToken(BEAuthToken beAuthToken){
        StringBuffer sb = new StringBuffer();
        sb.append(beAuthToken.getCtId() + "__");
        sb.append(beAuthToken.getUserKey() + "__");
        sb.append(beAuthToken.getUserAccountId() + "__");
        sb.append(beAuthToken.getTimestamp() + "__");
        sb.append((beAuthToken.getJiraToken() != null?beAuthToken.getJiraToken():"null") + "__");
        sb.append(beAuthToken.getUserAgent() + "__");
        sb.append(beAuthToken.getApiToken() !=null?beAuthToken.getApiToken():"null");

        return sb.toString();
    }

    private ResponseEntity<CaptureUser> getUserFromJira(String username, String password, String baseURL){
        ResponseEntity<CaptureUser> responseEntity = null;
        String uri = baseURL + JiraConstants.REST_USER_MYSELF;
        try {
            String pass = "Basic " + CaptureUtil.base64(username + ":" + password);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pass);
            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, CaptureUser.class);
        } catch (Exception exception) {
            log.error("Error during getting the user details from JIRA ", exception);
        }

        return responseEntity;
    }

    private ResponseEntity<JsonNode> callJiraForAuthenticationAndCookie(String userKey, String password, String baseURL){
        ResponseEntity<JsonNode> response = null;
        String uri = baseURL + JiraConstants.REST_AUTH_CHECK_URL;
        try {
            HttpHeaders headers = new HttpHeaders();
            JSONObject request = new JSONObject();
            request.put(USERNAME, userKey);
            request.put(PASSWORD, password);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
            response = restTemplate.exchange(uri, HttpMethod.POST, entity, JsonNode.class);

        } catch (Exception exception) {
            log.error("Error during authentication user credentials with JIRA ", exception);
        }

        return response;
    }
}
