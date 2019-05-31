package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.gdpr.GDPRUserService;
import com.thed.zephyr.capture.service.gdpr.MigrateService;
import com.thed.zephyr.capture.service.gdpr.model.UserDTO;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.UniqueIdGenerator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/rest/event/user")
public class UserWebhookController {

    @Autowired
    private Logger log;
    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private AtlassianHostRepository atlassianHostRepository;
    @Autowired
    private GDPRUserService gdprUserService;
    @Autowired
    private MigrateService migrateService;

    @RequestMapping(value = "/created", method = RequestMethod.POST)
    public ResponseEntity<?> userCreated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode createUserJson) {
        String tenantId = hostUser.getHost().getClientKey();
        log.info("Create JIRA user event for tenantId:{}", tenantId);
        try {
            AcHostModel acHostModel = (AcHostModel)atlassianHostRepository.findOne(hostUser.getHost().getClientKey());
            if(createUserJson != null && createUserJson.has("user")) {
                JsonNode userNode = createUserJson.get("user");
                List<UserDTO> userDTOS = new ArrayList<>();
                userDTOS.add(new UserDTO(userNode.get("key").asText(), userNode.get("name").asText(), userNode.get("accountId").asText()));
                gdprUserService.processToPushMigration(
                        userDTOS,
                        acHostModel.getClientKey(),
                        acHostModel.getCtId());
            }

        } catch (Exception exception) {
            log.error("Error during create JIRA user.", exception);
        }
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        processMigration(hostUser, acHostModel);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/updated", method = RequestMethod.POST)
    public ResponseEntity<?> userUpdated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode updateUserJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String ctid = acHostModel.getCtId();
        log.debug("Invoked userUpdated event");
        log.debug("JSON from webhook invoker : " + updateUserJson);
        try {
            JsonNode userNode = null != updateUserJson ? updateUserJson.get("user") : null;
            if (userNode == null) {
                log.warn("User Update event triggered with no Project details.");
                processMigration(hostUser, acHostModel);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            String username = userNode.get("name").asText();
            String userKey = userNode.get("key").asText();
            String displayName = userNode.get("displayName").asText();
            String userAccountId = userNode.get("accountId").asText();
            if (null != username) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + username);
            }
            if (null != userKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + userKey);
            }
            if(null != userAccountId) {
            	tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + userAccountId);
            }
            if(displayName != null) {
            	sessionService.updateUserDisplayNamesForSessions(ctid, userKey, userAccountId, displayName);
            }
        } catch (Exception e) {
            log.warn("Unable to handle the user updating webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the user updating webhook");
        }
        processMigration(hostUser, acHostModel);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/deleted", method = RequestMethod.POST)
    public ResponseEntity<?> userDeleted(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode deleteUserJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Invoked user Deleted event");
        log.debug("JSON from webhook invoker : " + deleteUserJson);
        try {
            JsonNode userNode = null != deleteUserJson ? deleteUserJson.get("user") : null;
            if (userNode == null) {
                log.warn("User Update event triggered with no Project details.");
                processMigration(hostUser, acHostModel);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            String username = userNode.get("name").asText();
            String userKey = userNode.get("key").asText();
            String userAccountId = userNode.get("accountId").asText();
            if (null != username) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + username);
            }
            if (null != userKey) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + userKey);
            }
            if (null != userAccountId) {
                tenantAwareCache.delete(acHostModel, ApplicationConstants.USER_CACHE_KEY_PREFIX + userAccountId);
            }
        } catch (Exception e) {
            log.warn("Unable to handle the user deleting webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the user deleting webhook");
        }
        processMigration(hostUser, acHostModel);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Trigger migration from issue webhook
     * @param hostUser
     * @param acHostModel
     */
    private void processMigration(AtlassianHostUser hostUser, AcHostModel acHostModel) {
        try {
             if(acHostModel != null && !CaptureUtil.isTenantGDPRComplaint()) {
               String jobProgressId = new UniqueIdGenerator().getStringId();
               migrateService.migrateData(hostUser, acHostModel, jobProgressId);
            }
        } catch (Exception exception) {
            log.error("Error during process user migration {}", exception.getMessage());
        }
    }


}
