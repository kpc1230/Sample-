package it.com.atlassian.bonfire.toolbar;

import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTabPanel;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTestSessionsTab;
import com.atlassian.bonfire.pageobjects.session.CaptureSessionOwnerPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.test.categories.OnDemandAcceptanceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Temporarily ignoring those tests as they are very flaky.
 * https://jdog.jira-dev.com/browse/BONDEV-435
 */
@Category(OnDemandAcceptanceTest.class)
@Ignore("TODO: Fix flaky tests")
public class CaptureSessionAcceptanceTest extends BaseJiraWebTest {
    private static final String SOFTWARE_PROJECT_KEY = "CSETST";
    private static final String SOFTWARE_PROJECT_NAME = "Test Project";
    private static final String SESSION_NAME = "This is a test session.";
    private static final String SESSION_NAME_SHARED = "This is a test session. SHARED";
    private static final String SESSION_NAME_EDIT = "I just edited the session name";
    private static final String SESSION_NAME_EDIT_SHARED = "I just edited the session name SHARED";
    private static final String SESSION_ADDITIONAL_INFO = "This is the additional info for a test session";

    private static final String SOFTWARE_PROJECT_TYPE = "software";

    @Before
    public void beforeTest() throws JSONException {
        ProjectDeleteHelper.deleteProjectSilently(jira.backdoor(), SOFTWARE_PROJECT_KEY);
        jira.backdoor().project().addProject(SOFTWARE_PROJECT_NAME, SOFTWARE_PROJECT_KEY, "admin", SOFTWARE_PROJECT_TYPE);

        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
        CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();
        Poller.waitUntilTrue("Create button is missing", sessionsTab.isCreateButtonPresent());
        assertEquals(0, sessionsTab.testSessionCount());
        sessionsTab = sessionsTab.clickCreateSessionButton().addName(SESSION_NAME).addAdditionalInfo(SESSION_ADDITIONAL_INFO).submitForm();
        assertEquals(1, sessionsTab.testSessionCount());

        // HACK: go to the session page to trigger callout dialog as it influences edit dialog later(see callout.js for the info)
        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
    }

    @After
    public void afterTest() throws Exception {
        ProjectDeleteHelper.deleteProjectSilently(jira.backdoor(), SOFTWARE_PROJECT_KEY);
    }

    @Test
    @LoginAs(admin = true)
    public void testSessionVisitPage() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Name is Wrong", SESSION_NAME_SHARED, session.getSessionName());
    }

    @Test
    @LoginAs(admin = true)
    public void testSessionStatusChange() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        // NOTE:
        // This test stays on the same page on each action to avoid race conditions in activity stream

        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Status is Wrong", "CREATED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "STARTED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session = session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "PAUSED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session = session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "STARTED", session.getCurrentStatus());
    }

    @Test
    @LoginAs(admin = true)
    public void testEditSession() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Name is Wrong", SESSION_NAME_SHARED, session.getSessionName());
        session = session.clickEditSession().editName(SESSION_NAME_EDIT).submitEdit();
        Poller.waitUntilEquals("Test Session Name is Wrong", SESSION_NAME_EDIT_SHARED, session.getSessionName());
    }

    @Test
    @LoginAs(admin = true)
    public void testCompleteSession() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Status is Wrong", "CREATED", session.getCurrentStatus());
        Poller.waitUntilTrue("Change button is missing", session.isChangeStatusButtonPresent());

        session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "STARTED", session.getCurrentStatus());
        Poller.waitUntilTrue("Complete button is missing", session.isCompleteSessionButtonPresent());

        session = session.clickCompleteSession().submitComplete();
        Poller.waitUntilEquals("Test Session Status is Wrong", "COMPLETED", session.getCurrentStatus());
    }

    @Test
    @LoginAs(admin = true)
    public void testDeleteSession() {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
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

    private <P extends Page> P navigateToPage(Class<P> pageClass, Object... args) {
        return jira.visit(pageClass, args);
    }
}
