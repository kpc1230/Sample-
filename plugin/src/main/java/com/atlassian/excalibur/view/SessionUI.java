package com.atlassian.excalibur.view;

import com.atlassian.bonfire.util.model.SessionDisplayHelper;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.model.SessionActivityItem;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.List;

/**
 * This class represents everything in test-session-view.vm
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

    private SessionDisplayHelper displayHelper;

    // Constructor used for the test-session-view.vm - ugly beast
    public SessionUI(Session session, List<SessionActivityItem> sessionActivity, Duration estimatedTimeSpent, ExcaliburWebUtil excaliburWebUtil,
                     String currentBrowser, String sessionStatusPretty, String sessionStatusChange, String sessionReturnURL, List<ParticipantUI> participants,
                     List<IssueUI> visibleRelatedIssues, List<IssueUI> possibleTimetracking, String raisedIssueNavLink, List<IssueUI> visibleRaisedIssues,
                     List<IssueUI> visibleRaisedSubTasks, boolean timeTrackingOn, SessionDisplayHelper displayHelper) {
        this.session = session;
        this.sessionActivity = sessionActivity;
        this.timeLoggedString = excaliburWebUtil.formatTimeSpentWJira(session.getTimeLogged());
        this.additionalInfoRaw = session.getAdditionalInfo();
        this.additionalInfoHtml = excaliburWebUtil.renderWikiContent(session.getAdditionalInfo());
        this.estimatedTimeSpent = excaliburWebUtil.formatShortTimeSpent(estimatedTimeSpent);
        this.relatedProjectName = session.getRelatedProject().getName();
        this.visibleRelatedIssues = visibleRelatedIssues;
        this.possibleTimetracking = possibleTimetracking;
        this.currentBrowser = currentBrowser;
        this.sessionStatusPretty = sessionStatusPretty;
        this.sessionStatusChange = sessionStatusChange;
        this.sessionReturnURL = excaliburWebUtil.htmlEncode(sessionReturnURL);
        this.participants = participants;
        this.dateCreated = excaliburWebUtil.formatDateTime(session.getTimeCreated());
        this.dateCompleted = session.getTimeFinished() != null ? excaliburWebUtil.formatDateTime(session.getTimeFinished()) : excaliburWebUtil
                .getText("not.specified");
        this.raisedIssueNavLink = raisedIssueNavLink;
        this.visibleRaisedIssues = visibleRaisedIssues;
        this.visibleRaisedSubTasks = visibleRaisedSubTasks;
        this.displayHelper = displayHelper;
        this.timeTrackingOn = timeTrackingOn;
    }

    public SessionUI(Session session, ExcaliburWebUtil excaliburWebUtil) {
        this.session = session;
        this.timeLoggedString = excaliburWebUtil.formatTimeSpentWJira(session.getTimeLogged());
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

    public boolean isStatusEditable() {
        return displayHelper.isStatusEditable();
    }

    public boolean isSessionEditable() {
        return displayHelper.isSessionEditable();
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

    public ApplicationUser getAssignee() {
        return session.getAssignee();
    }

    public String getAssigneeDisplayName() {
        return session.getAssignee().getDisplayName();
    }

    public Status getStatus() {
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

    public boolean isCanCreateNote() {
        return displayHelper.isCanCreateNote();
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

    public boolean isCanJoin() {
        return displayHelper.isCanJoin();
    }

    public boolean isIsJoined() {
        return displayHelper.isJoined();
    }

    public boolean isShared() {
        return session.isShared();
    }

    public boolean isHasActive() {
        return displayHelper.isHasActive();
    }

    public boolean isShowPauseWarning() {
        return displayHelper.isHasActive() && displayHelper.isStarted();
    }

    public boolean isShowParticipants() {
        return !getParticipants().isEmpty();
    }

    public boolean isCanCreateSession() {
        return displayHelper.isCanCreateSession();
    }

    public boolean isAssignee() {
        return displayHelper.isAssignee();
    }

    public String getRelatedProjectName() {
        return relatedProjectName;
    }

    public boolean isComplete() {
        return displayHelper.isComplete();
    }

    public boolean isCreated() {
        return displayHelper.isCreated();
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

    public boolean isShowInvite() {
        return displayHelper.isShowInvite();
    }

    public boolean isCanAssign() {
        return displayHelper.isCanAssign();
    }
}
