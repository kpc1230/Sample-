package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thed.zephyr.capture.service.jira.IssueTypeService;
import com.thed.zephyr.capture.service.jira.MetadataService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

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
    public Map<String, Object> createFieldScreenRenderer(Project project) {
        Map<String, Object> resultMap = new HashMap<>();

        List<Map<String, Object>> fieldMap = new ArrayList<>();
        Map<String, Object> fieldValueMap = new HashMap<>();


        List<IssueType> issueTypes = issueTypeService.getIssueTypesByProject(project.getId());
        List<Long> issueTypeIds = new ArrayList<>();
        issueTypes.forEach(issueType -> {
            issueTypeIds.add(issueType.getId());
        });
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String uri = host.getHost().getBaseUrl();
        URI targetUrl= UriComponentsBuilder.fromUriString(uri)
                .path(JiraConstants.REST_API_CREATE_ISSUE_SCHEMA)
                .queryParam("projectIds", project.getId())
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
                        JsonNode options = vN.get(ALLOWED_VALUES);
                        if(vN.has(ALLOWED_VALUES)
                                && options != null) {
                            String key = vN.get(KEY).asText();
                            ObjectNode objectNode = (ObjectNode)vN;
                            objectNode.set(OPTIONS, options);
                            objectNode.remove(ALLOWED_VALUES);
                            fieldValueMap.put(key, vN);
                        }
                    }

                    fieldMap.add(iFMap);
                }
            }
            resultMap.put(FIELD_LIST_BEANS,fieldMap);
            resultMap.put(FIELD_DETAILS,fieldValueMap);
            resultMap.put(USER_BEANS, userService.getAssignableUserByProjectKey(project.getKey()));
            return resultMap;
        } catch (Exception exception) {
            log.error("Error during getting issue from jira.", exception);
         }

         return resultMap;
    }


}
