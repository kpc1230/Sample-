package com.thed.zephyr.capture.service.dao;

import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.service.BonfireUnmarshalService;
import com.thed.zephyr.capture.service.BonfireUserService;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.excalibur.model.*;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.exception.AttachmentNotFoundException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.thumbnail.ThumbnailManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * This service marshals and unmarshals session stuff for storage. This should go away when we feel like writing mountains of upgrade tasks to move
 * sessions into a json representation. This file needs to go away soon.
 */
@Service(SessionMarshaller.SERVICE)
public class SessionMarshaller {
    public static final String SERVICE = "bonfire-sessionmarshaller";

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private static final SessionActivityItem NO_LONGER_VALID = new BaseSessionActivityItem(null, null, null);

    // Persistence - do not change the values of these or write an upgrade task
    private static final String SESSION_CREATOR = "owner";
    private static final String SESSION_ASSIGNEE = "assignee";
    private static final String SESSION_NAME = "name";
    private static final String SESSION_STARTED = "start";
    private static final String SESSION_ENDED = "end";
    private static final String SESSION_LOGGED_TIME = "duration";
    private static final String SESSION_STATUS = "status";
    private static final String SESSION_SHAREDNESS = "shared";
    private static final String SESSION_PARTICIPANTS = "participants";
    private static final String SESSION_RELATED_ISSUES = "relatedIssue";
    private static final String SESSION_RELATED_PROJECT = "relatedProject";
    private static final String SESSION_RAISED_ISSUES = "raisedIssues";
    private static final String SESSION_STATUS_HISTORY = "sessionStatusHistory";
    private static final String SESSION_NOTE_IDS = "sessionNoteIds";
    private static final String SESSION_ADDITIONAL_INFO = "additionalInfo";
    private static final String SESSION_ACTIVITY_ITEMS = "activityItems";
    private static final String SESSION_ACTIVITY_ITEM_CLASS = "class";
    private static final String SESSION_DEFAULT_TEMPLATE = "defaultTemplateId";

    // Services
    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService userService;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @Resource(name = BonfireUnmarshalService.SERVICE)
    private BonfireUnmarshalService bonfireUnmarshalService;

    @Resource
    private IssueManager jiraIssueManager;

    @Resource
    private ProjectManager jiraProjectManager;

    @JIRAResource
    private AttachmentManager jiraAttachmentManager;

    @JIRAResource
    private ThumbnailManager jiraThumbnailManager;

    private final Logger log = Logger.getLogger(this.getClass());

    // MARSHAL////////////////////////////////////////////////////////////////////////////////////////////////////
    public Map<String, Object> marshal(Session session) {
        Map<String, Object> values = new HashMap<String, Object>();
        // User creator;
        String creatorName = ApplicationUsers.getKeyFor(session.getCreator());
        values.put(SESSION_CREATOR, creatorName);
        // User assignee;
        String assigneeName = ApplicationUsers.getKeyFor(session.getAssignee());
        values.put(SESSION_ASSIGNEE, assigneeName);

        // String name;
        values.put(SESSION_NAME, session.getName());
        // Date started;

        // TODO Check if this will this work after migration to another server in another timezone?
        values.put(SESSION_STARTED, session.getTimeCreated().toString(ISODateTimeFormat.dateTime()));

        if (session.getTimeFinished() != null) {
            values.put(SESSION_ENDED, session.getTimeFinished().toString(ISODateTimeFormat.dateTime()));
        }

        if (session.getTimeLogged() != null) {
            values.put(SESSION_LOGGED_TIME, Long.toString(session.getTimeLogged().getMillis()));
        }

        values.put(SESSION_STATUS, session.getStatus().toString());
        values.put(SESSION_SHAREDNESS, String.valueOf(session.isShared()));
        // Comma separated issue ids
        if (session.getRelatedIssues() != null) {
            StringBuilder relatedIssuesSB = new StringBuilder();
            for (Issue relatedIssue : session.getRelatedIssues()) {
                relatedIssuesSB.append(relatedIssue.getId().toString()).append(",");
            }
            values.put(SESSION_RELATED_ISSUES, relatedIssuesSB.toString());
        }

        if (session.getRelatedProject() != null) {
            values.put(SESSION_RELATED_PROJECT, session.getRelatedProject().getId().toString());
        } else {
            values.put(SESSION_RELATED_PROJECT, Long.toString(-1L));
        }

        // Comma separated issue ids
        StringBuilder raisedIssues = new StringBuilder();

        for (Issue issueRaised : session.getIssuesRaised()) {
            raisedIssues.append(issueRaised.getId().toString()).append(",");
        }

        values.put(SESSION_RAISED_ISSUES, raisedIssues.toString());

        // Session status history
        StringBuilder sessionStatusHistorySerialised = new StringBuilder();
        Map<DateTime, Session.Status> sessionStatusHistory = session.getSessionStatusHistory();

        for (DateTime timestamp : sessionStatusHistory.keySet()) {
            sessionStatusHistorySerialised.append(timestamp.toString(ISODateTimeFormat.dateTime())).append(",")
                    .append(sessionStatusHistory.get(timestamp).toString()).append(",");
        }

        values.put(SESSION_STATUS_HISTORY, sessionStatusHistorySerialised.toString());

        List<Long> sessionNoteIds = session.getSessionNoteIds();

        if (sessionNoteIds != null) {
            values.put(SESSION_NOTE_IDS, sessionNoteIds.toString());
        }

        values.put(SESSION_ADDITIONAL_INFO, session.getAdditionalInfo());

        values.put(SESSION_ACTIVITY_ITEMS, marshalSessionActivityItemList(session.getSessionActivity()));

        List<String> participants = new ArrayList<String>();
        for (Participant p : session.getParticipants()) {
            participants.add(p.toJSON().toString());
        }
        values.put(SESSION_PARTICIPANTS, participants);
        values.put(SESSION_DEFAULT_TEMPLATE, session.getDefaultTemplateId());

        return values;
    }

