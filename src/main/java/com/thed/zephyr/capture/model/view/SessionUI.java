package com.thed.zephyr.capture.model.view;

import com.opensymphony.util.TextUtils;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.SessionActivityItem;
import com.thed.zephyr.capture.model.jira.Project;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.time.Duration;
import java.util.List;

/**
 * Created by aliakseimatsarski on 8/16/17.
 */
public class SessionUI {
    private Session session;

    private List<SessionActivityItem> sessionActivity;
    private String timeLoggedString;
    private String additionalInfoRaw;
    private String additionalInfoHtml;
    private String estimatedTimeSpent;
    private List<IssueUI> visibleRaisedIssues;
    private List<IssueUI> visibleRaisedSubTasks;
    private List<IssueUI> visibleRelatedIssues;
    private List<IssueUI> possibleTimetracking;
    private String currentBrowser;
    private String sessionStatusPretty;
    private String sessionStatusChange;
    private String sessionReturnURL;
    private List<ParticipantUI> participants;
    private String relatedProjectName;
    private String dateCreated;
    private String dateCompleted;
    private String raisedIssueNavLink;
    private boolean timeTrackingOn;

  //  private SessionDisplayHelper displayHelper;

    // Constructor used for the test-session-view.vm - ugly beast
    public SessionUI(Session session, List<SessionActivityItem> sessionActivity, Duration estimatedTimeSpent, String timeLoggedString, String additionalInfoHtml,
                     String currentBrowser, String sessionStatusPretty, String sessionStatusChange, String sessionReturnURL, List<ParticipantUI> participants,
                     List<IssueUI> visibleRelatedIssues, List<IssueUI> possibleTimetracking, String raisedIssueNavLink, List<IssueUI> visibleRaisedIssues,
                     List<IssueUI> visibleRaisedSubTasks, boolean timeTrackingOn) {
        this.session = session;
        this.sessionActivity = sessionActivity;
        this.timeLoggedString = timeLoggedString;
        this.additionalInfoRaw = session.getAdditionalInfo();
        this.additionalInfoHtml = additionalInfoHtml;
        this.estimatedTimeSpent = estimatedTimeSpent.toString();
        this.relatedProjectName = session.getRelatedProject().getName();
        this.visibleRelatedIssues = visibleRelatedIssues;
        this.possibleTimetracking = possibleTimetracking;
        this.currentBrowser = currentBrowser;
        this.sessionStatusPretty = sessionStatusPretty;
        this.sessionStatusChange = sessionStatusChange;
        this.sessionReturnURL = TextUtils.htmlEncode(sessionReturnURL);
        this.participants = participants;
        this.dateCreated = session.getTimeCreated().toString();
        this.dateCompleted = session.getTimeFinished() != null ? session.getTimeFinished().toString() : "not.specified";
        this.raisedIssueNavLink = raisedIssueNavLink;
        this.visibleRaisedIssues = visibleRaisedIssues;
        this.visibleRaisedSubTasks = visibleRaisedSubTasks;
     //   this.displayHelper = displayHelper;
        this.timeTrackingOn = timeTrackingOn;
    }

    public SessionUI(Session session) {
        this.session = session;
        this.timeLoggedString = session.getTimeLogged().toString();
    }

    public boolean isValid() {
        return session != null;
    }

    public boolean isTimeTrackingOn() {
        return timeTrackingOn;
    }

    public String getSessionStatusChange() {
        return sessionStatusChange;
    }

    public String getSessionStatusPretty() {
        return sessionStatusPretty;
    }

    public boolean isStatusEditable() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isStatusEditable();
    }

    public boolean isSessionEditable() throws Exception  {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isSessionEditable();
    }

    public String getCurrentBrowser() {
        return currentBrowser;
    }

    public List<IssueUI> getRelatedIssues() {
        return visibleRelatedIssues;
    }

    public String getName() {
        return session.getName();
    }

    public Long getId() {
        return session.getId();
    }

    public String getAssignee() {
        return session.getAssignee();
    }

    public String getAssigneeDisplayName() {
        return session.getAssignee();
    }

    public Session.Status getStatus() {
        return session.getStatus();
    }

    public Duration getTimeLogged() {
        return session.getTimeLogged();
    }

    public DateTime getTimeStarted() {
        return session.getTimeCreated();
    }

    public Project getRelatedProject() {
        return session.getRelatedProject();
    }

    public Long getRelatedProjectId() {
        return session.getRelatedProject().getId();
    }

    public String getRelatedProjectKey() {
        return session.getRelatedProject().getKey();
    }

    public boolean hasAdditionalInfo() {
        return StringUtils.isEmpty(session.getAdditionalInfo().trim());
    }

    public List<SessionActivityItem> getSessionActivity() {
        return sessionActivity;
    }

    public String getTimeLoggedString() {
        return timeLoggedString;
    }

    public String getAdditionalInfoHtml() {
        return additionalInfoHtml;
    }

    public String getEstimatedTimeSpent() {
        return estimatedTimeSpent;
    }

    public boolean isCanCreateNote() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isCanCreateNote();
    }

    public List<IssueUI> getIssuesRaised() {
        return visibleRaisedIssues;
    }

    public List<IssueUI> getSubTasksRaised() {
        return visibleRaisedSubTasks;
    }

    public int getIssuesRaisedCount() {
        return visibleRaisedIssues.size() + visibleRaisedSubTasks.size();
    }

    public String getSessionReturnURL() {
        return sessionReturnURL;
    }

    public List<ParticipantUI> getParticipants() {
        return participants;
    }

    public boolean isCanJoin() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isCanJoin();
    }

    public boolean isIsJoined() throws Exception {
        throw new Exception("This method need to be implemented");
     //   return displayHelper.isJoined();
    }

    public boolean isShared() {
        return session.isShared();
    }

    public boolean isHasActive() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isHasActive();
    }

    public boolean isShowPauseWarning() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isHasActive() && displayHelper.isStarted();
    }

    public boolean isShowParticipants() {
        return !getParticipants().isEmpty();
    }

    public boolean isCanCreateSession() throws Exception {
        throw new Exception("This method need to be implemented");
     //   return displayHelper.isCanCreateSession();
    }

    public boolean isAssignee() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isAssignee();
    }

    public String getRelatedProjectName() {
        return relatedProjectName;
    }

    public boolean isComplete() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isComplete();
    }

    public boolean isCreated() throws Exception {
        throw new Exception("This method need to be implemented");
     //   return displayHelper.isCreated();
    }

    public String getAdditionalInfoRaw() {
        return additionalInfoRaw;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getDateCompleted() {
        return dateCompleted;
    }

    public List<IssueUI> getPossibleTimetracking() {
        return possibleTimetracking;
    }

    public boolean hasIssuesToLink() {
        return !visibleRelatedIssues.isEmpty();
    }

    public boolean hasIssuesToTimeTrack() {
        return !possibleTimetracking.isEmpty();
    }

    public String getRaisedIssueNavLink() {
        return raisedIssueNavLink;
    }

    // This is bad. We don't want to expose the raw session. FateNotes require this though.
    public Session getSession() {
        return session;
    }

    public boolean isShowInvite() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isShowInvite();
    }

    public boolean isCanAssign() throws Exception {
        throw new Exception("This method need to be implemented");
    //    return displayHelper.isCanAssign();
    }
}
