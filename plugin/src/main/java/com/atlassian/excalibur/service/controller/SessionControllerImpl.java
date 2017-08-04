package com.atlassian.excalibur.service.controller;

import com.atlassian.bonfire.comparator.UserNameComparator;
import com.atlassian.bonfire.events.*;
import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.predicates.*;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.*;
import com.atlassian.bonfire.util.LightSessionUtils;
import com.atlassian.borrowed.greenhopper.customfield.CustomFieldService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.excalibur.index.comparators.*;
import com.atlassian.excalibur.index.predicates.*;
import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Participant;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.model.SessionBuilder;
import com.atlassian.excalibur.service.dao.IdDao;
import com.atlassian.excalibur.service.dao.SessionDao;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comparator.ProjectNameComparator;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service(SessionControllerImpl.SERVICE)
public class SessionControllerImpl implements SessionController {
    @JIRAResource
    private EventPublisher eventPublisher;

    @Resource(name = SessionDao.SERVICE)
    private SessionDao sessionDao;

    @Resource(name = NoteController.SERVICE)
    private NoteController noteController;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService bonfireUserService;

    @Resource(name = BonfireJiraHelperService.SERVICE)
    private BonfireJiraHelperService bonfireJiraHelperService;

    @Resource(name = LightSessionUtils.SERVICE)
    private LightSessionUtils lightSessionUtils;

    @Resource(name = CustomFieldService.SERVICE)
    private CustomFieldService customFieldService;

    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil webUtil;

    @JIRAResource
    private ProjectManager jiraProjectManager;

    @JIRAResource
    private IssueService issueService;

    private final Logger log = Logger.getLogger(this.getClass());

    @Override
    public CloneResult validateClone(String sessionId, String newName, ApplicationUser user) {
        LightSession ls = getLightSession(sessionId);
        if (ls == null) {
            ErrorCollection errorCollection = new ErrorCollection();
            errorCollection.addError(i18n.getText("session.invalid.long"));
            return new CloneResult(errorCollection, null);
        }
        List<String> issueKeys = lightSessionUtils.getRelatedIssueKeys(ls);
        CreateResult result = validateCreate(user, ls.getAssignee().getName(), newName, ls.getRelatedProject().getKey(), issueKeys,
                ls.getAdditionalInfo(), ls.isShared(), ls.getDefaultTemplateId());
        return new CloneResult(result);
    }

    @Override
    public SessionResult clone(CloneResult result) {
        SessionResult sessionResult = create(result);
        eventPublisher.publish(new CloneSessionEvent(sessionResult.getSession()));
        return sessionResult;
    }

    @Override
    public CreateResult validateCreate(ApplicationUser creator, String assigneeName, String name, String relatedProjectKey, List<String> relatedIssueKeys,
                                       String additionalInfo, boolean shared, String defaultTemplateId) {
        ErrorCollection errorCollection = new ErrorCollection();
        // Validate related project
        Project relatedProject = bonfireJiraHelperService.getAndValidateProject(creator, relatedProjectKey, errorCollection);
        // Validate the related issues
        List<Issue> relatedIssues = validateAndGetIssueKeys(errorCollection, creator, relatedIssueKeys, relatedProject);

        // Validate the assignee
        ApplicationUser assignee;
        if (StringUtils.isNotBlank(assigneeName)) {
            assignee = bonfireUserService.getUser(assigneeName);
            if (assignee == null) {
                errorCollection.addError(i18n.getText("session.assignee.not.exist", assignee));
            }
        } else {
            assignee = creator;
        }

        // Check that the name is not empty and within bounds
        if (name != null) {
            if (name.trim().isEmpty()) {
                errorCollection.addError(i18n.getText("session.name.empty"));
            }

            if (name.length() > BonfireConstants.SESSION_NAME_LENGTH_LIMIT) {
                errorCollection.addError(i18n.getText("session.name.exceed.limit", name.length(), BonfireConstants.SESSION_NAME_LENGTH_LIMIT));
            }
        }

        // Check that the additional info is within the length bounds
        if (additionalInfo != null && additionalInfo.length() > BonfireConstants.ADDITIONAL_INFO_LENGTH_LIMIT) {
            errorCollection.addError(i18n.getText("session.additionalInfo.exceed.limit", additionalInfo.length(),
                    BonfireConstants.ADDITIONAL_INFO_LENGTH_LIMIT));
        }

        if (creator == null || assignee == null) {
            errorCollection.addError(i18n.getText("session.null.fields"));
        } else {
            if (relatedProject != null) {
                // Check that the creator and assignee have assign issue permissions in the project
                if (!bonfirePermissionService.canCreateSession(creator, relatedProject)) {
                    errorCollection.addError(i18n.getText("session.creator.fail.permissions"));
                }

                if (!bonfirePermissionService.canBeAssignedSession(assignee, relatedProject)) {
                    errorCollection.addError(i18n.getText("session.assignee.fail.permissions", assignee.getName()));
                }
            }
        }

        if (errorCollection.hasErrors()) {
            return new CreateResult(errorCollection, null);
        }

        SessionBuilder sessionBuilder = new SessionBuilder(idDao.genNextId(), webUtil);
        sessionBuilder.setCreator(creator);
        sessionBuilder.setStatus(Status.CREATED);
        sessionBuilder.setAssignee(creator, assignee);
        sessionBuilder.setName(name);
        sessionBuilder.setTimeCreated(new DateTime());
        sessionBuilder.setAdditionalInfo(additionalInfo);
        sessionBuilder.setShared(shared);

        sessionBuilder.setRelatedIssues(relatedIssues);
        sessionBuilder.setRelatedProject(relatedProject);
        sessionBuilder.setDefaultTemplateId(defaultTemplateId);

        Session createdSession = sessionBuilder.build();

        // Not sure if this will work
        return new CreateResult(new ErrorCollection(), createdSession);
    }

