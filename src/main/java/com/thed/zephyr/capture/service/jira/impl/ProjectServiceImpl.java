package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.JiraConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static com.thed.zephyr.capture.util.JiraConstants.REST_API_PROJECT;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private Logger log;
    @Autowired
    private JiraRestClient jiraRestClient;
    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private DynamicProperty dynamicProperty;
    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;

    @Override
    public Project getProjectObj(Long projectId) {
        return getProjectObjByKey(String.valueOf(projectId));
    }

    @Override
    public Project getProjectObjByKey(String projectKey) {
         return jiraRestClient.getProjectClient().getProject(projectKey).claim();
    }

    @Override
    public ArrayList<BasicProject> getProjects() throws Exception {
        try{
            AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
            JsonNode projectString = atlassianHostRestClients.authenticatedAs(hostUser)
                    .getForObject(REST_API_PROJECT, JsonNode.class);
            return parseBasicProjects(projectString);
        } catch (Exception exception){
            log.error("Error during getting projects from Jira.", exception);
            throw new Exception("Error during getting projects.");
        }
    }

    /**
     * Get serialized project
     * @param projectId
     * @return
     */
    @Override
    public CaptureProject getCaptureProject(Long projectId) {
       return getCaptureProject(String.valueOf(projectId));
    }

    @Override
    public CaptureProject getCaptureProject(String projectIdOrKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        CaptureProject captureProject = null;
        try {
            captureProject = tenantAwareCache.getOrElse(acHostModel, buildProjectCacheKey(projectIdOrKey), new Callable<CaptureProject>() {
                @Override
                public CaptureProject call() throws Exception {
                    Project project = getProjectObjByKey(projectIdOrKey);
                    log.debug("Getting project from Jira.");
                    return new CaptureProject(project.getSelf(),
                            project.getKey(), project.getId(),
                            project.getName());
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.PROJECT_CACHE_EXPIRATION_DYNAMIC_PROP,ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());

        } catch (Exception exp) {
            log.error("Exception while getting the project from JIRA." + exp.getMessage(), exp);
        }
        return captureProject;
    }

    private String buildProjectCacheKey(String projectIdOrKey){
        return ApplicationConstants.PROJECT_CACHE_KEY_PREFIX+projectIdOrKey;
    }
    
    @Override
    public CaptureProject getCaptureProjectViaAddon(AcHostModel acHostModel, String projectIdOrKey) {        
        CaptureProject captureProject = null;
        try {
            captureProject = tenantAwareCache.getOrElse(acHostModel, buildProjectCacheKey(projectIdOrKey), new Callable<CaptureProject>() {
                @Override
                public CaptureProject call() throws Exception {
                	String uri = acHostModel.getBaseUrl() + JiraConstants.REST_API_PROJECT_2 + projectIdOrKey;

                	 CaptureProject response = atlassianHostRestClients.authenticatedAsAddon().getForObject(uri, CaptureProject.class);
                    return response;
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.PROJECT_CACHE_EXPIRATION_DYNAMIC_PROP,ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());

        } catch (Exception exp) {
            log.error("Exception while getting the project from JIRA." + exp.getMessage(), exp);
        }
        return captureProject;
    }

    /**
    * Parse and make list of basic projects
    * @param projectString
    * @return
    */
    private ArrayList<BasicProject> parseBasicProjects(JsonNode projectString) {
        ArrayList<BasicProject> basicProjects = new ArrayList<>();
        projectString.forEach(jsonNode -> {
            BasicProject basicProject =
                    new BasicProject(
                            URI.create(jsonNode.get("self").asText()),
                            jsonNode.get("key").asText(),
                            jsonNode.get("id").asLong(),
                            jsonNode.get("name").asText()
                    );
            basicProjects.add(basicProject);
        });
        return basicProjects;
    }
}
