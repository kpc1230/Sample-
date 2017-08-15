package com.thed.zephyr.capture.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import java.time.Duration;
import java.util.*;

/**
 * Created by aliakseimatsarski on 8/14/17.
 */
public class Session  implements Comparable<Session> {
    private final Long id;
    private final String creator;
    private final String assignee;
    private final String name;
    private final String additionalInfo;
    private final Status status;
    private final List<Issue> relatedIssues;
    private final Project relatedProject;
    private final DateTime timeCreated;
    private final DateTime timeFinished;
    private final Duration timeLogged;
    private final List<Issue> issuesRaised;
    private final Map<DateTime, Status> sessionStatusHistory;
    private final List<SessionActivityItem> sessionActivity;
    private final Map<Long, Note> sessionNotes;
    private final List<Long> sessionNoteIds;
    private final boolean shared;
    private final List<String> participants;
    private final String defaultTemplateId;

    public Session(Long id,
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
                   List<Long> sessionNoteIds,
                   boolean shared,
                   List<String> participants,
                   String defaultTemplateId) {
        this.id = id;
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
        this.defaultTemplateId = defaultTemplateId;
    }

    public List<Issue> getRelatedIssues() {
        return relatedIssues;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
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

    public List<Issue> getIssuesRaised() {
        return Collections.unmodifiableList(issuesRaised);
    }

    public List<String> getParticipants() {
        List<String> copy = new ArrayList<String>(participants);
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

    public List<SessionActivityItem> getSessionActivity() {
        return Collections.unmodifiableList(sessionActivity);
    }

    public List<Long> getSessionNoteIds() {
        return Collections.unmodifiableList(sessionNoteIds);
    }

    public Note getNote(Long noteId) {
        return sessionNotes.get(noteId);
    }

    public enum Status {
        CREATED, STARTED, PAUSED, COMPLETED
    }

    ;

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

    public int compareTo(Session session) {
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
