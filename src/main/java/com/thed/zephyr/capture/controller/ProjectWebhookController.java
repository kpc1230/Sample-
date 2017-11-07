package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.util.SessionSearchList;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.data.TemplateService;
import com.thed.zephyr.capture.util.ApplicationConstants;

import java.util.Objects;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rest/event/project")
public class ProjectWebhookController {

    @Autowired
    private Logger log;
    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private TemplateService templateService;


    @RequestMapping(value = "/created", method = RequestMethod.POST)
    public ResponseEntity<?> projectCreated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode createProjectJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Invoked projectCreated event");
        log.debug("JSON from webhook invoker : " + createProjectJson);
        try {

        } catch (Exception e) {
            log.warn("Unable to handle the project creation webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the project creation webhook");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @RequestMapping(value = "/updated", method = RequestMethod.POST)
    public ResponseEntity<?> projectUpdated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode updateProjectJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String ctid = acHostModel.getCtId();
        log.debug("Invoked projectUpdated event");
        log.debug("JSON from webhook invoker : " + updateProjectJson);
        try {
            JsonNode projectNode = null != updateProjectJson ? updateProjectJson.get("project") : null;
            if (projectNode == null) {
                log.warn("Project Update event triggered with no Project details.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Long projectId = projectNode.get("id").asLong();
            String projectKey = projectNode.get("key").asText();
            String projectName = projectNode.get("name").asText();
            if (null != projectId) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + String.valueOf(projectId));
            }
            if (null != projectKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + projectKey);
            }
            if(Objects.nonNull(projectId)) {
            	sessionService.updateProjectNameForSessions(ctid, projectId, projectName);
            }
        } catch (Exception e) {
            log.warn("Unable to handle the project updating webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the project updating webhook");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @RequestMapping(value = "/deleted", method = RequestMethod.POST)
    public ResponseEntity<?> projectDeleted(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode deleteProjectJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Invoked projectDeleted event");
        log.debug("JSON from webhook invoker : " + deleteProjectJson);
        try {
            JsonNode projectNode = null != deleteProjectJson ? deleteProjectJson.get("project") : null;
            if (projectNode == null) {
                log.warn("Project Delete event triggered with no Project details.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Long projectId = projectNode.get("id").asLong();
            String projectKey = projectNode.get("key").asText();
            if (null != projectId) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + String.valueOf(projectId));
                int index = 0, maxLimit = 10;
                long totalCount = deleteSessionsByBatch(projectId, index, maxLimit);
                int loopCount = ((int) totalCount / maxLimit);
                while(loopCount-- > 0) {
                	deleteSessionsByBatch(projectId, index, maxLimit);
                }
                String ctId = acHostModel.getCtId();
                templateService.deleteTemplatesByCtIdAndProject(ctId, projectId);
            }
            if (null != projectKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + projectKey);
            }

        } catch (Exception e) {
            log.warn("Unable to handle the project deleting webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the project deleting webhook");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    private long deleteSessionsByBatch(Long projectId, int index, int maxLimit) throws CaptureValidationException {
    	  SessionSearchList sessionSearchList = sessionService.getSessionsForProject(projectId, index, maxLimit);
          for(Session session : sessionSearchList.getContent()) {
          	sessionService.deleteSession(session.getId());
          }
          return sessionSearchList.getTotal();
    }


}
