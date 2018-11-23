package com.thed.zephyr.capture.service.jira;

import com.atlassian.jira.rest.client.internal.json.AttachmentJsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.SessionActivity;
import com.thed.zephyr.capture.model.jira.Attachment;
import com.thed.zephyr.capture.model.jira.BasicIssue;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.model.util.IssueChangeLog;
import com.thed.zephyr.capture.model.util.IssueChangeLogItem;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class WebhookHandlerService {

    private Logger log;
    private SessionService sessionService;
    private SessionESRepository sessionESRepository;
    private SessionActivityService sessionActivityService;

    public WebhookHandlerService(@Autowired Logger log,
                                 @Autowired SessionService sessionService,
                                 @Autowired SessionESRepository sessionESRepository,
                                 @Autowired SessionActivityService sessionActivityService) {
        this.log = log;
        this.sessionService = sessionService;
        this.sessionESRepository = sessionESRepository;
        this.sessionActivityService = sessionActivityService;
    }

    public void issueCreateEventHandler(AcHostModel acHostModel, BasicIssue basicIssue, CaptureUser user){
        log.trace("Triggered issueCreateEventHandler...");
        try {
            String recipientSessionId = sessionService.getActiveSession(acHostModel, user).getSessionId();
            if(StringUtils.isEmpty(recipientSessionId)){
                log.debug("User doesn't have any active sessions and doesn't participate in any.");
                return;
            }
            sessionService.addRaisedIssueToSession(acHostModel, recipientSessionId, basicIssue, user);
        } catch (Exception exception) {
            log.error("Error during performing issue create event.", exception);
        }
    }

    public void issueUpdatedEventHandler(AcHostModel acHostModel, BasicIssue basicIssue, IssueChangeLog issueChangeLog, CaptureUser user,JsonNode updatedIssueJson) throws JSONException {
        log.trace("Issue updated issueChangeLog:{}", issueChangeLog.toString());
        String recipientSessionId = sessionService.getActiveSession(acHostModel, user).getSessionId();
        if(StringUtils.isEmpty(recipientSessionId)){
            log.debug("User doesn't have any active sessions and doesn't participate in any.");
            return;
        }
        for (IssueChangeLogItem issueChangeLogItem:issueChangeLog.getItems()){
            switch (issueChangeLogItem.getFieldId()){
                case "attachment":
                    attachmentFieldUpdated(acHostModel, issueChangeLogItem, basicIssue, user, updatedIssueJson, recipientSessionId);
                    break;
            }
        }
    }

    private void attachmentFieldUpdated(AcHostModel acHostModel, IssueChangeLogItem issueChangeLogItem, BasicIssue basicIssue, CaptureUser user, JsonNode updatedIssueJson, String sessionId) throws JSONException {
        if(StringUtils.isEmpty(issueChangeLogItem.getTo())){
            //Attachment deleted from Issue
            sessionActivityService.removeAttachment(sessionId, issueChangeLogItem.getFrom(), basicIssue.getId());
        } else{
            //Attachment added into Issue
            Attachment attachment = getAddedAttachmentFromUpdatedIssueJson(issueChangeLogItem.getTo(), updatedIssueJson);
            attachment.setAuthorAccountId(user.getAccountId());
            Session session = sessionESRepository.findById(sessionId);
            sessionActivityService.addAttachment(session, basicIssue.getId(), attachment, new Date(attachment.getCreationDate()), attachment.getAuthor(), attachment.getAuthorAccountId());
        }
    }

    private Attachment getAddedAttachmentFromUpdatedIssueJson(String attachmentId, JsonNode updatedIssueJson) throws JSONException {
        ArrayNode attachmentNodes =  (ArrayNode)updatedIssueJson.get("issue").get("fields").get("attachment");
        for (JsonNode attachmentJson:attachmentNodes){
            if (StringUtils.equals(attachmentJson.get("id").asText(), attachmentId)){
                AttachmentJsonParser attachmentJsonParser = new AttachmentJsonParser();
                return new Attachment(attachmentJsonParser.parse(new JSONObject(attachmentJson.toString())));
            }
        }

        return null;
    }
}
