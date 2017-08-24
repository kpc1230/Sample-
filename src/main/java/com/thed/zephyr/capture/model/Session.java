package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.service.db.DateTimeTypeConverter;
import com.thed.zephyr.capture.service.db.DurationTypeConverter;
import com.thed.zephyr.capture.service.db.SessionIssueCollectionConverter;
import com.thed.zephyr.capture.service.db.SessionRelatedProjectTypeConverter;
import com.thed.zephyr.capture.service.db.SessionStatusTypeConverter;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;

import java.time.Duration;
import java.util.*;


/**
 * Created by aliakseimatsarski on 8/14/17.
 */
@DynamoDBTable(tableName = ApplicationConstants.SESSION_TABLE_NAME)
public class Session  implements Comparable<Session> {

    @Id
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = ApplicationConstants.GSI_CLIENT_KEY)
    private String clientKey;
    private String creator;
    private String assignee;
    private String name;
    private String additionalInfo;
    @DynamoDBTypeConverted(converter = SessionStatusTypeConverter.class)
    private Status status;
    @DynamoDBTypeConverted(converter = SessionIssueCollectionConverter.class)
    private Collection<Issue> relatedIssues;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_CLIENT_KEY)
    @DynamoDBTypeConverted(converter = SessionRelatedProjectTypeConverter.class)
    private Project relatedProject;
    @DynamoDBTypeConverted(converter = DateTimeTypeConverter.class)
    private DateTime timeCreated;
    @DynamoDBTypeConverted(converter = DateTimeTypeConverter.class)
    private DateTime timeFinished;
    @DynamoDBTypeConverted(converter = DurationTypeConverter.class)
    private Duration timeLogged;
    @DynamoDBTypeConverted(converter = SessionIssueCollectionConverter.class)
    private Collection<Issue> issuesRaised;
    @DynamoDBIgnore
    private Map<DateTime, Status> sessionStatusHistory;
    @DynamoDBIgnore
    private Collection<SessionActivityItem> sessionActivity;
    @DynamoDBIgnore
    private Map<Long, Note> sessionNotes;
    @DynamoDBIgnore
    private Set<Long> sessionNoteIds;
    private boolean shared;
    @DynamoDBIgnore
    private Collection<Participant> participants;
    private Set<Long> participantIds;
    private String defaultTemplateId;

    public Session(String id,
                   String clientKey,
                   String creator,
                   String assignee,
                   String name,
                   String additionalInfo,
                   Status status,
                   List<Issue> relatedIssues,
                   Project relatedProject,
                   DateTime timeCreated,
                   DateTime timeFinished,
                   Duration timeLogged,
                   List<Issue> issuesRaised,
                   Map<DateTime, Status> sessionStatusHistory,
                   List<SessionActivityItem> sessionActivity,
                   Map<Long, Note> sessionNotes,
                   Set<Long> sessionNoteIds,
                   boolean shared,
                   List<Participant> participants,
                   Set<Long> participantIds,
                   String defaultTemplateId) {
        this.id = id;
        this.clientKey = clientKey;
        this.creator = creator;
        this.assignee = assignee;
        this.name = name;
        this.additionalInfo = additionalInfo;
        this.status = status;
        this.relatedIssues = relatedIssues;
        this.relatedProject = relatedProject;
        this.timeCreated = timeCreated;
        this.timeFinished = timeFinished;
        this.timeLogged = timeLogged;
        this.issuesRaised = issuesRaised;
        this.sessionStatusHistory = sessionStatusHistory;
        this.sessionActivity = sessionActivity;
        this.sessionNotes = sessionNotes;
        this.sessionNoteIds = sessionNoteIds;
        this.shared = shared;
        this.participants = participants;
        this.participantIds = participantIds;
        this.defaultTemplateId = defaultTemplateId;
    }

    public Collection<Issue> getRelatedIssues() {
        return relatedIssues;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getClientKey(){
        return clientKey;
    }

    public String getCreator() {
        return creator;
    }

    public String getAssignee() {
        return assignee;
    }

    public Status getStatus() {
        return status;
    }

    public DateTime getTimeCreated() {
        return timeCreated;
    }

    public DateTime getTimeFinished() {
        return timeFinished;
    }

    public Duration getTimeLogged() {
        return timeLogged;
    }

    public String getDefaultTemplateId() {
        return defaultTemplateId;
    }

    public Collection<Issue> getIssuesRaised() {
        return issuesRaised;
    }

    public List<Participant> getParticipants() {
        List<Participant> copy = new ArrayList<Participant>(participants);
        Collections.sort(copy);
        return Collections.unmodifiableList(copy);
    }

    public boolean isShared() {
        return shared;
    }

    /**
     * Get the Session Status History (timestamp to status mapping)
     *
     * @return Map time instant to new status
     */
    public Map<DateTime, Status> getSessionStatusHistory() {
        return Collections.unmodifiableMap(sessionStatusHistory);
    }

    public Project getRelatedProject() {
        return relatedProject;
    }

    public Map<Long, Note> getSessionNotes() {
        return Collections.unmodifiableMap(sessionNotes);
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public Collection<SessionActivityItem> getSessionActivity() {
        return sessionActivity;
    }

    public Collection<Long> getSessionNoteIds() {
        return sessionNoteIds;
    }

    public Note getNote(Long noteId) {
        return sessionNotes.get(noteId);
    }

    public Set<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(Set<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public enum Status {
        CREATED, STARTED, PAUSED, COMPLETED
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Session session = (Session) o;

        if (additionalInfo != null ? !additionalInfo.equals(session.additionalInfo) : session.additionalInfo != null)
            return false;
        if (assignee != null ? !assignee.equals(session.assignee) : session.assignee != null) return false;
        if (creator != null ? !creator.equals(session.creator) : session.creator != null) return false;
        if (id != null ? !id.equals(session.id) : session.id != null) return false;
        if (issuesRaised != null ? !issuesRaised.equals(session.issuesRaised) : session.issuesRaised != null)
            return false;
        if (participants != null ? !participants.equals(session.participants) : session.participants != null)
            return false;
        if (name != null ? !name.equals(session.name) : session.name != null) return false;
        if (relatedProject != null ? !relatedProject.equals(session.relatedProject) : session.relatedProject != null)
            return false;
        if (sessionActivity != null ? !sessionActivity.equals(session.sessionActivity) : session.sessionActivity != null)
            return false;
        if (sessionNoteIds != null ? !sessionNoteIds.equals(session.sessionNoteIds) : session.sessionNoteIds != null)
            return false;
        if (sessionNotes != null ? !sessionNotes.equals(session.sessionNotes) : session.sessionNotes != null)
            return false;
        if (sessionStatusHistory != null ? !sessionStatusHistory.equals(session.sessionStatusHistory) : session.sessionStatusHistory != null)
            return false;
        if (status != session.status) return false;
        if (timeCreated != null ? !timeCreated.equals(session.timeCreated) : session.timeCreated != null) return false;
        if (timeFinished != null ? !timeFinished.equals(session.timeFinished) : session.timeFinished != null)
            return false;
        if (timeLogged != null ? !timeLogged.equals(session.timeLogged) : session.timeLogged != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (creator != null ? creator.hashCode() : 0);
        result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (additionalInfo != null ? additionalInfo.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (relatedProject != null ? relatedProject.hashCode() : 0);
        result = 31 * result + (timeCreated != null ? timeCreated.hashCode() : 0);
        result = 31 * result + (timeFinished != null ? timeFinished.hashCode() : 0);
        result = 31 * result + (timeLogged != null ? timeLogged.hashCode() : 0);
        result = 31 * result + (issuesRaised != null ? issuesRaised.hashCode() : 0);
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (sessionStatusHistory != null ? sessionStatusHistory.hashCode() : 0);
        result = 31 * result + (sessionActivity != null ? sessionActivity.hashCode() : 0);
        result = 31 * result + (sessionNotes != null ? sessionNotes.hashCode() : 0);
        result = 31 * result + (sessionNoteIds != null ? sessionNoteIds.hashCode() : 0);
        return result;
    }

    /**
     * Compare to another session.
     *
     * @param session Session to compare to
     * @return 0 if equal to session, otherwise based on id, name or owner in that order.
     */

    @Override
    public int compareTo(Session session) {
        if(session == null) return 1;
        // If equal, return zero
        if (this.id != null && this.id.compareTo(session.getId()) != 0) {
            return -this.id.compareTo(session.getId());
        } else if (this.timeCreated != null && this.timeCreated.compareTo(session.getTimeCreated()) != 0) {
            return this.timeCreated.compareTo(session.getTimeCreated());
        } else if (this.name != null && this.name.compareTo(session.getName()) != 0) {
            return this.name.compareTo(session.getName());
        } else if (this.creator != null && this.creator.compareTo(session.getCreator()) != 0) {
            return this.creator.compareTo(session.getCreator());
        } else if (this.status != null && this.status.compareTo(session.getStatus()) != 0) {
            return this.status.compareTo(session.getStatus());
        } else {
            // Need to consider the fact that all of our fields (except id) could be null, but theirs could be non-null
            return 0;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
