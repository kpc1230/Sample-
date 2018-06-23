package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.ChangelogItem;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.json.AttachmentJsonParser;
import com.atlassian.jira.rest.client.internal.json.ChangelogItemJsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.BasicIssue;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.predicates.ActiveParticipantPredicate;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.data.impl.SessionServiceImpl;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.service.jira.IssueWebHookHandler;
import com.thed.zephyr.capture.service.jira.WebhookHandlerService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/rest/event/issue")
public class IssueWebhookController {

    private Logger log;
    private SessionService sessionService;
    private SessionActivityService sessionActivityService;
    private CaptureI18NMessageSource i18n;
    private PermissionService permissionService;
    private IssueService issueService;
    private ITenantAwareCache tenantAwareCache;
    private IssueWebHookHandler issueWebHookHandler;
    private SessionESRepository sessionESRepository;
    private WebhookHandlerService webhookHandlerService;

    public IssueWebhookController(@Autowired Logger log,
                                  @Autowired SessionService sessionService,
                                  @Autowired SessionActivityService sessionActivityService,
                                  @Autowired CaptureI18NMessageSource i18n,
                                  @Autowired PermissionService permissionService,
                                  @Autowired IssueService issueService,
                                  @Autowired ITenantAwareCache tenantAwareCache,
                                  @Autowired IssueWebHookHandler issueWebHookHandler,
                                  @Autowired SessionESRepository sessionESRepository,
                                  @Autowired WebhookHandlerService webhookHandlerService) {
        this.log = log;
        this.sessionService = sessionService;
        this.sessionActivityService = sessionActivityService;
        this.i18n = i18n;
        this.permissionService = permissionService;
        this.issueService = issueService;
        this.tenantAwareCache = tenantAwareCache;
        this.issueWebHookHandler = issueWebHookHandler;
        this.sessionESRepository = sessionESRepository;
        this.webhookHandlerService = webhookHandlerService;
    }

