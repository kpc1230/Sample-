package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.service.UserService;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Created by Masud on 8/15/17.
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Override
    public JsonNode getUserProperty(String userName, String propName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_BASE_USER_PROPERTIES + "/" + propName + "?username=" + userName;

        try {
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            return response;
        } catch (RestClientException exception) {
            log.error("Error during getting user property from jira.", exception);
            // throw exception;
            return null;
        }
    }

    @Override
    public JsonNode getAllUserProperties(String userName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_BASE_USER_PROPERTIES + "?username=" + userName;
        try {
            JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
            return response;
        } catch (RestClientException exception) {
            log.error("Error during getting user properties from jira.", exception);
            throw exception;
        }
    }

    @Override
    public boolean deleteUserProperty(String userName, String propName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_BASE_USER_PROPERTIES + "/" + propName;
        try {
            restTemplate.delete(uri);
            return true;
        } catch (RestClientException exception) {
            log.error("Error during deleting the user property from jira.", exception);
            throw exception;
        }

    }

    @Override
    public boolean createOrUpdateUserProperty(String userName, String propName, JsonNode jsonNode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_BASE_USER_PROPERTIES + "/" + propName + "?username=" + userName;
        try {
            restTemplate.put(uri, jsonNode);
            return true;
        } catch (RestClientException exception) {
            log.error("Error during getting project from jira.", exception);
            throw exception;
        }
    }

}
