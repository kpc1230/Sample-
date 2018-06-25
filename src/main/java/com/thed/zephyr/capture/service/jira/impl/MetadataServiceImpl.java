package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.CustomField;
import com.thed.zephyr.capture.model.jira.FieldOption;
import com.thed.zephyr.capture.model.jira.FixVersionsFieldOption;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.jira.GroupService;
import com.thed.zephyr.capture.service.jira.IssueTypeService;
import com.thed.zephyr.capture.service.jira.MetadataService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.JiraConstants;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.thed.zephyr.capture.util.ApplicationConstants.*;

/**
 * Created by Masud on 8/22/17.
 */
@Service
public class MetadataServiceImpl implements MetadataService {

    @Autowired
    private Logger log;
    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;
    @Autowired
    private IssueTypeService issueTypeService;
    @Autowired
    private UserService userService;
    @Autowired
    private CaptureI18NMessageSource i18n;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private DynamicProperty dynamicProperty;

    @Override
    public Map<String, Object> createFieldScreenRenderer(CaptureProject captureProject) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser hostUser = (AtlassianHostUser) auth.getPrincipal();

        Map<String, Object> resultMap = new HashMap<>();

        List<Map<String, Object>> fieldMap = new ArrayList<>();
        Map<String, Object> fieldValueMap = new HashMap<>();
        List<JSONObject> issuelinksTypesList = new ArrayList<>();

