package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rest/event/issue")
public class IssueWebhookController {

    @Autowired
    private Logger log;

    @Autowired
    SessionService sessionService;

    @RequestMapping(value = "/created", method = RequestMethod.POST)
    public ResponseEntity issueCreated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode createIssueJson) {
        AcHostModel acHostModel = (AcHostModel)hostUser.getHost();
        String ctid = acHostModel.getCtId();
        log.debug("Invoked IssueCreate event");
        log.debug("JSON from webhook invoker : " + createIssueJson);
        try {
            if (null != createIssueJson && createIssueJson.has("issue")) {
                JsonNode issueNode = createIssueJson.get("issue");
                Long issueId = issueNode.get("id").asLong();
                Long projectId = issueNode.get("fields").get("project").get("id").asLong();
                String issueCreatedBy = issueNode.get("fields").get("creator").get("key").asText();
                if (issueId != null) {
                    sessionService.updateSessionWithIssue(ctid, projectId, issueCreatedBy, issueId);
                } else {
                    log.error("Issue creation details are empty from JIRA");
                    throw new CaptureValidationException("Issue ID is null");
                }

            } else {
                log.error("Issue creation details are empty from JIRA");
                throw new CaptureRuntimeException("Issue creation details are empty from JIRA");
            }

        } catch (Exception e) {
            log.warn("Unable to handle the issue creation webhook: ", e);
            throw new CaptureRuntimeException("Unable to handle the issue creation webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
