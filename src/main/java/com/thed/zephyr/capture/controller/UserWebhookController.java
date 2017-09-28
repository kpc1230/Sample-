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
@RequestMapping("/rest/event/user")
public class UserWebhookController {

    @Autowired
    private Logger log;
    @Autowired
    private ITenantAwareCache tenantAwareCache;


    @RequestMapping(value = "/created", method = RequestMethod.POST)
    public ResponseEntity userCreated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode createProjectJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String ctid = acHostModel.getCtId();
        log.debug("Invoked projectCreated event");
        log.debug("JSON from webhook invoker : " + createProjectJson);
        try {
            // Right now not used
        } catch (Exception e) {
            log.warn("Unable to handle the project creation webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the project creation webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/updated", method = RequestMethod.POST)
    public ResponseEntity usertUpdated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode updateUserJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String ctid = acHostModel.getCtId();
        log.debug("Invoked userUpdated event");
        log.debug("JSON from webhook invoker : " + updateUserJson);
        try {
            JsonNode userNode = null != updateUserJson ? updateUserJson.get("user") : null;
            if (userNode == null) {
                log.warn("User Update event triggered with no Project details.");
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            String username = userNode.get("name").asText();
            String userKey = userNode.get("key").asText();
            if (null != username) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + username);
            }
            if (null != userKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + userKey);
            }
        } catch (Exception e) {
            log.warn("Unable to handle the user updating webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the user updating webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/deleted", method = RequestMethod.POST)
    public ResponseEntity userDeleted(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode deleteUserJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Invoked user Deleted event");
        log.debug("JSON from webhook invoker : " + deleteUserJson);
        try {
            JsonNode userNode = null != deleteUserJson ? deleteUserJson.get("user") : null;
            if (userNode == null) {
                log.warn("User Update event triggered with no Project details.");
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
            String username = userNode.get("name").asText();
            String userKey = userNode.get("key").asText();
            if (null != username) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + username);
            }
            if (null != userKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + userKey);
            }
        } catch (Exception e) {
            log.warn("Unable to handle the user deleting webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the user deleting webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


}
