package com.thed.zephyr.capture.service.web;

import com.thed.zephyr.capture.customfield.BonfireSessionCustomFieldService;
import com.thed.zephyr.capture.predicates.ActiveParticipantPredicate;
import com.thed.zephyr.capture.predicates.InactiveParticipantPredicate;
import com.thed.zephyr.capture.predicates.IssueIsSubTaskPredicate;
import com.thed.zephyr.capture.service.BonfirePermissionService;
import com.thed.zephyr.capture.service.controller.ServiceOutcomeImpl;
import com.thed.zephyr.capture.util.SessionDisplayUtils;
import com.thed.zephyr.capture.util.model.SessionDisplayHelper;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.model.Participant;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.SessionActivityItem;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.view.ActivityStreamFilterUI;
import com.atlassian.excalibur.view.IssueUI;
import com.atlassian.excalibur.view.ParticipantUI;
import com.atlassian.excalibur.view.SessionUI;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.json.JSONObject;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.*;

@Service(SessionUIService.SERVICE)
public class SessionUIService {
    public static final String SERVICE = "bonfire-uicreationservice";

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = SessionDisplayUtils.SERVICE)
    private SessionDisplayUtils displayUtils;

    @Resource
    private IssueManager jiraIssueManager;

    @Resource
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Resource
    private ApplicationProperties jiraApplicationProperties;

    @JIRAResource
    private BuildUtilsInfo jiraBuildUtilsInfo;

    @JIRAResource
    private IssueService issueService;

    /**
     * This returns a SessionUIResult that contains and SessionUI or a set of errors
     *
     * @param user                 the user in play
     * @param sessionId            the id of the session to load
     * @param activityStreamFilter a filter of the sessions activity items
     * @param returnScreen         the return screen information
     * @return a service result
     */
    public SessionUIResult getSessionUI(final ApplicationUser user,
                                        Long sessionId,
                                        ActivityStreamFilterUI activityStreamFilter,
                                        JSONObject returnScreen) {
        ErrorCollection errorCollection = new ErrorCollection();

        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(sessionId);
        errorCollection.addAllErrors(sessionResult.getErrorCollection());

        // If that didn't work, go no further
        if (!sessionResult.isValid()) {
            return new SessionUIResult(errorCollection, null);
        }

        Session session = sessionResult.getSession();
        if (!bonfirePermissionService.canSeeSession(user, session)) {
            // true means this is a permission violation
            return new SessionUIResult(true);
        }

        session = sessionController.loadNotesForSession(session);
        // Current browser
        ExcaliburWebUtil.Browser browser = excaliburWebUtil.detectBrowser(ExecutingHttpRequest.get());
        String browserString = excaliburWebUtil.formatBrowserString(browser);
        // Session status pretty and change
        String sessionStatusPretty = null;
        String sessionStatusChange = null;
        if (Session.Status.PAUSED.equals(session.getStatus())) {
            sessionStatusPretty = getText("session.status.pretty.paused");
            sessionStatusChange = getText("session.status.pretty.desired.resume");
        } else if (Session.Status.STARTED.equals(session.getStatus())) {
            sessionStatusPretty = getText("session.status.pretty.started");
            sessionStatusChange = getText("session.status.pretty.desired.pause");
        } else if (Session.Status.CREATED.equals(session.getStatus())) {
            sessionStatusChange = getText("session.status.pretty.desired.start");
            sessionStatusPretty = getText("session.status.pretty.start");
        } else if (Session.Status.COMPLETED.equals(session.getStatus())) {
            sessionStatusPretty = getText("session.status.pretty.started");
        }
        boolean isTimeTrackingOn = jiraApplicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING);
        List<IssueUI> relatedIssues = Lists.newArrayList();
        List<IssueUI> possibleTimeTracking = Lists.newArrayList();
        if (session.getRelatedIssues() != null && !session.getRelatedIssues().isEmpty()) {
            boolean issueEditableWorkFlow = false;
            for (Issue i : session.getRelatedIssues()) {
                if (bonfirePermissionService.canSeeIssue(user, i)) {
                    IssueUI toAdd = new IssueUI(i, bonfirePermissionService.canUnraiseIssueInSession(user, i));
                    relatedIssues.add(toAdd);
                    if (issueService.isEditable(i, user)) {
                        possibleTimeTracking.add(toAdd);
                        issueEditableWorkFlow = true;
                    }
                }
            }
            isTimeTrackingOn = isTimeTrackingOn && issueEditableWorkFlow;
        }
        List<Issue> allVisibleRaisedIssues = Lists.newArrayList();
        for (Issue i : session.getIssuesRaised()) {
            if (bonfirePermissionService.canSeeIssue(user, i)) {
                allVisibleRaisedIssues.add(i);
            }
        }
        List<IssueUI> visibleRaisedIssues = Lists.newArrayList(Iterables.transform(
                        Iterables.filter(allVisibleRaisedIssues, Predicates.not(new IssueIsSubTaskPredicate())),
                        new Function<Issue, IssueUI>() {
                            @Override
                            public IssueUI apply(final Issue from) {
                                return new IssueUI(from, bonfirePermissionService.canUnraiseIssueInSession(user, from));
                            }
                        }
                )
        );

        List<IssueUI> visibleRaisedSubTasks = Lists.newArrayList(Iterables.transform(
                        Iterables.filter(allVisibleRaisedIssues, new IssueIsSubTaskPredicate()),
                        new Function<Issue, IssueUI>() {
                            @Override
                            public IssueUI apply(final Issue from) {
                                return new IssueUI(from, bonfirePermissionService.canUnraiseIssueInSession(user, from));
                            }
                        }
                )
        );

        // ParticipantUI objects
        List<ParticipantUI> participants = getParticipants(session);

        // Return Screen
        String sessionReturnURL = "/browse/" + session.getRelatedProject().getKey();
        String raisedIssueNavLink = getRaisedInIssueNavUrl(session);

        SessionDisplayHelper displayHelper = displayUtils.getDisplayHelper(user, session);

        SessionUI sessionUI = new SessionUI(session, getSessionActivityItems(session, activityStreamFilter, user),
                sessionController.calculateEstimatedTimeSpentOnSession(session), excaliburWebUtil, browserString, sessionStatusPretty,
                sessionStatusChange, sessionReturnURL, participants, relatedIssues, possibleTimeTracking, raisedIssueNavLink,
                visibleRaisedIssues, visibleRaisedSubTasks, isTimeTrackingOn, displayHelper);
        return new SessionUIResult(errorCollection, sessionUI);
    }

    private List<SessionActivityItem> getSessionActivityItems(final Session session, final ActivityStreamFilterUI activityStreamFilter,
                                                              final ApplicationUser user) {
        Collection<SessionActivityItem> sessionActivityItems = Collections2.filter(session.getSessionActivity(), new Predicate<SessionActivityItem>() {
            public boolean apply(SessionActivityItem sessionActivityItem) {
                boolean passFilter = activityStreamFilter.showItem(session, sessionActivityItem);
                boolean hasPermission = bonfirePermissionService.showActivityItem(user, sessionActivityItem);
                return passFilter && hasPermission;
            }
        });

        return ImmutableList.copyOf(sessionActivityItems);
    }

    private List<ParticipantUI> getParticipants(Session session) {
        List<ParticipantUI> toReturn = new ArrayList<ParticipantUI>();
        Set<ApplicationUser> duplicateDetector = new HashSet<ApplicationUser>();
        // Add the assignee first
        toReturn.add(new ParticipantUI(session.getAssignee(), excaliburWebUtil.getLargeAvatarUrl(session.getAssignee()), true, true));
        duplicateDetector.add(session.getAssignee());
        // Add the active participants first
        for (Participant p : Iterables.filter(session.getParticipants(), new ActiveParticipantPredicate())) {
            getParticipantHelper(duplicateDetector, toReturn, p, true);
        }
        // Add the inactive participants next
        for (Participant p : Iterables.filter(session.getParticipants(), new InactiveParticipantPredicate())) {
            getParticipantHelper(duplicateDetector, toReturn, p, false);
        }
        return toReturn;
    }

    private void getParticipantHelper(Set<ApplicationUser> duplicateDetector, List<ParticipantUI> toReturn, Participant p, boolean isActive) {
        if (!duplicateDetector.contains(p.getUser())) {
            toReturn.add(new ParticipantUI(p.getUser(), excaliburWebUtil.getLargeAvatarUrl(p.getUser()), isActive, false));
            duplicateDetector.add(p.getUser());
        }
    }

    private String getText(String key, Object... params) {
        return jiraAuthenticationContext.getI18nHelper().getText(key, params);
    }


    public static class SessionUIResult extends ServiceOutcomeImpl<SessionUI> {
        private final boolean permissionViolation;

        public SessionUIResult(ErrorCollection errorCollection, @Nullable SessionUI sessionUI) {
            super(errorCollection, sessionUI);
            this.permissionViolation = false;
        }

        public SessionUIResult(boolean permissionViolation) {
            super(null, null);
            this.permissionViolation = permissionViolation;
        }

        public boolean isPermissionViolation() {
            return permissionViolation;
        }
    }

    private String getRaisedInIssueNavUrl(Session session) {
        StringBuilder sb = new StringBuilder();
        sb.append("/secure/IssueNavigator.jspa?reset=true&jqlQuery=cf[");
        sb.append(bonfireSessionCustomFieldService.getRaisedInSessionCustomField().getIdAsLong());
        sb.append("]+=+");
        sb.append(session.getId());
        return sb.toString();
    }
}
