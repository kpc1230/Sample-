package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.JiraConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.concurrent.Callable;

/**
 * Created by Masud on 8/15/17.
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private Logger log;
    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private DynamicProperty dynamicProperty;
    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;

    @Override
    public JsonNode getUserProperty(String userName, String propName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_BASE_USER_PROPERTIES + "/" + propName + "?username=" + userName;

        try {
            JsonNode response = atlassianHostRestClients.authenticatedAsAddon().getForObject(uri, JsonNode.class);
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
            JsonNode response = atlassianHostRestClients.authenticatedAsAddon().getForObject(uri, JsonNode.class);
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
            atlassianHostRestClients.authenticatedAsAddon().delete(uri);
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
            atlassianHostRestClients.authenticatedAsAddon().put(uri, jsonNode);
            return true;
        } catch (RestClientException exception) {
            log.error("Error during getting project from jira.", exception);
            throw exception;
        }
    }

    /**
     * Get Assignable users by project
     * @param projectKey
     * @return
     */
    @Override
    public JsonNode getAssignableUserByProjectKey(String projectKey,String username){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

        String uri = host.getHost().getBaseUrl();
        URI targetUrl= UriComponentsBuilder.fromUriString(uri)
                .path(JiraConstants.REST_API_ASSIGNABLE_USER)
                .queryParam("project", projectKey)
                .queryParam("username", StringUtils.isNotEmpty(username)?username:"")
                .build()
                .encode()
                .toUri();
        try {
            String response = atlassianHostRestClients.authenticatedAsAddon().getForObject(targetUrl, String.class);
            return new ObjectMapper().readTree(response);
        } catch (Exception exception) {
            log.error("Error during getting assignable user by project from jira.", exception);
        }
        return null;
    }

    /**
     * Get Assignable users by project
     * @param projectKey
     * @return
     */
    @Override
    public JsonNode getAssignableUserByProjectKey(String projectKey){
        return getAssignableUserByProjectKey(projectKey,null);
    }

    @Override
    public CaptureUser findUserByName(String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        String uri = host.getHost().getBaseUrl();
        CaptureUser captureUser = null;
        try {
            captureUser = tenantAwareCache.getOrElse(acHostModel, buildUserCacheKey(username), new Callable<CaptureUser>() {
                @Override
                public CaptureUser call() throws Exception {
                    URI targetUrl = UriComponentsBuilder.fromUriString(uri)
                            .path(JiraConstants.REST_API_USER)
                            .queryParam("username", username)
                            .build()
                            .encode()
                            .toUri();

                    CaptureUser response = atlassianHostRestClients.authenticatedAsAddon().getForObject(targetUrl, CaptureUser.class);
                    return response;
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.USER_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());

        } catch (Exception exception) {
            log.error("Error during getting user by username from jira.", exception);
        }
        return captureUser;
    }

    @Override
    public CaptureUser findUserByKey(String key) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        return findUserByKey(acHostModel, key);
    }
    
    public CaptureUser findUserByKey(AcHostModel acHostModel, String key) {
        String uri = acHostModel.getBaseUrl();
        CaptureUser captureUser = null;
        try {
            captureUser = tenantAwareCache.getOrElse(acHostModel, buildUserCacheKey(key), new Callable<CaptureUser>() {
                @Override
                public CaptureUser call() throws Exception {
                    URI targetUrl = UriComponentsBuilder.fromUriString(uri)
                            .path(JiraConstants.REST_API_USER)
                            .queryParam("key", key)
                            .build()
                            .encode()
                            .toUri();

                    CaptureUser response = atlassianHostRestClients.authenticatedAsAddon().getForObject(targetUrl, CaptureUser.class);
                    return response;
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.USER_CACHE_EXPIRATION_DYNAMIC_PROP, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());

        } catch (Exception exception) {
            log.error("Error during getting user by user key from jira.", exception);
        }
        return captureUser;
    }

    private String buildUserCacheKey(String userIdOrKey){
        return ApplicationConstants.USER_CACHE_KEY_PREFIX+userIdOrKey;
    }

}
