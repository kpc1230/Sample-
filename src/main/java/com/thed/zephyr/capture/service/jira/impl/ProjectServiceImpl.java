package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.config.DynamicIntProperty;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.cache.LockService;
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
import java.util.Optional;
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
    @Autowired
    private LockService lockService;


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
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        String userKey = hostUser.getUserKey().isPresent()?hostUser.getUserKey().get():"undefined";
        Integer expiration = dynamicProperty.getIntProp("user.projects.cache.expiration.sec", 30).get();
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Getting projects by user:{} from cache", userKey);
        String lockKey = ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + userKey + "-" + acHostModel.getCtId();
        if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
            log.error("Not able to get the lock during getting Projects by user:{}", userKey);
            throw new CaptureRuntimeException("Not able to get the lock during getting Projects by user:{}" + userKey);
        }
        try{
            String projectString =  tenantAwareCache.getOrElse(acHostModel, createUserProjectsKey(userKey), new Callable<String>() {
                @Override
                public String call() throws Exception {
                    JsonNode jsonProjectsStr = atlassianHostRestClients.authenticatedAs(hostUser).getForObject(REST_API_PROJECT, JsonNode.class);
                    log.debug("Getting projects from Jira by user:{} jsonProjectsStr:{}", userKey, jsonProjectsStr.toString());

                    return jsonProjectsStr.toString();
                }
            }, expiration);
            ObjectMapper om = new ObjectMapper();

            return parseBasicProjects(om.readTree(projectString));
        } catch (Exception exception){
            log.error("Error during getting projects from Jira user:{}", hostUser.getUserKey().get(), exception);
            throw new Exception("Error during getting projects.");
        } finally {
            lockService.deleteLock(hostUser.getHost().getClientKey(), lockKey);
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
        return String.valueOf(ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + projectIdOrKey);
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
            Long projectId = jsonNode.get("id").asLong();
            BasicProject basicProject =
                    new BasicProject(
                            URI.create(jsonNode.get("self").asText()),
                            jsonNode.get("key").asText(),
                            projectId,
                            jsonNode.get("name").asText()
                    );
                basicProjects.add(basicProject);
        });
        log.debug("Parsed BasicProjects count:{} jsonProjectString:{}", basicProjects.size(), projectString.toString());

        return basicProjects;
    }

    private String createUserProjectsKey(String userKey){
        return String.valueOf(ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + userKey);
    }
}
