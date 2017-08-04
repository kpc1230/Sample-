package com.atlassian.excalibur.service.controller;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.service.controller.ServiceOutcomeImpl;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.core.util.thumbnail.Thumbnail;
import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.base.Predicate;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.List;

public interface SessionController {
    public static final String SERVICE = "excalibur-sessioncontroller";

    /**
     * @param creator           - Creator of the session
     * @param assigneeName      - Initial assignee of the session. if null/blank then will default to creator
     * @param name              - of the session
     * @param relatedProjectKey - related project key. Mandatory.
     * @param relatedIssueKeys  - related issues.
     * @param additionalInfo    - additional info
     * @param shared            - is shared?
     * @param defaultTemplateId - default tempalte id
     */
    public CreateResult validateCreate(ApplicationUser creator, String assigneeName, String name, String relatedProjectKey, List<String> relatedIssueKeys,
                                       String additionalInfo, boolean shared, String defaultTemplateId);

    public SessionResult create(CreateResult result);

    public CloneResult validateClone(String sessionId, String newName, ApplicationUser user);

    public SessionResult clone(CloneResult result);

    public UpdateResult validateJoinSession(String sessionId, ApplicationUser user);

    public UpdateResult validateLeaveSession(String sessionId, ApplicationUser user);

    public UpdateResult validateEditAdditionalInfoSession(String sessionId, ApplicationUser user, String additionalInfo);

    public UpdateResult validateShareSession(String sessionId, ApplicationUser user);

    public UpdateResult validateUnshareSession(String sessionId, ApplicationUser user);

    public UpdateResult validateStartSession(ApplicationUser user, Session session);

    public UpdateResult validatePauseSession(ApplicationUser user, Session session);

    public UpdateResult validateCompleteSession(ApplicationUser user, String sessionId, Duration timeLogged);

    public UpdateResult validateAssignSession(String sessionId, ApplicationUser assigner, String assignee);

    public UpdateResult validateAddRaisedIssue(Session session, IssueEvent issue);

    public UpdateResult validateAddRaisedIssues(ApplicationUser updater, Session session, List<String> issueKeys);

    public UpdateResult validateRemoveRaisedIssue(ApplicationUser updater, String sessionId, String issueKey);

    public UpdateResult validateAddAttachment(Session session, IssueEvent issueEvent, Attachment attachment, Thumbnail thumbnail);

    public UpdateResult validateUpdate(ApplicationUser updater, Session newSession, List<String> relatedIssues);

    public SessionResult update(UpdateResult result);

    public DeleteResult validateDelete(ApplicationUser deleter, String sessionId);

    public SessionResult delete(DeleteResult result);

    /**
     * Gets a session for a ApplicationUser based on the id
     *
     * @return session is null if the session does not exist
     */
    public SessionResult getSessionWithoutNotes(Long id);

    public SessionResult getSessionWithoutNotes(String id);

    /**
     * Load in notes for the given Session
     *
     * @param session to get notes for
     */
    public Session loadNotesForSession(Session session);

    /**
     * Calculate the duration spent on a session based on the started / paused information
     */
    public Duration calculateEstimatedTimeSpentOnSession(LightSession session);

    public Duration calculateEstimatedTimeSpentOnSession(Session session);

    /**
     * Gets the currently active session for a user, or null otherwise.
     *
     * @param user NonNull ApplicationUser to get the active session for.
     */
    public SessionResult getActiveSession(ApplicationUser user);

    public Long getActiveSessionId(ApplicationUser user);

    /**
     * Get all the shared sessions which are related to a particular user
     *
     * @param user       ApplicationUser we are interested in
     * @param startIndex the pagination start index
     * @param size       the pagination size
     * @return List of sessions related to that project
     */
    public List<Session> getSharedSessionsForUser(ApplicationUser user, int startIndex, int size);

    /**
     * Get all the sessions which are related to a particular issue
     *
     * @param issue           Issue we are interested in
     * @param filterPredicate a predicate to filter the results with
     * @param startIndex      the pagination start index
     * @param size            the pagination size
     * @return List of sessions related to that issue
     */
    public List<Session> getSessionsForIssueWithoutNotes(Issue issue, Predicate<IndexedSession> filterPredicate, int startIndex, int size);

