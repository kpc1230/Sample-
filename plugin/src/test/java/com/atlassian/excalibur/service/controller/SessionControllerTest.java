package com.atlassian.excalibur.service.controller;

import com.atlassian.bonfire.service.BonfireJiraHelperService;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.SessionBuilder;
import com.atlassian.excalibur.service.dao.IdDao;
import com.atlassian.excalibur.service.dao.SessionDao;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Lists;
import org.joda.time.DateTimeUtils;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionControllerTest {
    @Rule
    public final TestRule mockInContainer = MockitoMocksInContainer.forTest(this);

    @Mock
    private ApplicationUser mockUser;

    @Mock
    private Project mockProject;

    @Mock
    private NoteController noteController;

    @Mock
    private SessionDao sessionDao;

    @Mock
    private BonfireJiraHelperService helpService;

    @Mock
    private IdDao idDao;

    @Mock
    private IssueService issueService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private BonfirePermissionService bonfirePermissionService;

    @Mock
    private ExcaliburWebUtil excaliburWebUtil;

    @InjectMocks
    private SessionController sessionController = new SessionControllerImpl();

    @Before
    public void setUp() throws Exception {
        when(idDao.genNextId()).thenReturn(0L);
    }

    @After
    public void tearDown() {
        // Reset back time joda-time millisecond provider after tests
        DateTimeUtils.setCurrentMillisSystem();
    }

    private Session createGenericTestSession(ApplicationUser user, String name) {
        IssueService.IssueResult issueResult = getInvalidIssueResult();

        when(issueService.getIssue(mockUser, "TST")).thenReturn(issueResult);
        when(helpService.getAndValidateProject(eq(mockUser), eq("TST"), (com.atlassian.borrowed.greenhopper.web.ErrorCollection) anyObject()))
                .thenReturn(
                        mockProject);
        when(bonfirePermissionService.canCreateSession(mockUser, mockProject)).thenReturn(true);
        when(bonfirePermissionService.canBeAssignedSession(mockUser, mockProject)).thenReturn(true);
        when(mockUser.getName()).thenReturn("MockUser");
        when(idDao.genNextId()).thenReturn(0L);

        SessionController.CreateResult createResult = sessionController.validateCreate(mockUser, "", "example", "TST", Lists.newArrayList(""), "",
                false, "");
        return sessionController.create(createResult).getSession();
    }

    private IssueService.IssueResult getInvalidIssueResult() {
        ErrorCollection errors = new SimpleErrorCollection();
        errors.addError("ERROR", "PC LOAD LETTER");

        return new IssueService.IssueResult(null, errors);
    }

    @Test
    public void testSessionCreation() {
        SessionBuilder sb = new SessionBuilder(0L, excaliburWebUtil);
        sb.setCreator(mockUser);
        sb.setAssignee(mockUser, mockUser);
        sb.setName("example");
        sb.setStatus(Session.Status.CREATED);
        sb.setRelatedProject(mockProject);

        Session created = createGenericTestSession(mockUser, "example");

        // Fix the time dependent items
        sb.setTimeCreated(created.getTimeCreated());
        sb.setSessionActivity(created.getSessionActivity());
        sb.setSessionStatusHistory(created.getSessionStatusHistory());

        Session expectedSession = sb.build();
        assertTrue(created.equals(expectedSession));
    }

    @Test
    public void testSessionCreationAndDeletion() {
        Session created = createGenericTestSession(mockUser, "example");
        verify(sessionDao).save(created);
        when(sessionDao.load(created.getId())).thenReturn(created);
        when(bonfirePermissionService.canEditSession(mockUser, created)).thenReturn(true);

        SessionController.DeleteResult deleteResult = sessionController.validateDelete(mockUser, String.valueOf(created.getId()));
        sessionController.delete(deleteResult);
        verify(sessionDao).delete(created.getId());
    }

    @Test
    public void testEstimateTimeSpentWhenNotStarted() {
        Session session = createGenericTestSession(mockUser, "Example");
        assertEquals(sessionController.calculateEstimatedTimeSpentOnSession(session).getMillis(), 0L);
    }

    @Test
    public void testEstimateTimeSpentWhenStarted() {
        Session session = createGenericTestSession(mockUser, "Example");
        DateTimeUtils.setCurrentMillisFixed(1L);
        SessionBuilder sb = new SessionBuilder(session, excaliburWebUtil);

        sb.setStatus(Session.Status.STARTED);
        Session startedSession = sb.build();

        DateTimeUtils.setCurrentMillisFixed(2L);
        Duration timeSpentEarlier = sessionController.calculateEstimatedTimeSpentOnSession(startedSession);
        DateTimeUtils.setCurrentMillisFixed(3L);
        Duration timeSpentLater = sessionController.calculateEstimatedTimeSpentOnSession(startedSession);
        assertTrue(timeSpentEarlier.isShorterThan(timeSpentLater));
        assertTrue(timeSpentLater.getMillis() == 2L);
        assertTrue(timeSpentLater.isLongerThan(new Duration(0L)));
    }

    @Test
    public void testEstimateTimeSpentWhenPaused() {
        Session session = createGenericTestSession(mockUser, "Example");
        SessionBuilder sb = new SessionBuilder(session, excaliburWebUtil);

        DateTimeUtils.setCurrentMillisFixed(1L);
        sb.setStatus(Session.Status.STARTED);
        DateTimeUtils.setCurrentMillisFixed(2L);
        sb.setStatus(Session.Status.PAUSED);
        DateTimeUtils.setCurrentMillisFixed(10L);

        assertTrue(sessionController.calculateEstimatedTimeSpentOnSession(sb.build()).getMillis() == 1L);
    }

    @Test
    public void testEstimateTimeSpentMultipleRestarts() {
        Session session = createGenericTestSession(mockUser, "Example");
        SessionBuilder sb = new SessionBuilder(session, excaliburWebUtil);

        DateTimeUtils.setCurrentMillisFixed(1L);
        sb.setStatus(Session.Status.STARTED);
        DateTimeUtils.setCurrentMillisFixed(2L);
        sb.setStatus(Session.Status.PAUSED);

        DateTimeUtils.setCurrentMillisFixed(10L);
        sb.setStatus(Session.Status.STARTED);
        DateTimeUtils.setCurrentMillisFixed(11L);
        sb.setStatus(Session.Status.PAUSED);

        DateTimeUtils.setCurrentMillisFixed(20L);

        assertTrue(sessionController.calculateEstimatedTimeSpentOnSession(sb.build()).getMillis() == 2L);
    }
}