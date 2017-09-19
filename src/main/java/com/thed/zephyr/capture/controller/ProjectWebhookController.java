package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.util.ApplicationConstants;
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


    @RequestMapping(value = "/created", method = RequestMethod.POST)
    public ResponseEntity projectCreated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode createProjectJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String ctid = acHostModel.getCtId();
        log.debug("Invoked projectCreated event");
        log.debug("JSON from webhook invoker : " + createProjectJson);
        try {

        } catch (Exception e) {
            log.warn("Unable to handle the project creation webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the project creation webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/updated", method = RequestMethod.POST)
    public ResponseEntity projectUpdated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode updateProjectJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String ctid = acHostModel.getCtId();
        log.debug("Invoked projectUpdated event");
        log.debug("JSON from webhook invoker : " + updateProjectJson);
        try {
            JsonNode projectNode = null != updateProjectJson ? updateProjectJson.get("project") : null;
            if (projectNode == null) {
                log.warn("Project Update event triggered with no Project details.");
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            Long projectId = projectNode.get("id").asLong();
            String projectKey = projectNode.get("key").asText();
            if (null != projectId) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + String.valueOf(projectId));
            }
            if (null != projectKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + projectKey);
            }
        } catch (Exception e) {
            log.warn("Unable to handle the project updating webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the project updating webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/deleted", method = RequestMethod.POST)
    public ResponseEntity projectDeleted(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode deleteProjectJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Invoked projectDeleted event");
        log.debug("JSON from webhook invoker : " + deleteProjectJson);
        try {
            JsonNode projectNode = null != deleteProjectJson ? deleteProjectJson.get("project") : null;
            if (projectNode == null) {
                log.warn("Project Delete event triggered with no Project details.");
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            Long projectId = projectNode.get("id").asLong();
            String projectKey = projectNode.get("key").asText();
            if (null != projectId) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + String.valueOf(projectId));
            }
            if (null != projectKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.PROJECT_CACHE_KEY_PREFIX + projectKey);
            }

        } catch (Exception e) {
            log.warn("Unable to handle the project deleting webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the project deleting webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


}
