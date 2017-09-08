package com.thed.zephyr.capture.service.extension.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.Global.TokenHolder;
import com.thed.zephyr.capture.util.JiraConstants;
import com.thed.zephyr.capture.util.PlainRestTemplate;
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
    DynamoDBAcHostRepository dynamoDBAcHostRepository;

    @Autowired
    private TokenHolder tokenHolder;

    @Override
    public Boolean authenticateWithJira(String username, String password, String baseURL) {

        String uri = baseURL + JiraConstants.REST_AUTH_CHECK_URL;
        try {
            HttpHeaders headers = new HttpHeaders();
            JSONObject request = new JSONObject();
            request.put(USERNAME, username);
            request.put(PASSWORD, CaptureUtil.decodeBase64(password));
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.POST, entity, JsonNode.class);
            log.debug("User credentials authenticated with JIRA : Result - status (" + response.getStatusCode() + ") has body: " + response.hasBody());
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                JsonNode sessionNode = response.getBody()
                        .get(ApplicationConstants.SESSION);
                String tokenKey = sessionNode.get(NAME).asText();
                String tokenValue = sessionNode.get(VALUE).asText();
                tokenHolder.setTokenKey(tokenKey);
                tokenHolder.setTokenValue(tokenValue);
                log.debug("JIRA JWT token: {}", tokenValue);
                return true;
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return false;
            }
        } catch (Exception exception) {
            log.error("Error during authentication user credentials with JIRA ", exception);
        }
        return false;
    }

    @Override
    public AcHostModel getAcHostModelbyBaseUrl(String baseUrl) {
        return (AcHostModel) dynamoDBAcHostRepository.findFirstByBaseUrl(baseUrl).get();
    }

    @Override
    public CaptureUser getUserDetails(String username, String password, String baseURL) {
        CaptureUser user = null;
        String uri = baseURL + JiraConstants.REST_USER_MYSELF;
        try {
            String pass = "Basic " + CaptureUtil.base64(username + ":" + CaptureUtil.decodeBase64(password));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pass);
            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            ResponseEntity<CaptureUser> response = restTemplate.exchange(uri, HttpMethod.GET, entity, CaptureUser.class);
            log.debug("Getting the user details from JIRA : Result - status (" + response.getStatusCode() + ") has body: " + response.hasBody());
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                user = response.getBody();
            }
        } catch (Exception exception) {
            log.error("Error during getting the user details from JIRA ", exception);
        }
        return user;
    }
}
