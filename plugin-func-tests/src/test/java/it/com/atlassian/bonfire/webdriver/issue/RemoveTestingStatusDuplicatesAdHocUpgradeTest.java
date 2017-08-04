package it.com.atlassian.bonfire.webdriver.issue;

import java.util.Optional;
import org.junit.Test;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.testkit.client.DataImportControl;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.bonfire.pageobjects.issue.CaptureIssuePage;
import com.atlassian.bonfire.upgradetasks.AdHocTestingStatusDuplicatesRemovalUpgradeTask;

import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;

import static com.atlassian.bonfire.features.CaptureFeatureFlags.TESTING_STATUS_DB_PRIMARY;
import static org.junit.Assert.assertEquals;

@Restore("capture-duplicated-testingstatus.xml")
public class RemoveTestingStatusDuplicatesAdHocUpgradeTest extends AbstractBonfireWebTest {
    private final String upgradeTaskKey = "adHocTestingStatusDuplicatesRemovalUpgradeTask";

    @Test
    public void testTestingStatusValuesAreFixedAfterUpgrade() {
        //Ignoring in Vertigo as Vertigo uses -Dscheduler.noop=true
        if(jira.environmentData().isVertigoMode()) {
            return;
        }

        jira.backdoor().featureFlags().disableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
        assertIssueTestingStatus("HSP-12", "Complete");
        assertIssueTestingStatus("HSP-14", "In progress");
        assertIssueTestingStatus("HSP-15", "In progress");

        jira.backdoor().featureFlags().enableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
        assertIssueTestingStatus("HSP-12", "Not started");
        assertIssueTestingStatus("HSP-14", "Not started");
        assertIssueTestingStatus("HSP-15", "Not started");

        runAdHocUpgrade();
        assertIssueTestingStatus("HSP-12", "Complete");
        assertIssueTestingStatus("HSP-14", "In progress");
        assertIssueTestingStatus("HSP-15", "In progress");

        assertIssueTestingStatus("HSP-1", "Complete");
        assertIssueTestingStatus("HSP-2", "Complete");
        assertIssueTestingStatus("HSP-3", "Incomplete");
        assertIssueTestingStatus("HSP-4", "In progress");
        assertIssueTestingStatus("HSP-5", "In progress");
        assertIssueTestingStatus("HSP-6", "In progress");
        assertIssueTestingStatus("HSP-7", "In progress");
        assertTestingStatusPanelIsNotVisible("HSP-8");
        assertTestingStatusPanelIsNotVisible("HSP-9");
        assertTestingStatusPanelIsNotVisible("HSP-10");
        assertIssueTestingStatus("HSP-11", "Not started");
        assertIssueTestingStatus("HSP-12", "Complete");
        assertTestingStatusPanelIsNotVisible("HSP-13");
    }

    public void runAdHocUpgrade(){
        setAsapLenient(true);

        jira.backdoor().featureFlags().enableBool(AdHocTestingStatusDuplicatesRemovalUpgradeTask.getFlag().getFeatureKey());
        jira.backdoor()
                .getTestkit()
                .adHocUpgrade()
                .runUpgrade(upgradeTaskKey, Optional.of(true));

        setAsapLenient(false);
    }

    private void setAsapLenient(boolean b) {
        if(DataImportControl.isSkipDBInit() || jira.backdoor().dataImport().isSetUp()) {
            jira.backdoor().systemProperties().setProperty("jira.asap.lenient", Boolean.toString(b).toLowerCase());
        }
    }

    public void verifyTestingStatusOnIssuePageIsCorrect(){
        assertIssueTestingStatus("HSP-12", "Not started");
    }

    public void assertIssueTestingStatus(String issueKey, String status){
        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, issueKey);
        assertEquals(issuePage.getTestingStatus(), status);
    }

    public void assertTestingStatusPanelIsNotVisible(String issueKey) {
        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, issueKey);
        Poller.waitUntilFalse("Test session panel shouldn't be visible", issuePage.getCapturePanel().timed().isPresent());
    }
}