    private List<Map<String, String>> marshalSessionActivityItemList(List<SessionActivityItem> sessionActivity) {
        List<Map<String, String>> marshalledSessionActivityItems = new ArrayList<Map<String, String>>();
        for (SessionActivityItem sessionActivityItem : sessionActivity) {
            Class sessionActivityItemClass = sessionActivityItem.getClass();

            if (sessionActivityItemClass.equals(IssueRaisedSessionActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((IssueRaisedSessionActivityItem) sessionActivityItem));
            } else if (sessionActivityItemClass.equals(IssueUnraisedSessionActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((IssueUnraisedSessionActivityItem) sessionActivityItem));
            } else if (sessionActivityItemClass.equals(IssueAttachmentSessionActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((IssueAttachmentSessionActivityItem) sessionActivityItem));
            } else if (sessionActivityItemClass.equals(SessionNoteSessionActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((SessionNoteSessionActivityItem) sessionActivityItem));
            } else if (sessionActivityItemClass.equals(SessionStatusSessionActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((SessionStatusSessionActivityItem) sessionActivityItem));
            } else if (sessionActivityItemClass.equals(SessionAssignedSessionActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((SessionAssignedSessionActivityItem) sessionActivityItem));
            } else if (sessionActivityItemClass.equals(SessionJoinedActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((SessionJoinedActivityItem) sessionActivityItem));
            } else if (sessionActivityItemClass.equals(SessionLeftActivityItem.class)) {
                marshalledSessionActivityItems.add(marshalSessionActivityItem((SessionLeftActivityItem) sessionActivityItem));
            } else {
                throw new IllegalArgumentException("No marshal method defined for class " + sessionActivityItemClass.toString());
            }
        }
        return marshalledSessionActivityItems;
    }

    public Map<String, String> marshalSessionActivityItem(IssueRaisedSessionActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("timestamp", sessionActivityItem.getTime().toString(ISODateTimeFormat.dateTime()));
        String userKey = ApplicationUsers.getKeyFor(sessionActivityItem.getUser());
        data.put("user", userKey);
        data.put("issueId", sessionActivityItem.getIssue().getId().toString());
        return data;
    }

    public Map<String, String> marshalSessionActivityItem(IssueUnraisedSessionActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("timestamp", sessionActivityItem.getTime().toString(ISODateTimeFormat.dateTime()));
        String userKey = ApplicationUsers.getKeyFor(sessionActivityItem.getUser());
        data.put("user", userKey);
        data.put("issueId", sessionActivityItem.getIssue().getId().toString());
        return data;
    }