        List<IssuelinksType> issuelinksTypes = issueTypeService.getIssuelinksType();
        if (issuelinksTypes != null) {
            issuelinksTypes.forEach(issuelinksType -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", issuelinksType.getInward());
                jsonObject.put("value", issuelinksType.getInward());
                issuelinksTypesList.add(jsonObject);
                if (issuelinksType.getInward() != null && !issuelinksType.getInward().equalsIgnoreCase(issuelinksType.getOutward())) {
                    JSONObject jsonObject2 = new JSONObject();
                    jsonObject2.put("text", issuelinksType.getOutward());
                    jsonObject2.put("value", issuelinksType.getOutward());
                    issuelinksTypesList.add(jsonObject2);
                }

            });
        }

        try {
            String response = getMetaDataCacheOrFresh(hostUser,captureProject.getKey(),captureProject.getId());
            if(StringUtils.isNotEmpty(response)) {
                JsonNode jsonNode = new ObjectMapper().readTree(response).get(PROJECTS);
                for (final JsonNode pN : jsonNode) {
                    JsonNode itNode = pN.get(ISSUE_TYPES);
                    for (final JsonNode iN : itNode) {
                        Map<String, Object> iFMap = new HashMap<>();

                        com.thed.zephyr.capture.model.jira
                                .IssueType issueType = new ObjectMapper()
                                .readValue(iN.toString(), com.thed.zephyr.capture.model.jira.
                                        IssueType.class);
                        JsonNode fN = iN.get(FIELDS);
                        iFMap.put(FIELDS, fN);
                        iFMap.put(ISSUE_TYPE, issueType);

                        for (final JsonNode vN : fN) {
                            JsonNode options = vN.has(ALLOWED_VALUES) ? vN.get(ALLOWED_VALUES) : new ObjectMapper().createArrayNode();
                            List<FieldOption> fieldOptions = new ArrayList<>();
                            String key = vN.get(KEY).asText();
                            if (key.equals("assignee")) {
                                fieldOptions = addAssigneeOptions();
                            }
                            List<FieldOption> finalFieldOptions = fieldOptions;
                            options.forEach(jsonNode1 -> {
                                String name = jsonNode1.has("name") ? jsonNode1.get("name").asText() :
                                        jsonNode1.get("value").asText();
                                String value = jsonNode1.has("id") ? jsonNode1.get("id").asText() : null;
                                List<FieldOption> children = new ArrayList<>();
                                ArrayNode childrenJson = (ArrayNode) jsonNode1.get("children");
                                if (childrenJson != null) {
                                    for (JsonNode childJson : childrenJson) {
                                        String childText = childJson.get("value").asText();
                                        String childValue = childJson.get("id").asText();
                                        children.add(new FieldOption(childText, childValue));
                                    }
                                }
                                if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                                    if (key.equals("fixVersions")) {
                                        Boolean released = jsonNode1.get("released").asBoolean();
                                        finalFieldOptions.add(new FixVersionsFieldOption(name, value, children, released));
                                    } else {
                                        finalFieldOptions.add(new FieldOption(name, value, children));
                                    }
                                }
                            });

                            ObjectNode objectNode = (ObjectNode) vN;
                            if (!key.equalsIgnoreCase("issuelinks")) {
                                objectNode.set(OPTIONS, new ObjectMapper().valueToTree(finalFieldOptions));
                            } else {
                                if (issuelinksTypesList.size() > 0) {
                                    objectNode.set(OPTIONS, new ObjectMapper().convertValue(issuelinksTypesList, JsonNode.class));
                                } else {
                                    objectNode.set(OPTIONS, new ObjectMapper().valueToTree(fieldOptions));
                                }
                            }
                            CustomField customField = new ObjectMapper()
                                    .readValue(vN.toString(), CustomField.class);
                            String typeKey = customField.getSchema() != null &&
                                    customField.getSchema().getCustom() != null ?
                                    customField.getSchema().getCustom() : customField.getSchema().getType();
                            objectNode.put("typeKey", typeKey);
                            boolean systemField = false;
                            if (customField.getSchema() != null
                                    && customField.getSchema().getSystem() != null
                                    && customField.getSchema().getSystem().equals(customField.getKey())) {
                                systemField = true;
                            }
                            if (customField != null && customField.getSchema().getCustom() != null &&
                                    customField.getSchema().getCustom().endsWith("grouppicker")) {
                                objectNode.set(OPTIONS, new ObjectMapper().valueToTree(groupService.findGroups(null)));
                            }
                            objectNode.put("systemField", systemField);
                            objectNode.remove(ALLOWED_VALUES);
                            fieldValueMap.put(key, vN);
                        }

                        fieldMap.add(iFMap);
                    }
                }
            }

            JsonNode userNode = userService.getAssignableUserByProjectKey(captureProject.getKey());
            List<FieldOption> userBeans = new ArrayList<>();
            if(userNode != null && userNode.size() > 0) {
                userNode.forEach(jsonNode1 -> {
                    userBeans.add(new FieldOption(
                            jsonNode1.get("displayName").asText(),
                            jsonNode1.get("name").asText()
                    ));

                });
            }
            resultMap.put(FIELD_LIST_BEANS,fieldMap);
            resultMap.put(FIELD_DETAILS,fieldValueMap);
            resultMap.put(USER_BEANS, userBeans);
            return resultMap;
        } catch (Exception exception) {
            log.error("Error during getting issue from jira.", exception);
         }

         return resultMap;
    }

    @Override
    public String getMetaDataCacheOrFresh(AtlassianHostUser hostUser, String projectKey, Long projectId) {
        String metadata = null;
        try {
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            metadata = tenantAwareCache.getOrElse(acHostModel, createProjectMetaKey(projectKey), () -> {
                List<IssueType> issueTypes = issueTypeService.getIssueTypesByProject(projectId);
                List<Long> issueTypeIds = new ArrayList<>();
                issueTypes.forEach(issueType -> {
                    issueTypeIds.add(issueType.getId());
                });
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
                String uri = host.getHost().getBaseUrl();
                URI targetUrl= UriComponentsBuilder.fromUriString(uri)
                        .path(JiraConstants.REST_API_CREATE_ISSUE_SCHEMA)
                        .queryParam("projectIds", projectId)
                        .queryParam("issuetypeIds", issueTypeIds.toArray())
                        .queryParam("expand", ApplicationConstants.METADATA_EXPAND_KEY)
                        .build()
                        .encode()
                        .toUri();
                String response = atlassianHostRestClients.authenticatedAsAddon().getForObject(targetUrl, String.class);
                log.debug("Response for metadata {} {}",projectId,response);
                return response;
            }, dynamicProperty.getIntProp(ApplicationConstants.METADATA_CACHE_EXPIRATION_DYNAMIC_PROP,ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());

        } catch (Exception exp) {
            log.error("Exception while getting the project metadata from JIRA." + exp.getMessage(), exp);
        }
        return metadata;
    }

    @Override
    public String getIssueAttachementMetaCacheOrFresh(AtlassianHostUser hostUser) {
        String metadata = null;
        try {
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            metadata = tenantAwareCache.getOrElse(acHostModel, ApplicationConstants.ISSUE_ATTACH_METADATA_CACHE_KEY_PREFIX, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
                    String uri = host.getHost().getBaseUrl();
                    URI targetUrl= UriComponentsBuilder.fromUriString(uri)
                            .path(JiraConstants.REST_API_ISSUE_ATTACHEMENT_META_DATE)
                            .build()
                            .encode()
                            .toUri();
                    String response = atlassianHostRestClients.authenticatedAsAddon().getForObject(targetUrl, String.class);
                    log.debug("Response for issue attachment metadata {} {}",response);
                    return response;
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.METADATA_CACHE_EXPIRATION_DYNAMIC_PROP,ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());

        } catch (Exception exp) {
            log.error("Exception while getting the issue attachment metadata from JIRA." + exp.getMessage(), exp);
        }
        return metadata;
    }

    private List<FieldOption> addAssigneeOptions() {
        List<FieldOption> fieldOpts = new ArrayList<>();
        // -1 is used by JIRA to for automatically assignment.
        // -2 will be used by us to say assign to me
        // If the user decides to make usernames -1 or -2 then this won't work so well... This problem exists with JIRA. Realistically no one would
        // need a username -1 or -2
        // Automatic needs to be first so it defaults
        fieldOpts.add(new FieldOption(i18n.getMessage("issue.assignee.automatic"), "-1"));
        fieldOpts.add(new FieldOption(i18n.getMessage("issue.assignee.tome"), "-2"));
        fieldOpts.add(new FieldOption(i18n.getMessage("issue.assignee.unassigned"), ""));
        return fieldOpts;
    }

    private String createProjectMetaKey(String projectKey) {
        return ApplicationConstants.METADATA_CACHE_KEY_PREFIX + projectKey;
    }
}
