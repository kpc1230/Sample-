package com.thed.zephyr.capture.addon.impl;

import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

public class AddonInfoServiceImpl implements AddonInfoService {

    @Autowired
    private Logger log;

    @Autowired
    private Environment env;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Autowired
    private AddonDescriptorLoader ad;

    @Override
    public AddonInfo getAddonInfo(AcHostModel acHostModel) throws RestClientException {
        String pluginKey = env.getProperty(ApplicationConstants.PLUGIN_KEY);
        String uri = acHostModel.getBaseUrl() + JiraConstants.REST_API_ADD_ON_INFO + "/" + pluginKey;
        try {
            AddonInfo response = restTemplate.getForObject(uri, AddonInfo.class);
            return response;
        } catch (RestClientException exception) {
            log.error("Error during getting addon information from jira.", exception);
            throw exception;
        }
    }

    @Override
    public boolean createOrUpdateProperty(AcHostModel acHostModel, String propName, JsonNode jsonNode) {
        if(propName != null && jsonNode != null){
            String uri = acHostModel.getBaseUrl() + JiraConstants.REST_ADD_ON_PROPERTY
                    .replace("{addon-key}",ad.getDescriptor().getKey())
                    .concat("/")
                    .concat(propName);
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<String>(jsonNode.toString(), headers);
                ResponseEntity<JsonNode> response = restTemplate.exchange(uri, HttpMethod.PUT, entity, JsonNode.class);
                log.debug("Addon Property Created: Result - status (" + response.getStatusCode() + ") has body: " + response.hasBody());
                if (response != null && response.getStatusCode() == HttpStatus.OK) {
                    return true;
                } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    return false;
                }
            } catch (Exception exception) {
                log.error("Error during creating addon property ", exception);
            }
            return true;
        }
        return false;
    }

    @Override
    public JsonNode getProperty(AcHostModel acHostModel, String propName) {
        if(propName != null){
            String uri = acHostModel.getBaseUrl() + JiraConstants.REST_ADD_ON_PROPERTY
                    .replace("{addon-key}",ad.getDescriptor().getKey())
                    .concat("/")
                    .concat(propName);
            try {
                JsonNode response = restTemplate.getForObject(uri, JsonNode.class);
                if (response != null) {
                    return response;
                }
            } catch (Exception e) {}
        }
        return null;
    }

    @Override
    public boolean deleteProperty(AcHostModel acHostModel, String propName) {
        if(propName != null){
            String uri = acHostModel.getBaseUrl() + JiraConstants.REST_ADD_ON_PROPERTY
                    .replace("{addon-key}",ad.getDescriptor().getKey())
                    .concat("/")
                    .concat(propName);
            try {
                restTemplate.delete(uri);
                return true;
            } catch (Exception exception) {
                log.error("Error during getting addon property ", exception);
            }
        }
        return false;
    }
}