    @Override
    public SessionResult create(CreateResult result) {
        if (!result.isValid()) {
            return result;
        }

        Session createdSession = result.getSession();
        sessionDao.save(createdSession);

        eventPublisher.publish(new CreateSessionEvent(createdSession));

        return SessionResult.ok(createdSession);
    }

    @Override
    public UpdateResult validateJoinSession(String sessionId, ApplicationUser user) {
        try {
            Long longSessionId = Long.parseLong(sessionId);
            return _validateJoinSession(longSessionId, user);
        } catch (NumberFormatException e) {
            return new UpdateResult(new ErrorCollection(i18n.getText("session.invalid.id", sessionId)), null);
        }
    }

    private UpdateResult _validateJoinSession(Long sessionId, ApplicationUser user) {
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            return new UpdateResult(sessionResult.getErrorCollection(), sessionResult.getSession());
        }

        Session session = sessionResult.getSession();
        ErrorCollection errorCollection = new ErrorCollection();
        DeactivateResult deactivateResult = null;
        if (session != null && user != null) {
            if (!session.isShared()) {
                errorCollection.addError(i18n.getText("session.join.not.shared", session.getName()));
            }
            if (!Status.STARTED.equals(session.getStatus())) {
                errorCollection.addError(i18n.getText("session.join.not.started", session.getName()));
            }
            if (!bonfirePermissionService.canJoinSession(user, session)) {
                errorCollection.addError(i18n.getText("session.join.no.permission", session.getName()));
            }
            // Deactivate current active session
            SessionResult activeSessionResult = getActiveSession(user);
            if (activeSessionResult.isValid()) {
                deactivateResult = _validateDeactivateSession(activeSessionResult.getSession(), user);
                if (!deactivateResult.isValid()) {
                    errorCollection.addAllErrors(deactivateResult.getErrorCollection());
                }
            }
            session = new SessionBuilder(session, webUtil).addParticipantJoined(user).build();
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, session);
        }

        return new UpdateResult(_validateUpdate(user, session), deactivateResult, user, true, false);
    }

    @Override
    public UpdateResult validateLeaveSession(String sessionId, ApplicationUser user) {
        DeactivateResult leaveResult = _validateDeactivateSession(sessionId, user);
        return new UpdateResult(leaveResult.getErrorCollection(), leaveResult.getSession(), leaveResult, false, true);
    }

    @Override
    public UpdateResult validateEditAdditionalInfoSession(String sessionId, ApplicationUser user, String additionalInfo) {
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            return new UpdateResult(sessionResult.getErrorCollection(), sessionResult.getSession());
        }
        SessionBuilder sb = new SessionBuilder(sessionResult.getSession(), webUtil).setAdditionalInfo(additionalInfo);
        return _validateEditUpdate(user, sb.build());
    }

    @Override
    public UpdateResult validateShareSession(String sessionId, ApplicationUser user) {
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            return new UpdateResult(sessionResult.getErrorCollection(), sessionResult.getSession());
        }
        SessionBuilder sb = new SessionBuilder(sessionResult.getSession(), webUtil).setShared(true);
        return _validateEditUpdate(user, sb.build());
    }

    @Override
    public UpdateResult validateUnshareSession(String sessionId, ApplicationUser user) {
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            return new UpdateResult(sessionResult.getErrorCollection(), sessionResult.getSession());
        }
        SessionBuilder sb = new SessionBuilder(sessionResult.getSession(), webUtil).setShared(false);
        return _validateEditUpdate(user, sb.build());
    }

    @Override
    public UpdateResult validateStartSession(ApplicationUser user, Session session) {
        DeactivateResult deactivateResult = null;
        // Deactivate current active session
        SessionResult activeSessionResult = getActiveSession(user);
        if (activeSessionResult.isValid()) {
            deactivateResult = _validateDeactivateSession(activeSessionResult.getSession(), user);
            if (!deactivateResult.isValid()) {
                return new UpdateResult(deactivateResult.getErrorCollection(), session);
            }
        }
        session = new SessionBuilder(session, webUtil).setStatus(Status.STARTED).build();
        UpdateResult updateResult = new UpdateResult(_validateEditUpdate(user, session), deactivateResult, user, true, false);
        updateResult.addEvent(new SessionStatusChangedEvent(session));
        return updateResult;
    }

    @Override
    public UpdateResult validatePauseSession(ApplicationUser user, Session session) {
        DeactivateResult pauseResult = _validateDeactivateSession(session.getId(), user);
        return new UpdateResult(pauseResult.getErrorCollection(), pauseResult.getSession(), pauseResult, false, true);
    }

    @Override
    public UpdateResult validateCompleteSession(ApplicationUser user, String sessionId, Duration timeLogged) {
        try {
            Long longSessionId = Long.parseLong(sessionId);
            DeactivateResult completeResult = _validateDeactivateSession(longSessionId, user, Status.COMPLETED, timeLogged);
            if (!completeResult.isValid()) {
                return new UpdateResult(completeResult.getErrorCollection(), null);
            }
            completeResult.addEvent(new CompleteSessionEvent(completeResult.getSession()));
            UpdateResult result = new UpdateResult(completeResult.getErrorCollection(), completeResult.getSession(), completeResult, false, true);
            return result;
        } catch (NumberFormatException e) {
            return new UpdateResult(new ErrorCollection(i18n.getText("session.invalid.id", sessionId)), null);
        }
    }

    private DeactivateResult _validateDeactivateSession(String sessionId, ApplicationUser user) {
        try {
            Long longSessionId = Long.parseLong(sessionId);
            return _validateDeactivateSession(longSessionId, user);
        } catch (NumberFormatException e) {
            return new DeactivateResult(new ErrorCollection(i18n.getText("session.invalid.id", sessionId)), null);
        }
    }

    private DeactivateResult _validateDeactivateSession(Long sessionId, ApplicationUser user) {
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            return new DeactivateResult(sessionResult.getErrorCollection(), sessionResult.getSession());
        }

        Session session = sessionResult.getSession();

        return _validateDeactivateSession(session, user, Status.PAUSED, null);
    }

    private DeactivateResult _validateDeactivateSession(Session session, ApplicationUser user) {
        return _validateDeactivateSession(session, user, Status.PAUSED, null);
    }

    private DeactivateResult _validateDeactivateSession(Session session) {
        return _validateDeactivateSession(session, session.getAssignee(), Status.PAUSED, null);
    }

    private DeactivateResult _validateDeactivateSession(Long sessionId, ApplicationUser user, Status status, Duration timeLogged) {
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            return new DeactivateResult(sessionResult.getErrorCollection(), sessionResult.getSession());
        }

        Session session = sessionResult.getSession();

        return _validateDeactivateSession(session, user, status, timeLogged);
    }

    private DeactivateResult _validateDeactivateSession(Session session, ApplicationUser user, Status status, Duration timeLogged) {
        if (session != null) {
            // Pause if it is assigned to me
            if (session.getAssignee().equals(user)) {
                List<ApplicationUser> leavingUsers = new ArrayList<ApplicationUser>();
                SessionBuilder sb = new SessionBuilder(session, webUtil);
                for (Participant p : Iterables.filter(session.getParticipants(), new ActiveParticipantPredicate())) {
                    sb.addParticipantLeft(p.getUser());
                    leavingUsers.add(p.getUser());
                }
                // If this is my active session then I want to leave it
                if (session.getId().equals(getActiveSessionId(user))) {
                    leavingUsers.add(user);
                }
                sb.setStatus(status);
                sb.setTimeLogged(timeLogged);
                session = sb.build();
                DeactivateResult deactivateResult = new DeactivateResult(_validateUpdate(user, session), leavingUsers);
                deactivateResult.addEvent(new SessionStatusChangedEvent(session));
                return deactivateResult;
            }
            // Just leave if it isn't
            else if (Iterables.any(session.getParticipants(), new UserIsParticipantPredicate(user))) {
                session = new SessionBuilder(session, webUtil).addParticipantLeft(user).build();
            }
        }
        return new DeactivateResult(_validateUpdate(user, session), user);
    }

    @Override
    public UpdateResult validateAssignSession(String sessionId, ApplicationUser assigner, String assignee) {
        try {
            Long longSessionId = Long.valueOf(sessionId);
            ApplicationUser assigneeObj = bonfireUserService.getUser(assignee);
            if (assigneeObj == null) {
                return new UpdateResult(new ErrorCollection(i18n.getText("session.assignee.not.exist", assignee)), null);
            }
            return _validateAssignSession(longSessionId, assigner, assigneeObj);
        } catch (NumberFormatException e) {
            return new UpdateResult(new ErrorCollection(i18n.getText("session.invalid.id", sessionId)), null);
        }
    }

    private UpdateResult _validateAssignSession(Long sessionId, ApplicationUser assigner, ApplicationUser assignee) {
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            return new UpdateResult(sessionResult.getErrorCollection(), sessionResult.getSession());
        }

        Session session = sessionResult.getSession();

        DeactivateResult pauseResult = null;
        if (session != null) {
            if (!bonfirePermissionService.canBeAssignedSession(assignee, session.getRelatedProject())) {
                return new UpdateResult(new ErrorCollection(i18n.getText("validation.service.user.not.assignable", assignee.getDisplayName())),
                        session);
            }
            // If the session that is to be assigned is started, then pause it
            if (Status.STARTED.equals(session.getStatus())) {
                // Pause for current user
                pauseResult = _validateDeactivateSession(session);
                if (!pauseResult.isValid()) {
                    return new UpdateResult(pauseResult.getErrorCollection(), pauseResult.getSession());
                }
                // Assign to assignee
                pauseResult = new DeactivateResult(pauseResult, new SessionBuilder(pauseResult.getSession(), webUtil).setAssignee(assigner, assignee)
                        .build());
                pauseResult.addEvent(new AssignSessionEvent(session, assigner));
                UpdateResult result = new UpdateResult(_validateUpdate(assigner, session), pauseResult, null, false, true);

                return result;
            }
            // Assign to assignee
            session = new SessionBuilder(session, webUtil).setAssignee(assigner, assignee).build();
        }
        UpdateResult result = _validateUpdate(assigner, session);
        result.addEvent(new AssignSessionEvent(session, assigner));
        return result;
    }

    @Override
    public UpdateResult validateAddRaisedIssue(Session session, IssueEvent issueEvent) {
        ApplicationUser updater = getEventUser(issueEvent);
        Session updatedSession = new SessionBuilder(session, webUtil).addRaisedIssue(issueEvent.getIssue(), new DateTime(issueEvent.getTime()),
                updater).build();
        // NOTE event is being thrown elsewhere...
        return _validateUpdate(updater, updatedSession);
    }

    @Override
    public UpdateResult validateAddRaisedIssues(ApplicationUser updater, Session session, List<String> issueKeys) {
        ErrorCollection errorCollection = new ErrorCollection();
        SessionBuilder sb = new SessionBuilder(session, webUtil);
        List<Issue> addedIssues = Lists.newArrayList();
        for (String s : issueKeys) {
            if (StringUtils.isNotBlank(s)) {
                IssueResult result = issueService.getIssue(updater, s);
                if (!result.isValid()) {
                    errorCollection.addError(i18n.getText("session.issue.key.invalid", s));
                } else {
                    Issue issue = result.getIssue();
                    if (!session.getIssuesRaised().contains(issue)) {
                        sb.addRaisedIssue(issue, new DateTime(), updater);
                        addedIssues.add(issue);
                    }
                }
            }
        }
        Session updatedSession = sb.build();
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, updatedSession);
        }
        UpdateResult updateResult = _validateUpdate(updater, updatedSession);
        for (Issue i : addedIssues) {
            updateResult.addEvent(new IssueRaisedInSessionEvent(updatedSession, i, updater));
        }
        return updateResult;
    }

    @Override
    public UpdateResult validateRemoveRaisedIssue(ApplicationUser updater, String sessionId, String issueKey) {
        ErrorCollection errorCollection = new ErrorCollection();
        IssueResult result = issueService.getIssue(updater, issueKey);
        if (!result.isValid()) {
            errorCollection.addError(i18n.getText("session.issue.key.invalid", issueKey));
        }
        Issue issue = result.getIssue();
        SessionResult sessionResult = getSessionWithoutNotes(sessionId);
        if (!sessionResult.isValid()) {
            errorCollection.addAllErrors(sessionResult.getErrorCollection());
        }
        Session session = sessionResult.getSession();
        if (!bonfirePermissionService.canUnraiseIssueInSession(updater, result.getIssue())) {
            errorCollection.addError(i18n.getText("validation.service.unraise.permission"));
        }
        if (!session.getIssuesRaised().contains(issue)) {
            errorCollection.addError(i18n.getText("validation.service.unraise.notexist"));
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, null);
        }
        Session updatedSession = new SessionBuilder(session, webUtil).removeRaisedIssue(issue, new DateTime(), updater).build();
        UpdateResult updateResult = _validateUpdate(updater, updatedSession);
        updateResult.addEvent(new IssueUnraisedInSessionEvent(updatedSession, issue, updater));
        return updateResult;
    }

    @Override
    public UpdateResult validateAddAttachment(Session session, IssueEvent issueEvent, Attachment attachment, Thumbnail thumbnail) {
        ApplicationUser updater = getEventUser(issueEvent);
        Session updatedSession = new SessionBuilder(session, webUtil).addAttachment(new DateTime(issueEvent.getTime()), updater,
                issueEvent.getIssue(), attachment, thumbnail).build();
        return _validateUpdate(updater, updatedSession);
    }

    @Override
    public UpdateResult validateUpdate(ApplicationUser updater, Session newSession, List<String> relatedIssues) {
        ErrorCollection errorCollection = new ErrorCollection();
        List<Issue> newRelatedIssues = validateAndGetIssueKeys(errorCollection, updater, relatedIssues, newSession.getRelatedProject());
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, newSession);
        } else {
            // Add the related issues that the user does not have permission to change
            newRelatedIssues.addAll(getInvisibleRelatedIssues(updater, newSession));
            newSession = new SessionBuilder(newSession, webUtil).setRelatedIssues(newRelatedIssues).build();
        }
        return _validateEditUpdate(updater, newSession);
    }

    // An update that actually edits the session
    private UpdateResult _validateEditUpdate(ApplicationUser updater, Session newSession) {
        if (updater != null || newSession != null) {
            // Check that the updater has edit permissions on the session given
            if (!bonfirePermissionService.canEditSession(updater, newSession)) {
                return new UpdateResult(new ErrorCollection(i18n.getText("session.update.not.editable")), newSession);
            }
        }

        return _validateUpdate(updater, newSession);
    }

    private UpdateResult _validateUpdate(ApplicationUser updater, Session newSession) {
        ErrorCollection errorCollection = new ErrorCollection();
        Session loadedSession = null;
        // Validation
        // Check inputs not null
        if (updater == null || newSession == null) {
            errorCollection.addError(i18n.getText("session.null.fields"));
        } else {
            // Check that the name is not empty
            if (newSession.getName().trim().isEmpty()) {
                errorCollection.addError(i18n.getText("session.name.empty"));
            }

            if (newSession.getName().length() > BonfireConstants.SESSION_NAME_LENGTH_LIMIT) {
                errorCollection.addError(i18n.getText("session.name.exceed.limit", newSession.getName().length(),
                        BonfireConstants.SESSION_NAME_LENGTH_LIMIT));
            }

            if (newSession.getAdditionalInfo() != null && newSession.getAdditionalInfo().length() > BonfireConstants.ADDITIONAL_INFO_LENGTH_LIMIT) {
                errorCollection.addError(i18n.getText("session.additionalInfo.exceed.limit", newSession.getAdditionalInfo().length(),
                        BonfireConstants.ADDITIONAL_INFO_LENGTH_LIMIT));
            }
            if (newSession.getRelatedIssues().size() > BonfireConstants.RELATED_ISSUES_LIMIT) {
                errorCollection.addError(i18n.getText("session.relatedissues.exceed", newSession.getRelatedIssues().size(),
                        BonfireConstants.RELATED_ISSUES_LIMIT));
            }
            // ANYTHING PAST THIS POINT IS A SANITY CHECK
            // Load in the session to check that it still exists
            loadedSession = sessionDao.load(newSession.getId());
            if (loadedSession == null) {
                errorCollection.addError(i18n.getText("session.invalid", newSession.getId()));
            } else {
                // If the session status is changed, we better have been allowed to do that!
                if (!newSession.getStatus().equals(loadedSession.getStatus())
                        && !bonfirePermissionService.canEditSessionStatus(updater, loadedSession)) {
                    errorCollection.addError(i18n.getText("session.status.change.permissions.violation"));
                }
                // If the assignee has changed, then the new session should be paused
                if (!newSession.getAssignee().equals(loadedSession.getAssignee()) && newSession.getStatus().equals(Status.STARTED)) {
                    errorCollection.addError(i18n.getText("session.assigning.active.session.violation"));
                }
                // Status can't go backwards from COMPLETED
                if (loadedSession.getStatus().equals(Status.COMPLETED) && !newSession.getStatus().equals(Status.COMPLETED)) {
                    errorCollection.addError(i18n.getText("session.reopen.completed.violation"));
                }
                // Check that certain fields haven't changed - creator + time created (paranoid check)
                if (!newSession.getCreator().equals(loadedSession.getCreator())) {
                    errorCollection.addError(i18n.getText("session.change.creator.violation"));
                }
                if (!loadedSession.getTimeCreated().equals(newSession.getTimeCreated())) {
                    errorCollection.addError(i18n.getText("session.change.timecreated.violation"));
                }
            }
            // If we just completed the session, we want to update the time finished
            if (!newSession.getStatus().equals(loadedSession.getStatus()) && newSession.getStatus().equals(Status.COMPLETED)) {
                if (newSession.getTimeFinished() == null) {
                    SessionBuilder sb = new SessionBuilder(newSession, webUtil);
                    sb.setTimeFinished(new DateTime());
                    newSession = sb.build();
                } else {
                    errorCollection.addError(i18n.getText("session.change.timefinished.violation"));
                }
            }
        }
        if (errorCollection.hasErrors()) {
            return new UpdateResult(errorCollection, newSession);
        }

        // At this point, all is good
        List<ApplicationUser> leavers = new ArrayList<ApplicationUser>();
        // If we aren't shared, we wanna kick out all the current users
        if (!newSession.isShared()) {
            SessionBuilder sb = new SessionBuilder(newSession, webUtil);
            for (Participant p : Iterables.filter(newSession.getParticipants(), new ActiveParticipantPredicate())) {
                sb.addParticipantLeft(p.getUser());
                leavers.add(p.getUser());
            }
            newSession = sb.build();
        }
        UpdateResult toReturn = new UpdateResult(new ErrorCollection(), newSession, leavers);
        // At this point none of these values should be null thanks to the validation above
        toReturn.addEvent(new UpdateSessionEvent(updater, loadedSession, newSession));
        return toReturn;
    }

    @Override
    public SessionResult update(UpdateResult result) {
        if (!result.isValid()) {
            return result;
        }
        // If this update has users leaving a session, then do the leave first
        if (result.getDeactivateResult() != null) {
            saveDeactivateSession(result.getDeactivateResult());
        }
        // If the session is a 'deactivate' then it will have been saved already
        if (!result.isDeactivate()) {
            saveUpdatedSession(result);
        }

        return result;
    }

    private void saveUpdatedSession(UpdateResult result) {
        // Update depending on flags
        if (result.isActivate()) {
            sessionDao.setActiveSession(result.getUser(), result.getSession().getId());
        }
        save(result.getSession(), result.getLeavers(), result.getEvents());
    }

    private void saveDeactivateSession(DeactivateResult result) {
        if (!result.isValid()) {
            return;
        }
        save(result.getSession(), result.getLeavers(), result.getEvents());
    }

    private void save(Session session, List<ApplicationUser> leavers, List<Object> events) {
        sessionDao.save(session);
        for (ApplicationUser l : leavers) {
            sessionDao.clearActiveSession(l);
        }
        publishUpdateEvents(events);
        reindexRelatedIssues(session);
    }

    // Make sure the testing status JQL is still correct
    private void reindexRelatedIssues(Session session) {
        for (Issue issue : session.getRelatedIssues()) {
            // Fail silently if it didn't work
            try {
                customFieldService.reindexSingleIssue(issue);
            } catch (IndexException e) {
                // Shh.. we wanna be silent
            }
        }
    }

    private void publishUpdateEvents(List<Object> events) {
        for (Object event : events) {
            eventPublisher.publish(event);
        }
    }

    @Override
    public DeleteResult validateDelete(ApplicationUser deleter, String sessionId) {
        try {
            Long longSessionId = Long.valueOf(sessionId);
            // Check session still exists
            Session loadedSession = sessionDao.load(longSessionId);
            // Check permissions
            if (loadedSession == null) {
                return new DeleteResult(new ErrorCollection(i18n.getText("session.delete.already")), loadedSession, deleter);
            } else if (!bonfirePermissionService.canEditSession(deleter, loadedSession)) {
                return new DeleteResult(new ErrorCollection(i18n.getText("session.delete.permission.fail")), loadedSession, deleter);
            }
            return new DeleteResult(new ErrorCollection(), loadedSession, deleter);
        } catch (NumberFormatException e) {
            return new DeleteResult(new ErrorCollection(i18n.getText("session.invalid.id", sessionId)), null, deleter);
        }
    }

    @Override
    public SessionResult delete(DeleteResult result) {
        if (!result.isValid()) {
            return result;
        }

        // Delete the notes for this session
        Session session = result.getSession();

        NoteController.BulkDeleteResult notesDeleteResult = noteController.validateDeleteNotesForSession(result.getUser(), session);

        noteController.delete(notesDeleteResult);

        // Delete the session itself
        sessionDao.delete(session.getId());
        // Clear it as assignees active session
        if (getActiveSessionId(session.getAssignee()).equals(session.getId())) {
            sessionDao.clearActiveSession(session.getAssignee());
        }
        // Clear it as all the active participants active session
        for (Participant p : Iterables.filter(session.getParticipants(), new ActiveParticipantPredicate())) {
            sessionDao.clearActiveSession(p.getUser());
        }

        eventPublisher.publish(new DeleteSessionEvent(session, result.getUser()));
        return result;
    }

    @Override
    public Duration calculateEstimatedTimeSpentOnSession(LightSession session) {
        Map<DateTime, Session.Status> sessionStatusHistory = lightSessionUtils.getSessionStatusHistory(session);
        return calculateEstimatedTimeSpentOnSession(sessionStatusHistory, session.getId());
    }

    @Override
    public Duration calculateEstimatedTimeSpentOnSession(Session session) {
        Map<DateTime, Session.Status> sessionStatusHistory = session.getSessionStatusHistory();
        return calculateEstimatedTimeSpentOnSession(sessionStatusHistory, session.getId());
    }

    private Duration calculateEstimatedTimeSpentOnSession(Map<DateTime, Session.Status> sessionStatusHistory, Long sessionId) {
        SortedSet<DateTime> timestampSortedKeySet = new TreeSet<DateTime>(sessionStatusHistory.keySet());
        DateTime startTime = null; // Initialise to avoid null reference complaints
        Duration timeSpent = new Duration(0L);

        // NB: we should *always* have a STARTED before a PAUSED by design
        for (DateTime timestamp : timestampSortedKeySet) {
            Session.Status sessionStatus = sessionStatusHistory.get(timestamp);
            switch (sessionStatus) {
                case STARTED:
                    startTime = timestamp;
                    break;
                case PAUSED:
                    // Append the time
                    if (startTime != null) {
                        timeSpent = timeSpent.plus(new Duration(startTime, timestamp));
                        startTime = null;
                    } else {
                        log.warn("Test Session " + sessionId + " : Paused before Started");
                    }
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        // If we're not paused at this point, add time from started to now
        if (startTime != null) {
            timeSpent = timeSpent.plus(new Duration(startTime, new DateTime()));
        }
        return timeSpent;
    }

    // An audit of usages revealed that the errors are always ignored when the active session doesn't exist.
    @Override
    public SessionResult getActiveSession(ApplicationUser user) {
        Long activeSessionId = getActiveSessionId(user);

        if (activeSessionId == -1L) {
            return new SessionResult(new ErrorCollection(i18n.getText("no.active.session")), null);
        }

        Session activeSession = sessionDao.load(activeSessionId);

        if (activeSession == null) {
            log.debug(String.format("Unable to load active session with id: %s", activeSessionId));
            // If the active session doesn't exist then we want to clear it as an active session
            sessionDao.clearActiveSession(user);
            return new SessionResult(new ErrorCollection(i18n.getText("no.active.session")), null);
        }

        return SessionResult.ok(activeSession);
    }

    // If the id obtained from this method is then used to get a session, you are using the wrong method (except the one right above this)
    @Override
    public Long getActiveSessionId(ApplicationUser user) {
        return sessionDao.getActiveSessionId(user);
    }

    @Override
    public SessionResult getSessionWithoutNotes(Long id) {
        if (id == null) {
            return new SessionResult(new ErrorCollection(i18n.getText("session.invalid.long")), null);
        }

        Session session = sessionDao.load(id);

        if (session == null) {
            return new SessionResult(new ErrorCollection(i18n.getText("session.invalid.long")), null);
        }

        return SessionResult.ok(session);
    }

    @Override
    public SessionResult getSessionWithoutNotes(String id) {
        if (id == null) {
            return new SessionResult(new ErrorCollection(i18n.getText("session.invalid.long")), null);
        }

        try {
            Long idNum = Long.valueOf(id);
            return getSessionWithoutNotes(idNum);
        } catch (NumberFormatException e) {
            return new SessionResult(new ErrorCollection(i18n.getText("session.invalid.long")), null);
        }
    }

    @Override
    public List<Session> getSharedSessionsForUser(ApplicationUser user, int startIndex, int size) {
        Predicate<IndexedSession> predicate = Predicates.<IndexedSession>and(new UserSharedSessionPredicate(user, bonfirePermissionService),
                new SessionPermissionPredicate(user, bonfirePermissionService));
        return sessionDao.load(startIndex, size, new IssueIndexedSessionComparator(), predicate);
    }

    @Override
    public List<Session> getSessionsForIssueWithoutNotes(final Issue issue, Predicate<IndexedSession> filterPredicate, int startIndex, int size) {
        return sessionDao.load(startIndex, size, new IssueIndexedSessionComparator(),
                Predicates.<IndexedSession>and(new IssueIndexedSessionPredicate(issue), filterPredicate));
    }

    @Override
    public List<Session> getSessionsForUserNoNotesActiveFirst(final ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size) {
        Long id = getActiveSessionId(user);
        Predicate<IndexedSession> predicate = Predicates.<IndexedSession>and(new UserIndexedSessionPredicate(user), filterPredicate,
                new SessionPermissionPredicate(user, bonfirePermissionService));
        return sessionDao.load(startIndex, size, new UserIndexedSessionComparator(id), predicate);
    }

    @Override
    public List<Session> getSessionsForUserWithoutNotes(final ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size) {

        Predicate<IndexedSession> predicate = Predicates.<IndexedSession>and(new UserIndexedSessionPredicate(user), filterPredicate,
                new SessionPermissionPredicate(user, bonfirePermissionService));
        return sessionDao.load(startIndex, size, new UserIndexedSessionComparator(), predicate);
    }

    @Override
    public List<Session> getSessionsForProjectWithoutNotes(final Project project, Predicate<IndexedSession> filterPredicate, int startIndex, int size) {
        return sessionDao.load(startIndex, size, new ProjectIndexedSessionComparator(),
                Predicates.<IndexedSession>and(new ProjectIndexedSessionPredicate(project), filterPredicate));
    }

    @Override
    public Session loadNotesForSession(Session session) {
        // TODO Can we avoid this with an Iterable in the Session?
        SessionBuilder sb = new SessionBuilder(session, webUtil);
        sb.setSessionNotes(noteController.getNoteIterable(session.getSessionNoteIds()));
        return sb.build();
    }

    // Returns a list of issues this user cannot see
    private List<Issue> getInvisibleRelatedIssues(ApplicationUser user, Session session) {
        List<Issue> invisibileIssues = Lists.newArrayList();
        for (Issue i : session.getRelatedIssues()) {
            if (!bonfirePermissionService.canSeeIssue(user, i)) {
                invisibileIssues.add(i);
            }
        }
        return invisibileIssues;
    }

    private List<Issue> validateAndGetIssueKeys(ErrorCollection errorCollection, ApplicationUser user, List<String> issueKeys, Project relatedProject) {
        List<Issue> relatedIssues = Lists.newArrayList();
        List<String> duplicatePrevention = Lists.newArrayList();
        for (String issueKey : issueKeys) {
            if (!StringUtils.isEmpty(issueKey) && !duplicatePrevention.contains(issueKey)) {
                IssueResult issueResult = issueService.getIssue(user, issueKey);

                if (!issueResult.isValid()) {
                    errorCollection.addError(i18n.getText("session.issue.key.invalid", issueKey));
                } else {
                    Issue relatedIssue = issueResult.getIssue();

                    if (!relatedIssue.getProjectObject().equals(relatedProject)) {
                        errorCollection.addError(i18n.getText("session.issue.key.different.project", issueKey, relatedProject.getKey()));
                    } else {
                        duplicatePrevention.add(issueKey);
                        relatedIssues.add(relatedIssue);
                    }
                }
            }
        }
        if (relatedIssues.size() > BonfireConstants.RELATED_ISSUES_LIMIT) {
            errorCollection.addError(i18n.getText("session.relatedissues.exceed", relatedIssues.size(), BonfireConstants.RELATED_ISSUES_LIMIT));
        }
        return relatedIssues;
    }

    private Predicate<IndexedSession> filteredPredicateBuilder(final ApplicationUser currentUser, List<String> userNameFilters, List<Long> projectIdFilters,
                                                               List<Status> statusFilters, String searchTerm) {
        Predicate<IndexedSession> predicate = new SessionPermissionPredicate(currentUser, bonfirePermissionService);
        if (userNameFilters != null && !userNameFilters.isEmpty()) {
            predicate = Predicates.<IndexedSession>and(predicate, new MultiAssigneeIndexedSessionPredicate(userNameFilters));
        }
        if (projectIdFilters != null && !projectIdFilters.isEmpty()) {
            predicate = Predicates.<IndexedSession>and(predicate, new MultiProjectIndexedSessionPredicate(projectIdFilters));
        }
        if (statusFilters != null && !statusFilters.isEmpty()) {
            predicate = Predicates.<IndexedSession>and(predicate, new MultiStatusIndexedSessionPredicate(statusFilters));
        }
        if (StringUtils.isNotBlank(searchTerm)) {
            predicate = Predicates.<IndexedSession>and(predicate, new SessionNameMatchPredicate(searchTerm));
        }
        return predicate;
    }

    /*****************
     * LIGHT SESSIONS
     *****************/

    @Override
    public LightSession getLightSession(Long id) {
        if (id != null) {
            return sessionDao.lightLoad(id);
        }
        return null;
    }

    @Override
    public LightSession getLightSession(String id) {
        try {
            if (id != null) {
                Long idNum = Long.valueOf(id);
                return getLightSession(idNum);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    @Override
    public List<LightSession> getLightSessionsForUserActiveFirst(final ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size) {
        Long id = getActiveSessionId(user);
        Predicate<IndexedSession> predicate = Predicates.<IndexedSession>and(new UserIndexedSessionPredicate(user), filterPredicate,
                new SessionPermissionPredicate(user, bonfirePermissionService));
        return sessionDao.lightLoad(startIndex, size, new UserIndexedSessionComparator(id), predicate);
    }

    @Override
    public List<LightSession> getSharedLightSessionsForUser(ApplicationUser user, int startIndex, int size) {
        Predicate<IndexedSession> predicate = Predicates.<IndexedSession>and(new UserSharedSessionPredicate(user, bonfirePermissionService),
                new SessionPermissionPredicate(user, bonfirePermissionService));
        return sessionDao.lightLoad(startIndex, size, new IssueIndexedSessionComparator(), predicate);
    }

    @Override
    public List<LightSession> getLightSessionsForProject(final Project project, int startIndex, int size) {
        return sessionDao.lightLoad(startIndex, size, new ProjectIndexedSessionComparator(), new ProjectIndexedSessionPredicate(project));
    }

    @Override
    public List<LightSession> getLightSessionsForProject(final Project project, Predicate<IndexedSession> filterPredicate, int startIndex, int size) {
        return sessionDao.lightLoad(startIndex, size, new ProjectIndexedSessionComparator(),
                Predicates.<IndexedSession>and(new ProjectIndexedSessionPredicate(project), filterPredicate));
    }

    @Override
    public List<LightSession> getLightSessionsForUser(final ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size) {

        Predicate<IndexedSession> predicate = Predicates.<IndexedSession>and(new UserIndexedSessionPredicate(user), filterPredicate,
                new SessionPermissionPredicate(user, bonfirePermissionService));
        return sessionDao.lightLoad(startIndex, size, new UserIndexedSessionComparator(), predicate);
    }

    @Override
    public List<LightSession> getAllVisibleLightSessions(ApplicationUser user, int startIndex, int size) {
        return sessionDao.lightLoad(startIndex, size, new IndexedSessionComparator(), new SessionPermissionPredicate(user,
                bonfirePermissionService));
    }

    @Override
    public List<LightSession> getAllVisibleLightSessionsFiltered(final ApplicationUser currentUser, int startIndex, int size, List<String> userNameFilters,
                                                                 List<Long> projectIdFilters, List<Status> statusFilters, String sortField, boolean ascending, String searchTerm) {
        // Calculate predicate
        Predicate<IndexedSession> predicate = filteredPredicateBuilder(currentUser, userNameFilters, projectIdFilters, statusFilters, searchTerm);
        // Calculate comparator
        Comparator<IndexedSession> comparator;
        if (BonfireConstants.SORTFIELD_CREATED.equals(sortField)) {
            comparator = new TimeCreatedIndexedSessionComparator(ascending);
        } else if (BonfireConstants.SORTFIELD_STATUS.equals(sortField)) {
            comparator = new StatusIndexedSessionComparator(ascending);
        } else if (BonfireConstants.SORTFIELD_SESSION_NAME.equals(sortField)) {
            comparator = new SessionNameIndexedSessionComparator(ascending);
        } else if (BonfireConstants.SORTFIELD_PROJECT.equals(sortField)) {
            comparator = new ProjectNameIndexedSessionComparator(ascending);
        } else if (BonfireConstants.SORTFIELD_ASSIGNEE.equals(sortField)) {
            comparator = new AssigneeNameIndexedSessionComparator(ascending);
        } else if (BonfireConstants.SORTFIELD_SHARED.equals(sortField)) {
            comparator = new SharedIndexedSessionComparator(ascending);
        } else {
            comparator = new UserIndexedSessionComparator();
        }
        return sessionDao.lightLoad(startIndex, size, comparator, predicate);
    }

    @Override
    public int getSessionCount(ApplicationUser user) {
        return sessionDao.getSessionCount(new SessionPermissionPredicate(user, bonfirePermissionService));
    }

    @Override
    public int getFilteredSessionCount(final ApplicationUser currentUser, List<String> userNameFilters, List<Long> projectIdFilters, List<Status> statusFilters,
                                       String searchTerm) {
        // Calculate predicate
        Predicate<IndexedSession> predicate = filteredPredicateBuilder(currentUser, userNameFilters, projectIdFilters, statusFilters, searchTerm);
        return sessionDao.getSessionCount(predicate);
    }

    @Override
    public List<Project> getAllRelatedProjects(ApplicationUser user) {
        List<Project> projects = Lists.newArrayList();
        List<Long> projectIds = sessionDao.getAllRelatedProjects(new SessionPermissionPredicate(user, bonfirePermissionService));
        for (Long l : projectIds) {
            Project project = jiraProjectManager.getProjectObj(l);
            if (project != null && !projects.contains(project)) {
                projects.add(project);
            }
        }
        Collections.sort(projects, new ProjectNameComparator());
        return projects;
    }

    @Override
    public List<ApplicationUser> getAllAssignees(ApplicationUser user) {
        List<ApplicationUser> users = Lists.newArrayList();
        List<String> usernames = sessionDao.getAllAssignees(new SessionPermissionPredicate(user, bonfirePermissionService));
        for (String s : usernames) {
            ApplicationUser loadedUser = bonfireUserService.safeGetUser(s);
            if (loadedUser.isActive() && !users.contains(loadedUser)) {
                users.add(loadedUser);
            }
        }
        Collections.sort(users, new UserNameComparator());
        return users;
    }

    private ApplicationUser getEventUser(IssueEvent event) {
        try {
            final Object user = event.getClass().getMethod("getUser").invoke(event);
            if (user instanceof ApplicationUser) {
                return (ApplicationUser) user;
            } else {
                return ApplicationUsers.from((User) user);
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("IssueEvent should have getUser method", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("IssueEvent should have getUser method", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("IssueEvent should have getUser method", e);
        }
    }
}
