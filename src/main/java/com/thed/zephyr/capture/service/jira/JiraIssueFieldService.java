package com.thed.zephyr.capture.service.jira;


import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.CustomFieldMetadata;

/**
 * Created by snurulla on 8/22/2017.
 */
public interface JiraIssueFieldService {

    public JsonNode createCustomField(CustomFieldMetadata fieldMetadata);

    public JsonNode getAllCustomFields();

    public void createDefaultCustomeFields();

}
