package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.CustomField;
import com.thed.zephyr.capture.model.jira.FieldOption;
import com.thed.zephyr.capture.service.jira.IssueTypeService;
import com.thed.zephyr.capture.service.jira.MetadataService;
import com.thed.zephyr.capture.service.jira.UserService;
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

import static com.thed.zephyr.capture.util.ApplicationConstants.*;

/**
 * Created by Masud on 8/22/17.
 */
@Service
public class MetadataServiceImpl implements MetadataService {

    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Autowired
    private IssueTypeService issueTypeService;

    @Autowired
    private UserService userService;

    @Override
    public Map<String, Object> createFieldScreenRenderer(CaptureProject captureProject) {
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

        List<IssueType> issueTypes = issueTypeService.getIssueTypesByProject(captureProject.getId());
        List<Long> issueTypeIds = new ArrayList<>();
        issueTypes.forEach(issueType -> {
            issueTypeIds.add(issueType.getId());
        });
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl();
        URI targetUrl= UriComponentsBuilder.fromUriString(uri)
                .path(JiraConstants.REST_API_CREATE_ISSUE_SCHEMA)
                .queryParam("projectIds", captureProject.getId())
                .queryParam("issuetypeIds", issueTypeIds.toArray())
                .queryParam("expand","projects.issuetypes.fields")
                .build()
                .encode()
                .toUri();

        try {
            String response = restTemplate.getForObject(targetUrl, String.class);
            JsonNode jsonNode = new ObjectMapper().readTree(response).get(PROJECTS);
            for(final JsonNode pN: jsonNode){
                JsonNode itNode = pN.get(ISSUE_TYPES);
                for(final JsonNode iN: itNode){
                    Map<String, Object> iFMap = new HashMap<>();

                    com.thed.zephyr.capture.model.jira
                            .IssueType issueType = new ObjectMapper()
                            .readValue(iN.toString(),com.thed.zephyr.capture.model.jira.
                                    IssueType.class);
                    JsonNode fN = iN.get(FIELDS);
                    iFMap.put(FIELDS, fN);
                    iFMap.put(ISSUE_TYPE,issueType);

                    for(final JsonNode vN: fN)
                    {
                        JsonNode options = vN.has(ALLOWED_VALUES) ? vN.get(ALLOWED_VALUES):
                                new ObjectMapper().createArrayNode();
                        List<FieldOption> fieldOptions = new ArrayList<>();
                        options.forEach(jsonNode1 -> {
                            String name = jsonNode1.has("name") ? jsonNode1.get("name").asText():
                                    jsonNode1.get("value").asText();
                            String value = jsonNode1.has("id") ? jsonNode1.get("id").asText(): null;
                            if(StringUtils.isNotEmpty(name)
                                    && StringUtils.isNotEmpty(value)) {
                                fieldOptions.add(new FieldOption(name,value));
                            }
                        });

                        String key = vN.get(KEY).asText();
                        ObjectNode objectNode = (ObjectNode)vN;
                        if(!key.equalsIgnoreCase("issuelinks")){
                            objectNode.set(OPTIONS, new ObjectMapper().valueToTree(fieldOptions));
                        }else{
                            if(issuelinksTypesList.size()>0){
                                objectNode.set(OPTIONS, new ObjectMapper().convertValue(issuelinksTypesList,JsonNode.class));
                            }else {
                                objectNode.set(OPTIONS, new ObjectMapper().valueToTree(fieldOptions));
                            }
                        }
                        CustomField customField = new ObjectMapper()
                                .readValue(vN.toString(), CustomField.class);
                        String typeKey = customField.getSchema() != null &&
                                customField.getSchema().getCustom() != null ?
                                customField.getSchema().getCustom(): customField.getSchema().getType();
                        objectNode.put("typeKey",typeKey);
                        boolean systemField = false;
                        if(customField.getSchema() != null
                           && customField.getSchema().getSystem() != null
                           && customField.getSchema().getSystem().equals(customField.getKey())){
                            systemField = true;
                        }
                        objectNode.put("systemField",systemField);
                        objectNode.remove(ALLOWED_VALUES);
                        fieldValueMap.put(key, vN);
                    }

                    fieldMap.add(iFMap);
                }
            }

            JsonNode userNode = userService.getAssignableUserByProjectKey(captureProject.getKey());
            List<FieldOption> userBeans = new ArrayList<>();
            userNode.forEach(jsonNode1 -> {
//                ObjectNode objectNode = new ObjectMapper().createObjectNode();
//                objectNode.put("text",jsonNode1.get("displayName").asText());
//                objectNode.put("value",jsonNode1.get("key").asText());
//                objectNode.set("children",new ObjectMapper().createArrayNode());
//                objectNode.put("hasChildren",false);

                userBeans.add(new FieldOption(
                        jsonNode1.get("displayName").asText(),
                        jsonNode1.get("key").asText()
                ));

            });
            resultMap.put(FIELD_LIST_BEANS,fieldMap);
            resultMap.put(FIELD_DETAILS,fieldValueMap);
            resultMap.put(USER_BEANS, userBeans);
            return resultMap;
        } catch (Exception exception) {
            log.error("Error during getting issue from jira.", exception);
         }

         return resultMap;
    }


}
