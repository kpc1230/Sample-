package com.thed.zephyr.capture.addon;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.UnauthorizedException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import org.springframework.web.client.RestClientException;


public interface AddonInfoService {
    AddonInfo getAddonInfo(AcHostModel acHostModel) throws RestClientException, UnauthorizedException;
    boolean createOrUpdateProperty(AcHostModel acHostModel, String propName, JsonNode jsonNode);
    JsonNode getProperty(AcHostModel acHostModel, String propName);
    boolean deleteProperty(AcHostModel acHostModel, String propName);
}
