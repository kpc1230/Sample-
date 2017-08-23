package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.jira.IssueType;
import com.thed.zephyr.capture.service.jira.IssueTypeService;
import com.thed.zephyr.capture.util.JiraConstants;
 import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Masud on 8/18/17.
 */
@Service
public class IssueTypeServiceImpl implements IssueTypeService {

    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;


    @Override
    public List<IssueType> getIssueTypes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

        String uri = host.getHost().getBaseUrl()+ JiraConstants.REST_API_ISSUE_TYPE;

        try {
            IssueType[] response = restTemplate.getForObject(uri, IssueType[].class);
            return Arrays.asList(response);
        } catch (RestClientException exception) {
            log.error("Error during getting issue types from jira.", exception);
            throw exception;
        }
    }

    @Override
    public IssueType getIssueType(Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

        String uri = host.getHost().getBaseUrl()+ JiraConstants.REST_API_ISSUE_TYPE+"/"+id;

        try {
            IssueType response = restTemplate.getForObject(uri, IssueType.class);
            return response;
        } catch (RestClientException exception) {
            log.error("Error during getting issue type from jira.", exception);
            throw exception;
        }
    }

    @Override
    public List<IssueType> getIssueTypesByProject(Long projectId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        List<IssueType> issueTypes = new ArrayList<>();
        String uri = host.getHost().getBaseUrl()+ JiraConstants.REST_API_PROJECT+"/"+projectId+"?expand=issueTypes";

        try {
            String response = restTemplate.getForObject(uri, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode issueTypesNode = mapper.readTree(response).get("issueTypes");
            if(issueTypesNode.isArray()){
                IssueType[] issueTypesArr = new ObjectMapper().readValue(issueTypesNode.toString(),IssueType[].class);
                issueTypes = Arrays.asList(issueTypesArr);
            }
            return issueTypes;
        } catch (Exception exception) {
            log.error("Error during getting issue types by project from jira.", exception);
         }
         return issueTypes;
    }
}
