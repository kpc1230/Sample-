package it.com.atlassian.bonfire.webdriver.issue;

import org.junit.Before;
import org.junit.Test;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.bonfire.pageobjects.issue.CaptureIssuePage;
import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;

import static com.atlassian.bonfire.features.CaptureFeatureFlags.TESTING_STATUS_DB_PRIMARY;
import static org.junit.Assert.assertEquals;

public class TestingStatusUpgradeTaskTest extends AbstractBonfireWebTest {

    @Before
    public void setUp() throws Exception {
        jira.backdoor().featureFlags().enableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
        restoreData("capture-testingstatus-upgrade.xml");
    }

    @Test
    public void testUpgradeTaskDoesntModifyExistingData() {
        assertIssueTestingStatus("HSP-1", "Not started");
        assertIssueTestingStatus("HSP-2", "Not started");
    }

    @Test
    public void testUpgradeTaskAddsMissingValues() {
        assertIssueTestingStatus("HSP-3", "Incomplete");
        assertIssueTestingStatus("HSP-4", "In progress");
        assertIssueTestingStatus("HSP-5", "In progress");
        assertIssueTestingStatus("HSP-6", "In progress");
        assertIssueTestingStatus("HSP-7", "In progress");
        assertTestingStatusPanelIsNotVisible("HSP-8");
        assertTestingStatusPanelIsNotVisible("HSP-9");
        assertTestingStatusPanelIsNotVisible("HSP-10");
        assertIssueTestingStatus("HSP-11", "Not started");
        assertIssueTestingStatus("HSP-12", "Not started");
        assertTestingStatusPanelIsNotVisible("HSP-13");
        assertIssueTestingStatus("HSP-14", "Not started");
        assertIssueTestingStatus("HSP-15", "Not started");
    }

    public void verifyTestingStatusOnIssuePageIsCorrect(){
        assertIssueTestingStatus("HSP-12", "Not started");
    }

    public void assertIssueTestingStatus(String issueKey, String status){
        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, issueKey);
        assertEquals(status, issuePage.getTestingStatus());
    }

    public void assertTestingStatusPanelIsNotVisible(String issueKey) {
        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, issueKey);
        Poller.waitUntilFalse("Test session panel shouldn't be visible", issuePage.getCapturePanel().timed().isPresent());
    }
}
