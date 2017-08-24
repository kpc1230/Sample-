package com.thed.zephyr.capture.model;

import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.jira.Attachment;
import org.joda.time.DateTime;
import com.google.common.collect.Lists;

import java.time.Duration;
import java.util.*;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class SessionBuilder {
    private String id;
    private String clientKey;
    private String creator;
    private String assignee;
    private String name;
    private String additionalInfo;
    private Session.Status status;
    private List<Issue> relatedIssues;
    private Project relatedProject;
    private DateTime timeCreated;
    private DateTime timeFinished;
    private Duration timeLogged;
    private List<Issue> issuesRaised;
    private Map<DateTime, Session.Status> sessionStatusHistory;
    private List<SessionActivityItem> sessionActivity;
    private Map<Long, Note> sessionNotes;
    private Set<Long> sessionNoteIds;
    private boolean shared;
    private List<Participant> participants;
    private Set<Long> participantIds;
    private String defaultTemplateId;

    public SessionBuilder(Session session) {
        this.id = session.getId();
        this.clientKey = session.getClientKey();
        this.creator = session.getCreator();
        this.assignee = session.getAssignee();
        this.name = session.getName();
        this.additionalInfo = session.getAdditionalInfo() != null ? session.getAdditionalInfo() : "";
        this.status = session.getStatus();
        this.relatedIssues = Lists.newArrayList(session.getRelatedIssues());
        this.relatedProject = session.getRelatedProject();
        this.timeCreated = session.getTimeCreated();
        this.timeFinished = session.getTimeFinished();
        this.timeLogged = session.getTimeLogged();
        this.issuesRaised = Lists.newArrayList(session.getIssuesRaised());
        this.sessionStatusHistory = new HashMap<DateTime, Session.Status>(session.getSessionStatusHistory());
        this.sessionActivity = Lists.newArrayList(session.getSessionActivity());
        this.sessionNoteIds = new TreeSet<>(session.getSessionNoteIds());
        this.sessionNotes = new HashMap<Long, Note>(session.getSessionNotes());
        this.shared = session.isShared();
        this.participants = Lists.newArrayList(session.getParticipants());
        this.participantIds = new TreeSet<>(session.getParticipantIds());
        this.defaultTemplateId = session.getDefaultTemplateId();
    }

    public SessionBuilder(String id/*, ExcaliburWebUtil webUtil*/) {
    //    this.webUtil = webUtil;
        this.id = id;
        this.issuesRaised = Lists.newArrayList();
        this.relatedIssues = Lists.newArrayList();
        this.sessionStatusHistory = new HashMap<DateTime, Session.Status>();
        this.sessionNotes = new HashMap<Long, Note>();
        this.sessionNoteIds = new TreeSet<>();
        this.sessionActivity = Lists.newArrayList();
        this.participants = Lists.newArrayList();
        this.additionalInfo = "";
    }
    
    public SessionBuilder() {
    	this.issuesRaised = Lists.newArrayList();
        this.relatedIssues = Lists.newArrayList();
        this.sessionStatusHistory = new HashMap<DateTime, Session.Status>();
        this.sessionNotes = new HashMap<Long, Note>();
        this.sessionNoteIds = new TreeSet<>();
        this.sessionActivity = Lists.newArrayList();
        this.participants = Lists.newArrayList();
        this.additionalInfo = "";
    }

    public Session build() {
        return new Session(id,
                clientKey,
                creator,
                assignee,
                name,
                additionalInfo,
                status,
                relatedIssues,
                relatedProject,
                timeCreated,
                timeFinished,
                timeLogged,
                issuesRaised,
                sessionStatusHistory,
                sessionActivity,
                sessionNotes,
                sessionNoteIds,
                shared,
                participants,
                participantIds,
                defaultTemplateId);
    }


    public SessionBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public SessionBuilder setCreator(String creator) {
        this.creator = creator;
        // When the creator is set, we want to set the assignee to the same without creating an activity item
        if (assignee == null) {
            assignee = creator;
        }
        return this;
    }

    public SessionBuilder setAssignee(String assigner, String assignee, String avatarUrl) {
        // Don't do redundant assigns
        if (this.assignee == null || !this.assignee.equals(assignee)) {
            this.assignee = assignee;
            sessionActivity.add(new SessionAssignedSessionActivityItem(new DateTime(), assigner, assignee, avatarUrl));
        }
        return this;
    }

    public SessionBuilder setAssigneeNoActivityItem(String assignee) {
        this.assignee = assignee;
        return this;
    }

    public SessionBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public SessionBuilder setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
        return this;
    }

    public SessionBuilder setShared(boolean shared) {
        this.shared = shared;
        return this;
    }

    public SessionBuilder setStatus(Session.Status status, String avatarUrl) {
        DateTime timestamp = new DateTime();
        if (status != null && this.status != status) {
            sessionStatusHistory.put(timestamp, status);

            // Session Activity items
            boolean firstStarted = (this.status == Session.Status.CREATED && status == Session.Status.STARTED);
            sessionActivity.add(new SessionStatusSessionActivityItem(timestamp, assignee, status, firstStarted, avatarUrl));
            this.status = status;
        }
        return this;
    }

    public SessionBuilder setParticipants(List<Participant> participants) {
        this.participants = participants;
        return this;
    }

    public SessionBuilder addParticipantJoined(String user, String avatarUrl) {
        boolean currentlyParticipating = false;
        DateTime now = new DateTime();
        for (Iterator<Participant> iterator = participants.iterator(); iterator.hasNext(); ) {
            Participant participant = iterator.next();
            if (participant.getUser().equals(user) && !participant.hasLeft()) {
                currentlyParticipating = true;
            }
        }
        if (!currentlyParticipating) {
            Participant participant = new ParticipantBuilder(user).setTimeJoined(now).build();
            participants.add(participant);
            sessionActivity.add(new SessionJoinedActivityItem(participant, avatarUrl));
        }
        return this;
    }

    public SessionBuilder addParticipantLeft(String user, String avatarUrl) {
        DateTime now = new DateTime();
        for (Iterator<Participant> iterator = participants.iterator(); iterator.hasNext(); ) {
            Participant participant = iterator.next();
            if (user != null && user.equals(participant.getUser()) && !participant.hasLeft()) {
                // ok we have a person how has joined but not left
                iterator.remove();

                participant = new ParticipantBuilder(participant).setTimeLeft(now).build();
                participants.add(participant);
                sessionActivity.add(new SessionLeftActivityItem(participant, avatarUrl));
                break;
            }
        }
        return this;
    }

    public SessionBuilder setRelatedIssues(List<Issue> relatedIssues) {
        this.relatedIssues = relatedIssues;
        return this;
    }

    public SessionBuilder addRelatedIssue(Issue relatedIssue) {
        this.relatedIssues.add(relatedIssue);
        return this;
    }

    public SessionBuilder setRelatedProject(Project relatedProject) {
        this.relatedProject = relatedProject;
        return this;
    }

    public SessionBuilder setTimeCreated(DateTime timeCreated) {
        this.timeCreated = timeCreated;
        return this;
    }

    public SessionBuilder setTimeFinished(DateTime timeFinished) {
        this.timeFinished = timeFinished;
        return this;
    }

    public SessionBuilder setTimeLogged(Duration timeLogged) {
        this.timeLogged = timeLogged;
        return this;
    }

    public SessionBuilder setIssuesRaised(List<Issue> issuesRaised) {
        this.issuesRaised = issuesRaised;
        return this;
    }

    public SessionBuilder setSessionStatusHistory(Map<DateTime, Session.Status> sessionStatusHistory) {
        this.sessionStatusHistory = sessionStatusHistory;
        return this;
    }

    public SessionBuilder setSessionActivity(List<SessionActivityItem> sessionActivity) {
        this.sessionActivity = sessionActivity;
        return this;
    }

    public SessionBuilder setDefaultTemplateId(String defaultTemplateId) {
        this.defaultTemplateId = defaultTemplateId;
        return this;
    }

    public SessionBuilder setSessionNotes(Iterable<Note> sessionNotes) {
        // TODO We take in an Iterable here as a nod to the future
        // Once we can move to iterables instead of lists / maps here, it's another transparent change (awesome)
        Map<Long, Note> noteMap = new HashMap<Long, Note>();
        for (Note note : sessionNotes) {
            noteMap.put(note.getId(), note);
        }
        this.sessionNotes = noteMap;
        return this;
    }

    public SessionBuilder setSessionNoteIds(Set<Long> sessionNoteIds) {
        this.sessionNoteIds = sessionNoteIds;
        return this;
    }

    public SessionBuilder addNote(Note note, String creator, String avatarUrl) {
        sessionNotes.put(note.getId(), note);

        sessionNoteIds.add(note.getId());

        // This is where we need the creator - this avoids us looking up the user in the UserManager again if we don't have to
        sessionActivity.add(new SessionNoteSessionActivityItem(note.getCreatedTime(), creator, note.getId(), avatarUrl));

        return this;
    }

    public SessionBuilder deleteNote(Note note) {
        sessionNotes.remove(note.getId());

        sessionNoteIds.remove(note.getId());

        // TODO is there a cleaner way to do this?
        SessionActivityItem markedForDeletion = null;
        for (SessionActivityItem item : sessionActivity) {
            if (item instanceof SessionNoteSessionActivityItem) {
                if (((SessionNoteSessionActivityItem) item).getNoteId().equals(note.getId())) {
                    markedForDeletion = item;
                    break;
                }
            }
        }

        sessionActivity.remove(markedForDeletion);

        return this;
    }

    public SessionBuilder addRaisedIssue(Issue issue, DateTime timeRaised, String creator) {
        issuesRaised.add(issue);

        sessionActivity.add(new IssueRaisedSessionActivityItem(timeRaised, creator, issue.getId(), issue));

        return this;
    }

    public SessionBuilder removeRaisedIssue(Issue issue, DateTime timeRemoved, String remover) {
        issuesRaised.remove(issue);

        sessionActivity.add(new IssueUnraisedSessionActivityItem(timeRemoved, remover, issue.getId(), issue));

        return this;
    }

    public SessionBuilder addAttachment(DateTime timestamp,
                                        String user,
                                        Issue issue,
                                        Attachment attachment,
                                        Thumbnail thumbnail) {
        sessionActivity.add(new IssueAttachmentSessionActivityItem(timestamp, user, issue.getId(), issue, attachment.getId(), attachment, thumbnail));

        return this;
    }
}
