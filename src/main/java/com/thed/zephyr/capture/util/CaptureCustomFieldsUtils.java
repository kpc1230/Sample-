package com.thed.zephyr.capture.util;


import com.thed.zephyr.capture.model.CustomFieldMetadata;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by snurulla on 8/22/2017.
 */
@Component
public class CaptureCustomFieldsUtils {

    private static final String CAPTURE_CUSTOM_FIELD_DESCRIPTION = "This custom field is created programatically by Capture for JIRA.";

    // All Environment related custom fields should be prefixed with "Bonfire"
    public static final String CAPTURE_USERAGENT_NAME = "Zephyr Capture for JIRA user agent";
    public static final String CAPTURE_BROWSER_NAME = "Zephyr Capture for JIRA browser";
    public static final String CAPTURE_OS_NAME = "Zephyr Capture for JIRA operating system";
    public static final String CAPTURE_URL_NAME = "Zephyr Capture for JIRA URL";
    public static final String CAPTURE_SCREEN_RES_NAME = "Zephyr Capture for JIRA screen resolution";
    public static final String CAPTURE_JQUERY_VERSION_NAME = "Zephyr Capture for JIRA jQuery version";
    public static final String CAPTURE_DOCUMENT_MODE = "Zephyr Capture for JIRA document mode";

    // IMPORTANT - if we change the plugin key, this will need to change too.
    public static final String JIRA_TEXT_FIELD_KEY = "com.atlassian.jira.plugin.system.customfieldtypes:textfield";
    public static final String TEXT_SEARCHER_KEY = "com.atlassian.jira.plugin.system.customfieldtypes:textsearcher";

    /**
     * the metadata needed to define the custom field in JIRA
     */

    public Map<String, CustomFieldMetadata> getAllCustomFieldsNeedsToCreate() {

        CustomFieldMetadata USERAGENT_METADATA = new CustomFieldMetadata(CAPTURE_USERAGENT_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
                JIRA_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);
        CustomFieldMetadata BROWSER_METADATA = new CustomFieldMetadata(CAPTURE_BROWSER_NAME,
                CAPTURE_CUSTOM_FIELD_DESCRIPTION, JIRA_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);
        CustomFieldMetadata OS_METADATA = new CustomFieldMetadata(CAPTURE_OS_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
                JIRA_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);
        CustomFieldMetadata URL_METADATA = new CustomFieldMetadata(CAPTURE_URL_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
                JIRA_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);
        CustomFieldMetadata SCREEN_RES_METADATA = new CustomFieldMetadata(CAPTURE_SCREEN_RES_NAME, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
                JIRA_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);
        CustomFieldMetadata JQUERY_VERSION_METADATA = new CustomFieldMetadata(CAPTURE_JQUERY_VERSION_NAME,
                CAPTURE_CUSTOM_FIELD_DESCRIPTION, JIRA_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);
        CustomFieldMetadata DOCUMENT_MODE_METADATA = new CustomFieldMetadata(CAPTURE_DOCUMENT_MODE, CAPTURE_CUSTOM_FIELD_DESCRIPTION,
                JIRA_TEXT_FIELD_KEY, TEXT_SEARCHER_KEY);

        Map<String, CustomFieldMetadata> metadataMap = new HashMap<>();
        metadataMap.put(CAPTURE_USERAGENT_NAME, USERAGENT_METADATA);
        metadataMap.put(CAPTURE_BROWSER_NAME, BROWSER_METADATA);
        metadataMap.put(CAPTURE_OS_NAME, OS_METADATA);
        metadataMap.put(CAPTURE_URL_NAME, URL_METADATA);
        metadataMap.put(CAPTURE_SCREEN_RES_NAME, SCREEN_RES_METADATA);
        metadataMap.put(CAPTURE_JQUERY_VERSION_NAME, JQUERY_VERSION_METADATA);
        metadataMap.put(CAPTURE_DOCUMENT_MODE, DOCUMENT_MODE_METADATA);
        return metadataMap;
    }

}
