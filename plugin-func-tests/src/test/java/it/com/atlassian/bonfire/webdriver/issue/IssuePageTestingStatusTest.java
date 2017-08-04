package it.com.atlassian.bonfire.webdriver.issue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.bonfire.pageobjects.issue.CaptureIssuePage;
import cloud.atlassian.rdbms.schema.test.util.StandaloneRdbmsSchemaService;
import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;
import util.CaptureStandaloneRdbmsSchemaServiceTools;

import static com.atlassian.bonfire.features.CaptureFeatureFlags.*;
import static org.junit.Assert.assertEquals;

@Restore("capture-testingstatus-searcher.xml")
public class IssuePageTestingStatusTest extends AbstractBonfireWebTest {
    public static final String SESSION_NAME = "TMP";

    /*  Data in restored file:
        Issue    Key    Status on IssuePage Test Sessions   RaisedDuring   Env Data
        10000  | HSP1 |    Complete        |  TS1         |    -          |    -
        10001  | HSP2 |    Complete        |  TS1         |    -          |    -
        10004  | HSP5 |    In progress     |  TS2         |    -          |    -
        10005  | HSP6 |    In progress     |  TS2         |    -          |    -
        10002  | HSP3 |    Incomplete      |  TS1, TS3    |    -          |    -
        10003  | HSP4 |    In progress     |  TS2, TS3    |    -          |    -
        10006  | HSP7 |    In progress     |  TS4, TS5    |    -          |    -
        10007  | HSP8 |    Not started     |     -        |    -          |    -
        10008  | HSP9 |    Not started     |     -        |    -          |    -
        10009  | HSP10|    Not started     |     -        |    -          |    -
        10100  | HSP11|    Not started     |     -        |   TS2         |    -
        10101  | HSP12|    Not started     |     -        |   TS2         |    -
        10102  | HSP13|        -           |     -        |    -          |    -
        10103  | HSP14|    Not started     |     -        |    -          |    +
        10104  | HSP15|    Not started     |     -        |   TS2         |    +

        Test Session  Status
        TS1         | Completed
        TS2         | Started
        TS3         | Created
        TS4         | Paused
        TS5         | Created
        TS6         | Completed
        TS6         | Created
    */

    private static final StandaloneRdbmsSchemaService rdbmsSchemaService = CaptureStandaloneRdbmsSchemaServiceTools
            .getStandaloneRdbmsSchemaService();


    @BeforeClass
    public static void beforeClass() {
        rdbmsSchemaService.dropAllTables();

    }

    @Before
    public void before() {
        rdbmsSchemaService.performMigrations();
        rdbmsSchemaService.wipeAllTables();
        jira.backdoor().featureFlags().disableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
    }

    @AfterClass
    public static void afterClass() {
        rdbmsSchemaService.dropAllTables();
    }

    @Test
    public void testTestingStatusIsUpdatedWithFlagsDisabled() throws Exception {
        jira.backdoor().featureFlags().disableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
        jira.backdoor().featureFlags().disableBool(TESTING_STATUS_UPDATE_IN_DB.asFlag().getFeatureKey());
        verifyTestingStatusIsUpdatedForIssueWithSingleTestSession();
        verifyTestingStatusIsUpdatedForIssueWithManyTestSession();
    }

    @Test
    public void testTestingStatusIsUpdatedInDatabaseMode() throws Exception {
        jira.backdoor().featureFlags().enableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
        jira.backdoor().featureFlags().enableBool(TESTING_STATUS_UPDATE_IN_DB.asFlag().getFeatureKey());
        verifyTestingStatusIsUpdatedForIssueWithSingleTestSession();
        verifyTestingStatusIsUpdatedForIssueWithManyTestSession();
    }

    @Test
    public void testTestingStatusIsUpdatedInBothPlaces() throws Exception {
        //Update data in DB, but read from old place
        jira.backdoor().featureFlags().disableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
        jira.backdoor().featureFlags().enableBool(TESTING_STATUS_UPDATE_IN_DB.asFlag().getFeatureKey());
        verifyTestingStatusIsUpdatedForIssueWithSingleTestSession();
        verifyTestingStatusIsUpdatedForIssueWithManyTestSession();
    }

    public void verifyTestingStatusIsUpdatedForIssueWithSingleTestSession() throws Exception {
        String issue = "HSP-10";
        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, issue);

        issuePage.openCreateSessionDialog().addName(SESSION_NAME).submitForm();
        assertEquals(issuePage.getTestingStatus(), "Not started");

        issuePage.startSession(SESSION_NAME);
        assertEquals(issuePage.getTestingStatus(), "In progress");

        issuePage.pauseSession(SESSION_NAME);
        assertEquals(issuePage.getTestingStatus(), "In progress");

        issuePage.openCompleteSessionDialog(SESSION_NAME).submitCompleteToIssuePage();
        assertEquals(issuePage.getTestingStatus(), "Complete");

        issuePage.openDeleteSessionDialog(SESSION_NAME).submitDeleteOnIssuePage();
    }

    public void verifyTestingStatusIsUpdatedForIssueWithManyTestSession() throws Exception {
        String issue = "HSP-10";

        String sessionOne = "S1";
        String sessionTwo = "S2";
        String lastSession = "S3";

        final CaptureIssuePage issuePage = jira.visit(CaptureIssuePage.class, issue);

        issuePage.openCreateSessionDialog().addName(sessionOne).submitForm();
        issuePage.openCreateSessionDialog().addName(sessionTwo).submitForm();
        assertEquals(issuePage.getTestingStatus(), "Not started");

        issuePage.startSession(sessionOne);
        assertEquals(issuePage.getTestingStatus(), "In progress");

        issuePage.startSession(sessionTwo);
        assertEquals(issuePage.getTestingStatus(), "In progress");

        issuePage.pauseSession(sessionTwo);
        assertEquals(issuePage.getTestingStatus(), "In progress");

        issuePage.openCompleteSessionDialog(sessionOne).submitCompleteToIssuePage();
        assertEquals(issuePage.getTestingStatus(), "In progress");

        issuePage.openCompleteSessionDialog(sessionTwo).submitCompleteToIssuePage();
        assertEquals(issuePage.getTestingStatus(), "Complete");

        issuePage.openCreateSessionDialog().addName(lastSession).submitForm();
        assertEquals(issuePage.getTestingStatus(), "Incomplete");

        issuePage.startSession(lastSession);
        assertEquals(issuePage.getTestingStatus(), "In progress");

        issuePage.openDeleteSessionDialog(lastSession).submitDeleteOnIssuePage();
        assertEquals(issuePage.getTestingStatus(), "Complete");
    }

    @Test
    public void testReadingTestingStatusFromDBAfterMigration() {
        jira.backdoor().featureFlags().enableBool(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey());
        verifyTestingStatusOnIssuePageIsCorrect();
    }

    @Test
    public void testReadingTestingStatus() {
        verifyTestingStatusOnIssuePageIsCorrect();
    }

    public void verifyTestingStatusOnIssuePageIsCorrect(){
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
        assertIssueTestingStatus("HSP-12", "Not started");
        assertTestingStatusPanelIsNotVisible("HSP-13");
        assertIssueTestingStatus("HSP-14", "Not started");
        assertIssueTestingStatus("HSP-15", "Not started");
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
