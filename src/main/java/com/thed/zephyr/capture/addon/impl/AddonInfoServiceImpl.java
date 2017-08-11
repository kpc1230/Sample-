package com.thed.zephyr.capture.addon.impl;

import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class AddonInfoServiceImpl implements AddonInfoService {

    @Autowired
    private Logger log;

    @Autowired
    private Environment env;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

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
}