    @RequestMapping(value = "/created", method = RequestMethod.POST)
    public ResponseEntity issueCreated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode issueJson){
        log.debug("Issue created event, issueJson:{}", issueJson.toString());
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        if(!issueJson.has("user") || !issueJson.has("issue") || !issueJson.get("issue").has("fields") || !issueJson.get("issue").get("fields").has("project")){
            log.error("Incorrect issue json from JIRA webhook issueJson:{}", issueJson.toString());
            return ResponseEntity.ok().build();
        }
        ObjectMapper om = new ObjectMapper();
        try {
            BasicIssue basicIssue = om.readValue(issueJson.get("issue").toString(), BasicIssue.class);
            CaptureProject captureProject = om.readValue(issueJson.get("issue").get("fields").get("project").toString(), CaptureProject.class);
            basicIssue.setProject(captureProject);
            CaptureUser user = om.readValue(issueJson.get("user").toString(), CaptureUser.class);
            webhookHandlerService.issueCreateEventHandler(acHostModel, basicIssue, user);
        } catch (IOException exception) {
            log.error("Error during read issue json issueJson:{}", issueJson.toString(), exception);
        }

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/updated", method = RequestMethod.POST)
    public ResponseEntity issueUpdated(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode updatedIssueJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Invoked IssueUpdated event");
        try {
            if (null != updatedIssueJson && updatedIssueJson.has("issue")) {
                JsonNode issueNode = updatedIssueJson.get("issue");
                SessionServiceImpl.SessionResult sessionResult = sessionService.getActiveSession(hostUser.getUserKey().get(), null);
                if (null != issueNode && null != issueNode.get("fields")) {
                    try {
                        //Get ChangeLogItems
                        log.debug("Fetching change log items from request body.");
                        if (updatedIssueJson.has("changelog")) {
                            JsonNode changeLogJson = updatedIssueJson.get("changelog");
                            List<JsonNode> changeLogItems = changeLogJson.findValues("items");
                            if (changeLogItems != null && changeLogItems.size() > 0 && changeLogItems.get(0).isArray()) {
                                ChangelogItemJsonParser changelogItemJsonParser = new ChangelogItemJsonParser();
                                for (final JsonNode changeLogItemJson : changeLogItems) {
                                    Iterator<JsonNode> jsonNodeIterator = changeLogItemJson.iterator();
                                    while (jsonNodeIterator.hasNext()) {
                                        JsonNode jsonNode = jsonNodeIterator.next();
                                        ChangelogItem changelogItem = changelogItemJsonParser.parse(new JSONObject(jsonNode.toString()));
                                        if (StringUtils.equalsIgnoreCase("Attachment", changelogItem.getField())) {
                                            AttachmentJsonParser attachmentJsonParser = new AttachmentJsonParser();
                                            if (changelogItem.getFrom() != null && changelogItem.getTo() == null) {
                                                //sessionActivityService.removeRaisedIssue()
                                                //delete
                                            } else if (changelogItem.getFrom() == null && changelogItem.getTo() != null) {
                                                Iterator<JsonNode> jsonNodeAttachmentIterator = issueNode.get("fields").get("attachment").iterator();
                                                while (jsonNodeAttachmentIterator.hasNext()) {
                                                    JsonNode attachmentNode = jsonNodeAttachmentIterator.next();
                                                    if (attachmentNode.get("id").asInt() == Integer.valueOf(changelogItem.getTo())) {
                                                        String userKey =attachmentNode.get("author").get("key").asText();
                                                        Attachment jiraAttachment = attachmentJsonParser.parse(new JSONObject(attachmentNode.toString()));
                                                        if (jiraAttachment != null) {
                                                            try {
                                                                com.thed.zephyr.capture.model.jira.Attachment attachment = new
                                                                        com.thed.zephyr.capture.model.jira.Attachment(jiraAttachment.getSelf(), jiraAttachment.getFilename(),
                                                                        userKey, jiraAttachment.getCreationDate().getMillis(),
                                                                        jiraAttachment.getSize(), jiraAttachment.getMimeType(),
                                                                        jiraAttachment.getContentUri());
                                                                Issue issue = issueService.getIssueObject(issueNode.get("key").asText());
                                                                SessionActivity sessionActivity = sessionActivityService.addAttachment(sessionResult.getSession(), issue, attachment, new Date(jiraAttachment.getCreationDate().getMillis()), attachment.getAuthor());
                                                                List<ErrorCollection.ErrorItem> errorItems = validateUpdate(hostUser.getUserKey().get(), sessionService.getSession(sessionActivity.getSessionId()));
                                                                errorItems.stream().forEach(errorItem -> {
                                                                    log.error("Error:" + errorItem.getMessage());
                                                                });
                                                            } catch (Exception e) {
                                                                // don't do anything in this case
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                        //Delete the cache
                        Long issueId = issueNode.get("id").asLong();
                        String issueKey = issueNode.get("id").asText();
                        if (null != issueNode && null != issueId) {
                            tenantAwareCache.delete(acHostModel, ApplicationConstants.ISSUE_CACHE_KEY_PREFIX + String.valueOf(issueId));
                        }
                        if (null != issueNode && null != issueKey) {
                            tenantAwareCache.delete(acHostModel, ApplicationConstants.ISSUE_CACHE_KEY_PREFIX + issueKey);
                        }

                    } catch (Exception e) {
                        log.error("Error handling issue update", e);
                    }
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

    @RequestMapping(value = "/deleted", method = RequestMethod.POST)
    public ResponseEntity issueDeleted(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestBody JsonNode deletedIssueJson) {
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        log.debug("Invoked IssueDelete event");
        log.debug("JSON from web hook invoker : " + deletedIssueJson);
        try {
            if (null != deletedIssueJson && deletedIssueJson.has("issue")) {
                JsonNode issueNode = deletedIssueJson.get("issue");
                Long issueId = issueNode.get("id").asLong();
                if (null != issueId) {
                    issueWebHookHandler.issueDeleteEventHandler(acHostModel, issueId);
                    tenantAwareCache.delete(acHostModel, ApplicationConstants.ISSUE_CACHE_KEY_PREFIX + String.valueOf(issueId));
                }

            } else {
                log.error("Issue deletion details are empty from JIRA");
                throw new CaptureRuntimeException("Issue deletion details are empty from JIRA");
            }

        } catch (Exception e) {
            log.warn("Unable to handle the issue delete web hook: ", e);
            throw new CaptureRuntimeException("Unable to handle the issue delete webhook");
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    private List<ErrorCollection.ErrorItem> validateUpdate(String updater, Session newSession) {
        ErrorCollection errorCollection = new ErrorCollection();
        Session loadedSession = null;
        // Validation
        // Check inputs not null
        if (updater == null || newSession == null) {
            errorCollection.addError(i18n.getMessage("session.null.fields"));
        } else {
            // Check that the name is not empty
            if (newSession.getName().trim().isEmpty()) {
                errorCollection.addError(i18n.getMessage("session.name.empty"));
            }

            if (newSession.getName().length() > CaptureConstants.SESSION_NAME_LENGTH_LIMIT) {
                errorCollection.addError(i18n.getMessage("session.name.exceed.limit", new Integer[]{newSession.getName().length(),
                        CaptureConstants.SESSION_NAME_LENGTH_LIMIT}));
            }

            if (newSession.getAdditionalInfo() != null && newSession.getAdditionalInfo().length() > CaptureConstants.ADDITIONAL_INFO_LENGTH_LIMIT) {
                errorCollection.addError(i18n.getMessage("session.additionalInfo.exceed.limit", new Integer[]{newSession.getAdditionalInfo().length(),
                        CaptureConstants.ADDITIONAL_INFO_LENGTH_LIMIT}));
            }
            if (newSession.getRelatedIssueIds().size() > CaptureConstants.RELATED_ISSUES_LIMIT) {
                errorCollection.addError(i18n.getMessage("session.relatedissues.exceed", new Integer[]{newSession.getRelatedIssueIds().size(),
                        CaptureConstants.RELATED_ISSUES_LIMIT}));
            }
            // ANYTHING PAST THIS POINT IS A SANITY CHECK
            // Load in the session to check that it still exists
            loadedSession = sessionService.getSession(newSession.getId());
            if (loadedSession == null) {
                errorCollection.addError(i18n.getMessage("session.invalid", new String[]{newSession.getId()}));
            } else {
                // If the session status is changed, we better have been allowed to do that!
                if (!newSession.getStatus().equals(loadedSession.getStatus())
                        && !permissionService.canEditSessionStatus(updater, loadedSession)) {
                    errorCollection.addError(i18n.getMessage("session.status.change.permissions.violation"));
                }
                // If the assignee has changed, then the new session should be paused
                if (!newSession.getAssignee().equals(loadedSession.getAssignee()) && newSession.getStatus().equals(Session.Status.STARTED)) {
                    errorCollection.addError(i18n.getMessage("session.assigning.active.session.violation"));
                }
                // Status can't go backwards from COMPLETED
                if (loadedSession.getStatus().equals(Session.Status.COMPLETED) && !newSession.getStatus().equals(Session.Status.COMPLETED)) {
                    errorCollection.addError(i18n.getMessage("session.reopen.completed.violation"));
                }
                // Check that certain fields haven't changed - creator + time created (paranoid check)
                if (!newSession.getCreator().equals(loadedSession.getCreator())) {
                    errorCollection.addError(i18n.getMessage("session.change.creator.violation"));
                }
                if (!loadedSession.getTimeCreated().equals(newSession.getTimeCreated())) {
                    errorCollection.addError(i18n.getMessage("session.change.timecreated.violation"));
                }
            }
            // If we just completed the session, we want to update the time finished
            if (!newSession.getStatus().equals(loadedSession.getStatus()) && newSession.getStatus().equals(Session.Status.COMPLETED)) {
                if (newSession.getTimeFinished() == null) {
                    newSession.setTimeFinished(new Date());
                } else {
                    errorCollection.addError(i18n.getMessage("session.change.timefinished.violation"));
                }
            }
        }
        if (errorCollection.hasErrors()) {
            return errorCollection.getErrors();
        }

        // At this point, all is good
        // If we aren't shared, we wanna kick out all the current users
        if (!newSession.isShared()) {
            for (Participant p : Iterables.filter(newSession.getParticipants(), new ActiveParticipantPredicate())) {
                sessionActivityService.addParticipantLeft(newSession, new Date(), p.getUser());
            }
        }
        return errorCollection.getErrors();
    }
}
