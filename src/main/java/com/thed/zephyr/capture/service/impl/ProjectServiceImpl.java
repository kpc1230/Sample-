package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.thed.zephyr.capture.model.jira.Project;
import com.thed.zephyr.capture.service.ProjectService;
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
 * Created by Masud on 8/13/17.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;

    @Override
    public Project getProjectObj(Long projectId) {
        return getProjectObjByKey(String.valueOf(projectId));
    }

    @Override
    public Project getProjectObjByKey(String projectKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

        String uri = host.getHost().getBaseUrl()+JiraConstants.REST_API_PROJECT+"/"+projectKey;

        try {
            Project response = restTemplate.getForObject(uri, Project.class);
            return response;
        } catch (RestClientException exception) {
            log.error("Error during getting project from jira.", exception);
            throw exception;
        }
    }

    @Override
    public List<Project> getProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

        String uri = host.getHost().getBaseUrl()+JiraConstants.REST_API_PROJECT;
        List<Project> projects = new ArrayList<>();
        try {
            Project[] listProject = restTemplate.getForObject(uri, Project[].class);
            return Arrays.asList(listProject);
        } catch (Exception exception) {
            log.error("Error during getting list project from jira.", exception);
         }
        return null;
    }

}
