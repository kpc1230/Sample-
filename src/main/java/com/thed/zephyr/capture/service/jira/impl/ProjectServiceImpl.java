package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.api.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.JiraConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

import static com.thed.zephyr.capture.util.JiraConstants.REST_API_PROJECT;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class ProjectServiceImpl implements ProjectService {


    private Logger log;
    private JiraRestClient jiraRestClient;
    private ITenantAwareCache tenantAwareCache;
    private DynamicProperty dynamicProperty;
    private AtlassianHostRestClients atlassianHostRestClients;
    private LockService lockService;
    private SessionESRepository sessionESRepository;

    public ProjectServiceImpl(@Autowired Logger log,
                              @Autowired JiraRestClient jiraRestClient,
                              @Autowired ITenantAwareCache tenantAwareCache,
                              @Autowired DynamicProperty dynamicProperty,
                              @Autowired AtlassianHostRestClients atlassianHostRestClients,
                              @Autowired LockService lockService,
                              @Autowired SessionESRepository sessionESRepository) {
        this.log = log;
        this.jiraRestClient = jiraRestClient;
        this.tenantAwareCache = tenantAwareCache;
        this.dynamicProperty = dynamicProperty;
        this.atlassianHostRestClients = atlassianHostRestClients;
        this.lockService = lockService;
        this.sessionESRepository = sessionESRepository;
    }

    @Override
    public Project getProjectObj(Long projectId) {
        return getProjectObjByKey(String.valueOf(projectId));
    }

    @Override
    public Project getProjectObjByKey(String projectKey) {
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        RestTemplate restTemplate = atlassianHostRestClients.authenticatedAsAddon();
        if(hostUser != null){
            restTemplate = atlassianHostRestClients.authenticatedAs(hostUser);
        }
        ResponseEntity<JsonNode> jsonNodeRE = restTemplate.getForEntity(REST_API_PROJECT+"/"+projectKey, JsonNode.class);
        if(jsonNodeRE != null && jsonNodeRE.getStatusCodeValue()==200){
            JsonNode projectJson = jsonNodeRE.getBody();
            try {
                BasicUser lead = new BasicUser(null, null, null);
                Collection<Version> versions = Collections.EMPTY_LIST;
                Collection<BasicComponent> components = Collections.EMPTY_LIST;
                OptionalIterable<IssueType> issueTypes = OptionalIterable.absent();
                Collection<BasicProjectRole> projectRoles = Collections.EMPTY_LIST;
                Project project = new Project(null, null,
                        projectJson.get("key").asText(),
                        projectJson.get("id").asLong(),
                        projectJson.get("name").asText(),
                        projectJson.get("description").asText(),
                        lead, null, versions, components, issueTypes, projectRoles);
                return project;
            } catch (Exception e) {
                log.error("Error during getting project {} {}", projectKey, e.getMessage());
                return null;
            }
        }else{
            return null;
        }
    }

    @Override
    public ArrayList<BasicProject> getProjects() throws Exception {
        AtlassianHostUser hostUser = CaptureUtil.getAtlassianHostUser();
        String userAccountId = hostUser.getUserAccountId().isPresent()?hostUser.getUserAccountId().get():"undefined";
        Integer expiration = dynamicProperty.getIntProp("user.projects.cache.expiration.sec", 30).get();
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Getting projects by user:{} from cache", userAccountId);
        String lockKey = ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + userAccountId + "-" + acHostModel.getCtId();
        if(!lockService.tryLock(hostUser.getHost().getClientKey(), lockKey, 5)) {
            log.error("Not able to get the lock during getting Projects by user:{}", userAccountId);
            throw new CaptureRuntimeException("Not able to get the lock during getting Projects by user:{}" + userAccountId);
        }
        try{
            String projectString =  tenantAwareCache.getOrElse(acHostModel, createUserProjectsKey(userAccountId), new Callable<String>() {
                @Override
                public String call() throws Exception {
                    JsonNode jsonProjectsStr = atlassianHostRestClients.authenticatedAs(hostUser).getForObject(REST_API_PROJECT, JsonNode.class);
                    log.debug("Getting projects from Jira by user:{} jsonProjectsStr:{}", userAccountId, jsonProjectsStr.toString());

                    return jsonProjectsStr.toString();
                }
            }, expiration);
            ObjectMapper om = new ObjectMapper();
            ArrayList<BasicProject> result = parseBasicProjects(om.readTree(projectString));

            return result;
        } catch (Exception exception){
            log.error("Error during getting projects from Jira user:{}", userAccountId, exception);
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
                     log.info("Getting project from jira using url: {}",uri);
                	 CaptureProject response = atlassianHostRestClients.authenticatedAsAddon().getForObject(uri, CaptureProject.class);
                    return response;
                }
            }, dynamicProperty.getIntProp(ApplicationConstants.PROJECT_CACHE_EXPIRATION_DYNAMIC_PROP,ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION).get());

        } catch (Exception exp) {
            log.error("Exception while getting the project from JIRA." + exp.getMessage(), exp);
        }
        return captureProject;
    }

    @Override
    public String getProjectName(Long projectId, @Nullable String sessionId){
        if(StringUtils.isEmpty(sessionId)){
            return getProjectName(projectId);
        }
        Session session = sessionESRepository.findById(sessionId);
        if(session == null || StringUtils.isEmpty(session.getProjectName())){
            return getProjectName(projectId);
        }

        return session.getProjectName();
    }

    private String getProjectName(Long projectId){
        CaptureProject captureProject = getCaptureProject(projectId);

        return captureProject != null?captureProject.getName():null;
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

    private String createUserProjectsKey(String userAccountId){
    	return String.valueOf(ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + userAccountId);
    }
}
