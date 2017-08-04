package it.com.atlassian.bonfire.webdriver.session;

import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTabPanel;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTestSessionsTab;
import com.atlassian.bonfire.pageobjects.session.CaptureSessionOwnerPage;
import com.atlassian.bonfire.pageobjects.session.CaptureSessionParticipantPage;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.elements.query.Poller;
import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Restore("capture-rest-data.xml")
public class CaptureJoinSessionTest extends AbstractBonfireWebTest {
    private static final String PROJECT_KEY = "PEXPRESS";
    private static final String SESSION_NAME = "This is a test session.";
    private static final String SESSION_NAME_SHARED = "This is a test session. SHARED";
    private static final String SESSION_NAME_EDIT = "I just edited the session name";
    private static final String SESSION_NAME_EDIT_SHARED = "I just edited the session name SHARED";
    private static final String SESSION_ADDITIONAL_INFO = "This is the additional info for a test session";
    private static final String SECOND_USER = "fry";



    @Before
    public void beforeTest() throws JSONException {
        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        sessionsTab = sessionsTab.clickCreateSessionButton().addName(SESSION_NAME).addAdditionalInfo(SESSION_ADDITIONAL_INFO).submitForm();
        assertEquals(2, sessionsTab.testSessionCount());

        // HACK: go to the session page to trigger callout dialog as it influences edit dialog later(see callout.js for the info)
        CaptureSessionOwnerPage session = sessionsTab.goToFirstSession();
        session.changeStatus();
        Poller.waitUntilEquals("Test Session Status is Wrong", "STARTED", session.getCurrentStatus());
    }

    @Test
    public void testJoinAndLeaveTestSession() {

        //We need to login as a different user
        jira.logout();
        jira.quickLogin(SECOND_USER, "fry");

        final CaptureProjectTabPanel page = navigateToPage(CaptureProjectTabPanel.class, PROJECT_KEY);
        final CaptureProjectTestSessionsTab sessionsTab = page.gotoSessionTab();

        CaptureSessionParticipantPage session = sessionsTab.goToFirstSession(CaptureSessionParticipantPage.class);
        assertNotNull(session);
        Poller.waitUntilEquals("Test Session Name is Wrong", SESSION_NAME_SHARED, session.getSessionName());

        //Join the test session
        session.joinTestSession();
        Poller.waitUntilTrue("Can not join the test session", session.isLeaveButtonPresent());


        //Leave the test session
        session.leaveTestSession();
        Poller.waitUntilTrue("Change button is missing", session.isJoinButtonPresent());
    }
}