    public Map<String, String> marshalSessionActivityItem(SessionJoinedActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("participant", sessionActivityItem.getParticipant().toJSON().toString());
        return data;
    }

    public Map<String, String> marshalSessionActivityItem(SessionLeftActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("participant", sessionActivityItem.getParticipant().toJSON().toString());
        return data;
    }

    public Map<String, String> marshalSessionActivityItem(IssueAttachmentSessionActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("timestamp", sessionActivityItem.getTime().toString(ISODateTimeFormat.dateTime()));
        String userKey = ApplicationUsers.getKeyFor(sessionActivityItem.getUser());
        data.put("user", userKey);
        data.put("attachmentId", sessionActivityItem.getAttachmentId().toString());
        data.put("issueId", sessionActivityItem.getIssue().getId().toString());
        return data;
    }

    public Map<String, String> marshalSessionActivityItem(SessionStatusSessionActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("timestamp", sessionActivityItem.getTime().toString(ISODateTimeFormat.dateTime()));
        String userKey = ApplicationUsers.getKeyFor(sessionActivityItem.getUser());
        data.put("user", userKey);
        data.put("status", sessionActivityItem.getStatus().toString());
        data.put("isFirstStarted", String.valueOf(sessionActivityItem.isFirstStarted()));
        return data;
    }

    public Map<String, String> marshalSessionActivityItem(SessionNoteSessionActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("timestamp", sessionActivityItem.getTime().toString(ISODateTimeFormat.dateTime()));
        String userKey = ApplicationUsers.getKeyFor(sessionActivityItem.getUser());
        data.put("user", userKey);
        data.put("noteId", sessionActivityItem.getNoteId().toString());
        return data;
    }