    /**
     * Get all the sessions which a ApplicationUser owns with the active session first.
     *
     * @param user            ApplicationUser we are interested in
     * @param filterPredicate a predicate to filter the results with
     * @param startIndex      the pagination start index
     * @param size            the pagination size
     * @return List of sessions owned by that user
     */
    public List<Session> getSessionsForUserNoNotesActiveFirst(final ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size);


    /**
     * Get all the sessions which a ApplicationUser owns
     *
     * @param user            ApplicationUser we are interested in
     * @param filterPredicate a predicate to filter the results with
     * @param startIndex      the pagination start index
     * @param size            the pagination size
     * @return List of sessions owned by that user
     */
    public List<Session> getSessionsForUserWithoutNotes(ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size);

    /**
     * Get all the sessions which are related to a particular project
     *
     * @param project         Project we are interested in
     * @param filterPredicate a predicate to filter the results with
     * @param startIndex      the pagination start index
     * @param size            the pagination size
     * @return List of sessions related to that project
     */
    public List<Session> getSessionsForProjectWithoutNotes(Project project, Predicate<IndexedSession> filterPredicate, int startIndex, int size);

    public static class CreateResult extends SessionValidationResult {

        public CreateResult(ErrorCollection errorCollection, Session session) {
            super(errorCollection, session);
        }
    }

    public static class CloneResult extends CreateResult {

        public CloneResult(ErrorCollection errorCollection, Session session) {
            super(errorCollection, session);
        }

        public CloneResult(CreateResult result) {
            super(result.getErrorCollection(), result.getSession());
        }
    }

    public static class UpdateResult extends SessionValidationResult {
        private final DeactivateResult deactivateResult;
        private final boolean isActivate;
        private final boolean isDeactivate;
        private final ApplicationUser relatedUser;
        private List<ApplicationUser> leavers;
        private List<Object> events;

        public UpdateResult(ErrorCollection errorCollection, Session session) {
            super(errorCollection, session);
            this.deactivateResult = null;
            this.relatedUser = null;
            this.isActivate = false;
            this.isDeactivate = false;
            this.leavers = new ArrayList<ApplicationUser>();
            this.events = new ArrayList<Object>();
        }

        public UpdateResult(ErrorCollection errorCollection, Session session, List<ApplicationUser> leavers) {
            super(errorCollection, session);
            this.deactivateResult = null;
            this.relatedUser = null;
            this.isActivate = false;
            this.isDeactivate = false;
            this.leavers = leavers;
            this.events = new ArrayList<Object>();
        }

        public UpdateResult(ErrorCollection errorCollection, Session session, DeactivateResult leaveResult, boolean isActivate, boolean isDeactivate) {
            super(errorCollection, session);
            this.deactivateResult = leaveResult;
            this.relatedUser = null;
            this.isActivate = isActivate;
            this.isDeactivate = isDeactivate;
            this.events = new ArrayList<Object>();
            this.leavers = new ArrayList<ApplicationUser>();
        }

        public UpdateResult(UpdateResult result, DeactivateResult leaveResult, ApplicationUser relatedUser, boolean isActivate, boolean isDeactivate) {
            super(result.getErrorCollection(), result.getSession());
            this.deactivateResult = leaveResult;
            this.relatedUser = relatedUser;
            this.isActivate = isActivate;
            this.isDeactivate = isDeactivate;
            this.events = result.getEvents();
            this.leavers = result.getLeavers();
        }

        DeactivateResult getDeactivateResult() {
            return deactivateResult;
        }

        boolean isActivate() {
            return isActivate;
        }

        boolean isDeactivate() {
            return isDeactivate;
        }

        boolean isSpecialUpdate() {
            return isActivate && isDeactivate;
        }

        ApplicationUser getUser() {
            return relatedUser;
        }

        void addEvent(Object event) {
            events.add(event);
        }

        List<Object> getEvents() {
            return events;
        }

        List<ApplicationUser> getLeavers() {
            return leavers;
        }
    }

