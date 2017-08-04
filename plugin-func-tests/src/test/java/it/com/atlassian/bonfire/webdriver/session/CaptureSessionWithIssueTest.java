package it.com.atlassian.bonfire.webdriver.session;

import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTabPanel;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTestSessionsTab;
import com.atlassian.bonfire.pageobjects.session.CaptureSessionOwnerPage;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.elements.query.Poller;
import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Restore("empty.xml")
public class CaptureSessionWithIssueTest extends AbstractBonfireWebTest {
    private static final String PROJECT_KEY = "ONE";
    private static final String ISSUE_KEY = "ONE-1";
    private static final String SESSION_NAME = "This is a test session.";
    private static final String SESSION_NAME_SHARED = "This is a test session. SHARED";
    private static final String SESSION_NAME_EDIT = "I just edited the session name";
    private static final String SESSION_NAME_EDIT_SHARED = "I just edited the session name SHARED";
    private static final String SESSION_ADDITIONAL_INFO = "This is the additional info for a test session";

    @Before
    public void beforeTest() throws JSONException {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();
        Poller.waitUntilTrue("Create button is missing", sessionsTab.isCreateButtonPresent());
        assertEquals(0, sessionsTab.testSessionCount());
        sessionsTab = sessionsTab.clickCreateSessionButton().addName(SESSION_NAME).addRelatedIssue(ISSUE_KEY).addAdditionalInfo(SESSION_ADDITIONAL_INFO).submitForm();
        assertEquals(1, sessionsTab.testSessionCount());

        // HACK: go to the session page to trigger callout dialog as it influences edit dialog later(see callout.js for the info)
        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
    }

    @Test
    public void testSessionVisitPage() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        final CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Name is Wrong", SESSION_NAME_SHARED, session.getSessionName());
    }

    @Test
    public void testSessionStatusChange() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        // NOTE:
        // This test stays on the same page on each action to avoid race conditions in activity stream

        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Status is Wrong", "CREATED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session = session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "STARTED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session = session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "PAUSED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session = session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "STARTED", session.getCurrentStatus());
    }

    @Test
    public void testEditSession() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Name is Wrong", SESSION_NAME_SHARED, session.getSessionName());
        session = session.clickEditSession().editName(SESSION_NAME_EDIT).submitEdit();
        Poller.waitUntilEquals("Test Session Name is Wrong", SESSION_NAME_EDIT_SHARED, session.getSessionName());
        Poller.waitUntilTrue("Related issue must be visible", session.isRelatedIssueVisible());
        session = session.clickEditSession().clearRelatedIssue().submitEdit();
        Poller.waitUntilFalse("Related issue must be hidden", session.isRelatedIssueVisible());
        session = session.clickEditSession().addRelatedIssueEdit(ISSUE_KEY).submitEdit();
        Poller.waitUntilTrue("Related issue must be visible", session.isRelatedIssueVisible());
    }

    @Test
    public void testCompleteSession() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Status is Wrong", "CREATED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session = session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "STARTED", session.getCurrentStatus());
        Poller.waitUntilTrue("Complete button is missing", session.isCompleteSessionButtonPresent());

        session = session.clickCompleteSession().submitComplete();
        Poller.waitUntilEquals("Test Session Status is Wrong", "COMPLETED", session.getCurrentStatus());
    }

    @Test
    public void testDeleteSession() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();
        Poller.waitUntilTrue("Create button is missing", sessionsTab.isCreateButtonPresent());

        final CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilTrue("More Actions button is missing", session.isMoreActionsVisible());
        session.clickMoreActions();
        Poller.waitUntilTrue("Delete Test Session button is missing", session.isDeleteTestSession());
        final CaptureProjectTabPanel projectTabPanel = session.clickDeleteTestSession().submitDelete();
        assertNotNull(projectTabPanel);
    }
}