    private Map<String, String> marshalSessionActivityItem(SessionAssignedSessionActivityItem sessionActivityItem) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(SESSION_ACTIVITY_ITEM_CLASS, sessionActivityItem.getClass().toString());
        data.put("timestamp", sessionActivityItem.getTime().toString(ISODateTimeFormat.dateTime()));
        String assignerKey = ApplicationUsers.getKeyFor(sessionActivityItem.getUser());
        data.put("assigner", assignerKey);
        String assigneeKey = ApplicationUsers.getKeyFor(sessionActivityItem.getAssignee());
        data.put("assignee", assigneeKey);
        return data;
    }

    // UNMARSHAL//////////////////////////////////////////////////////////////////////////////////////////////////
    public Session unMarshal(Long id, Map<String, Object> data) {
        SessionBuilder sb = new SessionBuilder(id, excaliburWebUtil);

        // Session Notes
        String sessionNoteIdsRaw = ((String) data.get(SESSION_NOTE_IDS));
        String[] sessionNoteIdArray = sessionNoteIdsRaw.substring(1, sessionNoteIdsRaw.length() - 1).split(",");
        List<Long> sessionNoteIds = new ArrayList<Long>(sessionNoteIdArray.length);
        for (String sessionNoteId : sessionNoteIdArray) {
            if (!StringUtils.isEmpty(sessionNoteId)) {
                sessionNoteIds.add(Long.valueOf(sessionNoteId.trim()));
            }
        }
        sb.setSessionNoteIds(sessionNoteIds);

        sb.setName((String) data.get(SESSION_NAME));
        sb.setShared(Boolean.valueOf((String) data.get(SESSION_SHAREDNESS)));

        ApplicationUser creator = userService.safeGetUserByKey((String) data.get(SESSION_CREATOR));
        sb.setCreator(creator);

        // TODO Upgrade task so we don't have clutter here - tech debt
        if (data.containsKey(SESSION_ASSIGNEE)) {
            String assigneeName = (String) data.get(SESSION_ASSIGNEE);
            sb.setAssigneeNoActivityItem(userService.safeGetUserByKey(assigneeName));
        } else {
            sb.setAssigneeNoActivityItem(creator);
        }

        sb.setTimeCreated(ISODateTimeFormat.dateTime().parseDateTime((String) data.get(SESSION_STARTED)));

        if (data.containsKey(SESSION_ENDED)) {
            sb.setTimeFinished(ISODateTimeFormat.dateTime().parseDateTime((String) data.get(SESSION_ENDED)));
        }

        if (data.containsKey(SESSION_LOGGED_TIME)) {
            sb.setTimeLogged(new Duration(new Long((String) data.get(SESSION_LOGGED_TIME))));
        }

        try {
            sb.setStatus(Session.Status.valueOf((String) data.get(SESSION_STATUS)));
        } catch (IllegalArgumentException e) {
            // We have stale data here, revert status to the start and log an error
            log.warn("Unknown status for session " + id + ", reverting to completed.");
            sb.setStatus(Session.Status.COMPLETED);
        }
        String allRelatedIssues = (String) data.get(SESSION_RELATED_ISSUES);
        if (allRelatedIssues != null) {
            String[] relatedIssueIds = allRelatedIssues.split(",");
            for (String s : relatedIssueIds) {
                if (!StringUtils.isEmpty(s)) {
                    Issue relatedIssue = jiraIssueManager.getIssueObject(Long.valueOf(s));
                    if (relatedIssue != null) {
                        sb.addRelatedIssue(relatedIssue);
                    }
                }
            }
        }
        sb.setRelatedProject(jiraProjectManager.getProjectObj(Long.valueOf((String) data.get(SESSION_RELATED_PROJECT))));

        // Don't think we'll be doing random access much, so LinkedList is good.
        List<Issue> issuesRaised = new LinkedList<Issue>();

        String allRaisedIssues = (String) data.get(SESSION_RAISED_ISSUES);

        if (allRaisedIssues != null) {
            String[] raisedIssueIds = allRaisedIssues.split(",");

            for (String raisedIssueId : raisedIssueIds) {
                if (raisedIssueId.isEmpty()) {
                    break;
                }
                Issue raisedIssue = jiraIssueManager.getIssueObject(Long.valueOf(raisedIssueId));
                if (raisedIssue != null) {
                    issuesRaised.add(raisedIssue);
                }
            }

            sb.setIssuesRaised(issuesRaised);
        }

        // Session Status History
        String sessionStatusHistorySerialised = (String) data.get(SESSION_STATUS_HISTORY);

        Map<DateTime, Session.Status> sessionStatusHistory = new HashMap<DateTime, Session.Status>();

        if (!StringUtils.isEmpty(sessionStatusHistorySerialised)) {
            String[] sessionStatusHistoryArray = sessionStatusHistorySerialised.split(",");
            // Should have pairs
            isTrue(sessionStatusHistoryArray.length % 2 == 0);
            // TODO De-uglify
            for (int i = 0; i < sessionStatusHistoryArray.length; i += 2) {
                try {
                    sessionStatusHistory.put(ISODateTimeFormat.dateTime().parseDateTime(sessionStatusHistoryArray[i]),
                            Session.Status.valueOf(sessionStatusHistoryArray[i + 1]));
                } catch (IllegalArgumentException e) {
                    // Status is bad, so lets just not add this one
                    log.warn("Lost session " + id + " status history item, due to unknown session status " + sessionStatusHistoryArray[i + 1]);
                }
            }
        }
        sb.setSessionStatusHistory(sessionStatusHistory);

        sb.setAdditionalInfo(StringUtils.defaultString((String) data.get(SESSION_ADDITIONAL_INFO)));

        sb.setSessionActivity(unmarshalSessionActivityItemList((List<Map<String, String>>) data.get(SESSION_ACTIVITY_ITEMS)));

        List<String> participantsJSON = (List<String>) data.get(SESSION_PARTICIPANTS);
        List<Participant> participants = new ArrayList<Participant>();
        if (participantsJSON != null) {
            for (String json : participantsJSON) {
                participants.add(bonfireUnmarshalService.getParticipantFromJSON(new JSONObject(json)));
            }
        }
        sb.setParticipants(participants);
        String defaultTemplateId = (String) data.get(SESSION_DEFAULT_TEMPLATE);
        if (StringUtils.isNotBlank(defaultTemplateId)) {
            sb.setDefaultTemplateId(defaultTemplateId);
        }

        // Validation - returns null if the session is no longer valid
        return validateAndReturnSession(sb.build());
    }

    public LightSession unMarshalLightSession(Long id, Map<String, Object> data) {
        String name = (String) data.get(SESSION_NAME);
        ApplicationUser assignee, creator;
        creator = userService.safeGetUserByKey((String) data.get(SESSION_CREATOR));
        if (data.containsKey(SESSION_ASSIGNEE)) {
            assignee = userService.safeGetUserByKey((String) data.get(SESSION_ASSIGNEE));
        } else {
            assignee = creator;
        }
        boolean shared = Boolean.valueOf((String) data.get(SESSION_SHAREDNESS));
        Status status;
        try {
            status = Session.Status.valueOf((String) data.get(SESSION_STATUS));
        } catch (IllegalArgumentException e) {
            status = Session.Status.COMPLETED;
        }
        Project project = jiraProjectManager.getProjectObj(Long.valueOf((String) data.get(SESSION_RELATED_PROJECT)));
        // No project, no session
        if (project == null) {
            return null;
        }
        String defaultTemplateId = (String) data.get(SESSION_DEFAULT_TEMPLATE);
        if (StringUtils.isBlank(defaultTemplateId)) {
            defaultTemplateId = "";
        }
        String additionalInfo = StringUtils.defaultString((String) data.get(SESSION_ADDITIONAL_INFO));
        return new LightSession(id,
                name,
                creator,
                assignee,
                status,
                shared,
                project,
                defaultTemplateId,
                additionalInfo,
                data);
    }

    private List<SessionActivityItem> unmarshalSessionActivityItemList(List<Map<String, String>> sessionActivity) {
        List<SessionActivityItem> unmarshalledSessionActivityItems = new ArrayList<SessionActivityItem>();
        if (sessionActivity != null) {
            for (Map<String, String> marshalledSessionActivityItem : sessionActivity) {
                if (marshalledSessionActivityItem != null) {
                    SessionActivityItem sessionActivityItem = unmarshalSessionActivityItem(marshalledSessionActivityItem);
                    if (sessionActivityItem == NO_LONGER_VALID) {
                        continue;
                    }
                    unmarshalledSessionActivityItems.add(sessionActivityItem);
                }
            }
        }
        return unmarshalledSessionActivityItems;
    }

    private SessionActivityItem unmarshalSessionActivityItem(Map<String, String> data) {
        String type = data.get(SESSION_ACTIVITY_ITEM_CLASS);
        notNull(type);

        if (type.equals(IssueRaisedSessionActivityItem.class.toString())) {
            return unmarshalIssueRaisedSessionActivityItem(data);
        }
        if (type.equals(IssueUnraisedSessionActivityItem.class.toString())) {
            return unmarshalIssueUnraisedSessionActivityItem(data);
        } else if (type.equals(IssueAttachmentSessionActivityItem.class.toString())) {
            return unmarshalIssueAttachmentSessionActivityItem(data);
        } else if (type.equals(SessionStatusSessionActivityItem.class.toString())) {
            return unmarshalSessionStatusActivityItem(data);
        } else if (type.equals(SessionNoteSessionActivityItem.class.toString())) {
            return unmarshalSessionNoteActivityItem(data);
        } else if (type.equals(SessionAssignedSessionActivityItem.class.toString())) {
            return unmarshalSessionAssignedSessionActivityItem(data);
        } else if (type.equals(SessionJoinedActivityItem.class.toString())) {
            return unmarshalSessionJoinedActivityItem(data);
        } else if (type.equals(SessionLeftActivityItem.class.toString())) {
            return unmarshalSessionLeftActivityItem(data);
        }

        log.error("No unmarshaller defined for type: " + type);
        return null;
    }

    private SessionActivityItem unmarshalSessionAssignedSessionActivityItem(Map<String, String> data) {
        DateTime timestamp = ISODateTimeFormat.dateTime().parseDateTime(data.get("timestamp"));
        ApplicationUser assigner = userService.safeGetUserByKey(data.get("assigner"));
        ApplicationUser assignee = userService.safeGetUserByKey(data.get("assignee"));

        return new SessionAssignedSessionActivityItem(timestamp, assigner, assignee, excaliburWebUtil);
    }

    private SessionActivityItem unmarshalSessionJoinedActivityItem(Map<String, String> data) {
        Participant participant = bonfireUnmarshalService.getParticipantFromJSON(new JSONObject(data.get("participant")));
        return new SessionJoinedActivityItem(participant, excaliburWebUtil);
    }

    private SessionActivityItem unmarshalSessionLeftActivityItem(Map<String, String> data) {
        Participant participant = bonfireUnmarshalService.getParticipantFromJSON(new JSONObject(data.get("participant")));
        return new SessionLeftActivityItem(participant, excaliburWebUtil);
    }

    private SessionActivityItem unmarshalSessionNoteActivityItem(Map<String, String> data) {
        DateTime timestamp = ISODateTimeFormat.dateTime().parseDateTime(data.get("timestamp"));
        ApplicationUser user = userService.safeGetUserByKey(data.get("user"));
        Long noteId = Long.valueOf(data.get("noteId"));

        return new SessionNoteSessionActivityItem(timestamp, user, noteId, excaliburWebUtil);
    }

    private SessionActivityItem unmarshalSessionStatusActivityItem(Map<String, String> data) {
        DateTime timestamp = ISODateTimeFormat.dateTime().parseDateTime(data.get("timestamp"));
        ApplicationUser user = userService.safeGetUserByKey(data.get("user"));
        Session.Status status = Session.Status.valueOf(data.get("status"));
        boolean firstStarted = Boolean.valueOf(data.get("isFirstStarted"));

        return new SessionStatusSessionActivityItem(timestamp, user, status, firstStarted, excaliburWebUtil);
    }

    private SessionActivityItem unmarshalIssueAttachmentSessionActivityItem(Map<String, String> data) {
        DateTime timestamp = ISODateTimeFormat.dateTime().parseDateTime(data.get("timestamp"));
        ApplicationUser user = userService.safeGetUserByKey(data.get("user"));
        Long issueId = Long.valueOf(data.get("issueId"));

        Issue issue = jiraIssueManager.getIssueObject(issueId);
        if (issue == null) {
            return NO_LONGER_VALID;
        }
        final Long attachmentId = Long.valueOf(data.get("attachmentId"));
        Attachment attachment = getAttachmentSafely(attachmentId);
        if (attachment == null) {
            return NO_LONGER_VALID;
        }
        Thumbnail thumbnail;
        try {
            thumbnail = jiraThumbnailManager.getThumbnail(attachment);
        } catch (IllegalArgumentException drat) {
            return NO_LONGER_VALID;
        }
        return new IssueAttachmentSessionActivityItem(timestamp, user, issueId, issue, attachmentId, attachment, thumbnail, excaliburWebUtil);
    }

    private Attachment getAttachmentSafely(final Long attachmentId) {
        try {
            return jiraAttachmentManager.getAttachment(attachmentId);
        } catch (AttachmentNotFoundException whyJiraWhy) {
            return null;
        }

    }

    private SessionActivityItem unmarshalIssueRaisedSessionActivityItem(Map<String, String> data) {
        DateTime timestamp = ISODateTimeFormat.dateTime().parseDateTime(data.get("timestamp"));
        ApplicationUser user = userService.safeGetUserByKey(data.get("user"));
        Long issueId = Long.valueOf(data.get("issueId"));
        Issue issue = jiraIssueManager.getIssueObject(issueId);
        if (issue == null) {
            return NO_LONGER_VALID;
        }
        return new IssueRaisedSessionActivityItem(timestamp, user, issueId, issue, excaliburWebUtil);
    }

    private SessionActivityItem unmarshalIssueUnraisedSessionActivityItem(Map<String, String> data) {
        DateTime timestamp = ISODateTimeFormat.dateTime().parseDateTime(data.get("timestamp"));
        ApplicationUser user = userService.safeGetUserByKey(data.get("user"));
        Long issueId = Long.valueOf(data.get("issueId"));
        Issue issue = jiraIssueManager.getIssueObject(issueId);
        if (issue == null) {
            return NO_LONGER_VALID;
        }
        return new IssueUnraisedSessionActivityItem(timestamp, user, issueId, issue, excaliburWebUtil);
    }

    /**
     * Validate the given session
     *
     * @param input Session to validate
     * @return null if session is invalid, input otherwise
     */
    private Session validateAndReturnSession(Session input) {
        log.debug("Validating session.");
        // If we have null to start with, return null
        if (input == null) {
            return null;
        }

        // Session must have a related project
        if (input.getRelatedProject() == null) {
            return null;
        }

        // Session must have an owner
        if (input.getCreator() == null) {
            return null;
        }

        return input;
    }

}
