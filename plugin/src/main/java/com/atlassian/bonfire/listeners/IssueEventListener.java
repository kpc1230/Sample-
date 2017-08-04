package com.atlassian.bonfire.listeners;

import com.atlassian.bonfire.customfield.BonfireMultiSessionCustomFieldService;
import com.atlassian.bonfire.customfield.BonfireSessionCustomFieldService;
import com.atlassian.bonfire.events.IssueRaisedInSessionEvent;
import com.atlassian.bonfire.service.BonfireServiceSupport;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionControllerImpl;
import com.atlassian.jira.database.QueryDslAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.model.querydsl.ChangeItemDTO;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.List;

import static com.atlassian.jira.model.querydsl.QChangeItem.CHANGE_ITEM;

/**
 * Listens for the Issue Created event, and adds it to the active test session for the user.
 *
 * @since v4.4
 */
public class IssueEventListener extends BonfireServiceSupport {
    @JIRAResource
    private EventPublisher eventPublisher;

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = SessionControllerImpl.SERVICE)
    private SessionController sessionController;

    @JIRAResource
    private AttachmentManager jiraAttachmentManager;

    @JIRAResource
    private ThumbnailManager jiraThumbnailManager;

    @JIRAResource
    private QueryDslAccessor queryDslAccessor;

    private final Logger log = Logger.getLogger(this.getClass());

    public IssueEventListener() {
    }

    @Override
    protected void onPluginStart() {
    }

    @Override
    protected void onPluginStop() {
    }

    @Override
    protected void onClearCache() {
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {
        Long eventTypeId = issueEvent.getEventTypeId();
        if (EventType.ISSUE_CREATED_ID.equals(eventTypeId)) {
            clearBonfireFields(issueEvent);
        }
        final ApplicationUser user = issueEvent.getUser();
        if (user == null) {
            return;
        }
        SessionController.SessionResult sessionResult = sessionController.getActiveSession(user);
        if (!sessionResult.isValid()) {
            return;
        }

        Session session = sessionResult.getSession();
        if (EventType.ISSUE_CREATED_ID.equals(eventTypeId)) {
            issueCreatedInSession(session, issueEvent);
        } else if (EventType.ISSUE_UPDATED_ID.equals(eventTypeId)) {
            issueUpdatedInSession(session, issueEvent);
        }
    }

    private void issueCreatedInSession(Session session, IssueEvent issueEvent) {

        SessionController.UpdateResult updateResult = sessionController.validateAddRaisedIssue(session, issueEvent);

        if (!updateResult.isValid()) {
            log.error(String.format("Unable to add issue raised to session. Validation errors: %s", updateResult.getErrorCollection().toString()));
        } else {
            sessionController.update(updateResult);
            eventPublisher.publish(new IssueRaisedInSessionEvent(updateResult.getSession(), issueEvent.getIssue(), issueEvent.getUser()));
        }
    }

    private void issueUpdatedInSession(Session session, IssueEvent issueEvent) {
        @SuppressWarnings({"unchecked"}) List<ChangeItemDTO> changeItems = getIssueChangeItems(issueEvent);
        for (ChangeItemDTO changeItem : changeItems) {
            if ("Attachment".equals(changeItem.getField())) {
                Long attachmentId = getAttachmentId(changeItem);
                if (attachmentId != null) {
                    try {
                        Attachment attachment = jiraAttachmentManager.getAttachment(attachmentId);
                        Thumbnail thumbnail = jiraThumbnailManager.getThumbnail(attachment);

                        SessionController.UpdateResult updateResult = sessionController.validateAddAttachment(session,
                                issueEvent, attachment, thumbnail);

                        if (!updateResult.isValid()) {
                            log.error(String.format("Unable to add attachment to session. Validation errors: %s", updateResult.getErrorCollection().toString()));
                        } else {
                            sessionController.update(updateResult);
                        }
                    } catch (AttachmentNotFoundException youAreFcKenJokingJira) {
                        // don't do anything in this case
                    }
                }
            }
        }
    }

    private Long getAttachmentId(ChangeItemDTO changeItem) {
        // the change item new value can be null if we are deleting an attachment
        String newValue = changeItem.getNewvalue();
        if (newValue != null) {
            try {
                return Long.valueOf(newValue);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private List<ChangeItemDTO> getIssueChangeItems(IssueEvent issueEvent) {
        return queryDslAccessor.executeQuery(con -> con.newSqlQuery()
                .select(CHANGE_ITEM)
                .from(CHANGE_ITEM)
                .where(CHANGE_ITEM.group.eq(issueEvent.getChangeLog().getLong("id")))
                .fetchResults()
                .getResults());
    }

    /**
     * This method fixes the fields that we never want an issue to have on creation. This mostly happens when an issue is cloned, the values for some
     * Dynamic Bonfire CFTs are also included, often incorrectly. This method fixes those values
     */
    private void clearBonfireFields(IssueEvent issueEvent) {
        Issue issue = issueEvent.getIssue();
        bonfireMultiSessionCustomFieldService.clearRelatedToValue(issue);
        bonfireSessionCustomFieldService.deleteRaisedInValue(issue);
    }
}
