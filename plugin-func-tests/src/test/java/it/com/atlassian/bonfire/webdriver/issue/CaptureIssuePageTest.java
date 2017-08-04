package it.com.atlassian.bonfire.webdriver.issue;

import com.atlassian.bonfire.pageobjects.issue.CaptureIssuePage;
import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.pageobjects.elements.query.Poller;
import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;
import org.junit.After;
import org.junit.Test;

@RestoreOnce("empty.xml")
public class CaptureIssuePageTest extends AbstractBonfireWebTest {
    public static final String SESSION_NAME = "TEST SESSION";
    public static final String ISSUE_KEY = "ONE-1";

    @After
    public void tearDown() throws Exception {
        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, ISSUE_KEY);
        issuePage.openDeleteSessionDialog(SESSION_NAME).submitDeleteOnIssuePage();
        Poller.waitUntilFalse("Session should be correctly removed after test", issuePage.getCapturePanelVisibility());
    }

    @Test
    public void testSessionCanBeCreatedStartedAndCompletedFromIssuePage() throws Exception {
        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, ISSUE_KEY);
        issuePage.openCreateSessionDialog().addName(SESSION_NAME).submitForm();
        issuePage.startSession(SESSION_NAME);
        issuePage.openCompleteSessionDialog(SESSION_NAME).submitCompleteToIssuePage();

        Poller.waitUntilEquals("Session should be in completed state now", "Completed", issuePage.getSessionStatus(SESSION_NAME));
    }
}