    static class DeactivateResult extends SessionValidationResult {
        private List<ApplicationUser> leavers;
        private List<Object> events;

        public DeactivateResult(ErrorCollection errorCollection, Session session) {
            super(errorCollection, session);
        }

        public DeactivateResult(UpdateResult result, ApplicationUser leaver) {
            super(result.getErrorCollection(), result.getSession());
            this.leavers = result.getLeavers();
            leavers.add(leaver);
            this.events = result.getEvents();
        }

        public DeactivateResult(UpdateResult result, List<ApplicationUser> leavers) {
            super(result.getErrorCollection(), result.getSession());
            this.leavers = leavers;
            this.events = result.getEvents();
        }

        public DeactivateResult(DeactivateResult result, Session session) {
            super(result.getErrorCollection(), session);
            this.leavers = result.getLeavers();
            this.events = result.events;
        }

        public void addLeaver(ApplicationUser leaver) {
            leavers.add(leaver);
        }

        public List<ApplicationUser> getLeavers() {
            return leavers;
        }

        public void addEvent(Object object) {
            this.events.add(object);
        }

        public List<Object> getEvents() {
            return this.events;
        }
    }

    public static class DeleteResult extends SessionValidationResult {
        private final ApplicationUser user;

        public DeleteResult(ErrorCollection errorCollection, Session session, ApplicationUser user) {
            super(errorCollection, session);

            this.user = user;
        }

        public ApplicationUser getUser() {
            return user;
        }
    }

    public static class SessionValidationResult extends SessionResult {
        public SessionValidationResult(ErrorCollection errorCollection, Session session) {
            super(errorCollection, session);
        }
    }

    public static class SessionResult extends ServiceOutcomeImpl<Session> {
        public SessionResult(ErrorCollection errorCollection, Session session) {
            super(errorCollection, session);
        }

        /**
         * Convenience method that returns a new ServiceOutcomeImpl instance containing no errors, and with the provided
         * returned value.
         *
         * @param returnedValue the returned value
         * @return a new ServiceOutcomeImpl
         */
        public static SessionResult ok(Session returnedValue) {
            return new SessionResult(new ErrorCollection(), returnedValue);
        }

        public Session getSession() {
            return getReturnedValue();
        }
    }

    /*****************
     * LIGHT SESSIONS
     *****************/

    public LightSession getLightSession(Long id);

    public LightSession getLightSession(String id);

    public List<LightSession> getLightSessionsForUserActiveFirst(final ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size);

    public List<LightSession> getLightSessionsForProject(final Project project, int startIndex, int size);

    public List<LightSession> getLightSessionsForProject(final Project project, Predicate<IndexedSession> filterPredicate, int startIndex, int size);

    public List<LightSession> getLightSessionsForUser(final ApplicationUser user, Predicate<IndexedSession> filterPredicate, int startIndex, int size);

    public List<LightSession> getSharedLightSessionsForUser(ApplicationUser user, int startIndex, int size);

    public List<LightSession> getAllVisibleLightSessions(ApplicationUser user, int startIndex, int size);

    /**
     * Takes in filters and returns all the visible sessions that satisfy the filters. Assumes the filters have no duplicates.
     *
     * @param userNameFilters   - List of usernames
     * @param projectIdFilters- List of project ids
     * @param statusFilters     - List of statuses
     */
    public List<LightSession> getAllVisibleLightSessionsFiltered(final ApplicationUser currentUser, int startIndex, int size, List<String> userNameFilters,
                                                                 List<Long> projectIdFilters, List<Status> statusFilters, String sortField, boolean ascending, String searchTerm);

    public int getSessionCount(ApplicationUser user);

    public int getFilteredSessionCount(final ApplicationUser currentUser, List<String> userNameFilters, List<Long> projectIdFilters, List<Status> statusFilters,
                                       String searchTerm);

    /*******************
     * NON-SESSION LOADS
     *******************/
    public List<Project> getAllRelatedProjects(ApplicationUser user);

    public List<ApplicationUser> getAllAssignees(ApplicationUser user);
}
