package com.thed.zephyr.capture.service.extension.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.JiraConstants;
import com.thed.zephyr.capture.util.PlainRestTemplate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Created by snurulla on 8/17/2017.
 */
@Service
public class JiraAuthServiceImpl implements JiraAuthService {
    @Autowired
    private Logger log;

    @Autowired
    private PlainRestTemplate restTemplate;

    @Autowired
    DynamoDBAcHostRepository dynamoDBAcHostRepository;

    @Override
    public Boolean authenticateWithJira(String username, String password, String baseURL) {

        String uri = baseURL + JiraConstants.REST_AUTH_CHECK_URL;
        try {
            String pass = "Basic " + CaptureUtil.base64(username + ":" + CaptureUtil.decodeBase64(password));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pass);
            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.GET, entity, JsonNode.class);
            log.debug("User credentials authenticated with JIRA : Result - status (" + response.getStatusCode() + ") has body: " + response.hasBody());
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                return true;
            }
        } catch (RestClientException exception) {
            log.error("Error during authentication user credentials with JIRA ", exception);
        }
        return false;
    }

    @Override
    public AcHostModel getAcHostModelbyBaseUrl(String baseUrl) {
        return (AcHostModel) dynamoDBAcHostRepository.findFirstByBaseUrl(baseUrl).get();
    }
}
