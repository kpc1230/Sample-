package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.JiraAuthService;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by snurulla on 8/17/2017.
 */
@Service
public class JiraAuthServiceImpl implements JiraAuthService {
    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Autowired
    DynamoDBAcHostRepository dynamoDBAcHostRepository;

    @Override
    public Boolean authenticateWithJita(String username, String password, String baseURL) {

        String uri = baseURL + JiraConstants.REST_AUTH_CHECK_URL;
        try {
            String pass = "Basic " + CaptureUtil.base64(username + ":" + password);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", pass);
            headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            RestTemplate restTemplate2 = new RestTemplate();
            ResponseEntity<JsonNode> response = restTemplate2.exchange(uri, HttpMethod.GET, entity, JsonNode.class);
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
