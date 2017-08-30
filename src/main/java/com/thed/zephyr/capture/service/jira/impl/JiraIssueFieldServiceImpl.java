package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.CustomFieldMetadata;
import com.thed.zephyr.capture.service.jira.JiraIssueFieldService;
import com.thed.zephyr.capture.util.CaptureCustomFieldsUtils;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by snurulla on 8/22/2017.
 */
@Service
public class JiraIssueFieldServiceImpl implements JiraIssueFieldService {

    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Autowired
    CaptureCustomFieldsUtils captureCustomFieldsUtils;

    @Override
    public JsonNode createCustomField(CustomFieldMetadata fieldMetadata) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_CUSTOM_FIELD;
        JsonNode reqJson = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            reqJson = mapper.convertValue(fieldMetadata, JsonNode.class);
            JsonNode respJson = restTemplate.postForObject(uri, reqJson, JsonNode.class);
            return respJson;
        } catch (RestClientException exception) {
            log.error("Error during creating the Custom field with details : " + reqJson.toString(), exception);
            return null;
        }
    }

    @Override
    public JsonNode getAllCustomFields() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        try {
            String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_CUSTOM_FIELD;
            JsonNode respJson = restTemplate.getForObject(uri, JsonNode.class);
            return respJson;
        } catch (RestClientException exception) {
            log.error("Error during getting the all custom fields.", exception);
            return null;
        }
    }

    @Override
    public void createDefaultCustomeFields() {
        log.debug("Started - > createDefaultCustomeFields");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_CUSTOM_FIELD;
        Map<String, CustomFieldMetadata> customFieldMaps = captureCustomFieldsUtils.getAllCustomFieldsNeedsToCreate();
        JsonNode json = getAllCustomFields();
        Set<String> jirsCFList = new HashSet<>();
        if (json != null && json.isArray()) {
            json.forEach(node -> jirsCFList.add(node.get("name").asText()));
        }
        customFieldMaps.entrySet().parallelStream().forEach(cf -> {
            if (!jirsCFList.contains(cf.getKey())) {
                CustomFieldMetadata cfmd = cf.getValue();
                log.debug("Custom field is not exist :  Creating new with details  :  " + cfmd.toString());
                createCustomField(host, cfmd);
            } else {
                log.debug("Custom field is already exist :  Custom Field name :  " + cf.getKey());
            }
        });
        log.debug("End - > createDefaultCustomeFields");

    }

    private JsonNode createCustomField(AtlassianHostUser host, CustomFieldMetadata fieldMetadata) {
        String uri = host.getHost().getBaseUrl() + JiraConstants.REST_API_CUSTOM_FIELD;
        JsonNode reqJson = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            reqJson = mapper.convertValue(fieldMetadata, JsonNode.class);
            JsonNode respJson = restTemplate.postForObject(uri, reqJson, JsonNode.class);
            return respJson;
        } catch (RestClientException exception) {
            log.error("Error during creating the Custom field with details : " + reqJson.toString(), exception);
            return null;
        }
    }
}
