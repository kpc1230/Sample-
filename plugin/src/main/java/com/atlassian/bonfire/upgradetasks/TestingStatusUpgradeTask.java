package com.atlassian.bonfire.upgradetasks;

import java.sql.Connection;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.querydsl.sql.SQLExpressions;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.database.QueryDslAccessor;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.log.clean.LaasLogger;
import com.atlassian.jira.log.clean.LaasLoggerFactory;
import com.atlassian.jira.model.querydsl.QCustomField;
import com.atlassian.jira.model.querydsl.QCustomFieldValue;
import com.atlassian.bonfire.customfield.BonfireMultiSessionCustomFieldService;
import com.atlassian.bonfire.customfield.TestingStatusCustomFieldService;
import com.atlassian.bonfire.service.TestingStatusService;
import cloud.atlassian.upgrade.api.UpgradeContext;
import cloud.atlassian.upgrade.api.UpgradeTask;

import static com.atlassian.bonfire.features.CaptureFeatureFlags.TESTING_STATUS_DB_PRIMARY;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.BONFIRE_MULTI_SESSION;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.BONFIRE_SESSION;
import static com.atlassian.bonfire.web.util.JiraFieldConstants.BONFIRE_TEXT;

@Service(TestingStatusUpgradeTask.SERVICE)
public class TestingStatusUpgradeTask implements UpgradeTask {
    public static final String SERVICE = "bonfire-TestingStatusUpgradeTask";
    private static final LaasLogger LOGGER = LaasLoggerFactory.getLogger(TestingStatusUpgradeTask.class);

    @JIRAResource
    private QueryDslAccessor queryDslAccessor;

    @Resource(name = UpgradeTaskKit.SERVICE)
    private UpgradeTaskKit upgradeTaskKit;

    @Resource
    private TestingStatusService testingStatusService;

    @Resource(name = TestingStatusCustomFieldService.SERVICE)
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService multiSessionCustomFieldService;

    @Resource
    private IssueManager jiraIssueManager;

    @Resource
    private JiraFeatureFlagService featureFlagService;

    @Override
    public int getBuildNumber() {
        return 14;
    }

    @Override
    public String getShortDescription() {
        return "Inserts missing TestingStatus custom field values into database";
    }

    @Override
    public void runUpgrade(UpgradeContext upgradeContext) {

        long start = System.currentTimeMillis();
        LOGGER.privacySafe().info("Starting Upgrade: Inserts missing TestingStatus custom field values into database");

        if (!featureFlagService.getBooleanValue(TESTING_STATUS_DB_PRIMARY.asFlag())) {
            LOGGER.privacySafe().error(TESTING_STATUS_DB_PRIMARY.asFlag().getFeatureKey() + " is disabled");
            return;
        }

        //Those calls will create customfield if they are not already created
        CustomField testingStatusCustomField = testingStatusCustomFieldService.getTestingStatusSessionCustomField();
        multiSessionCustomFieldService.getRelatedToSessionCustomField().getIdAsLong();

        //Get issues with Capture's customfields that don't have TestingStatus values
        List<Long> issuesCaptureCF = getIssuesWithMissingTestingStatusValues(testingStatusCustomField.getIdAsLong());

        List<Issue> foundIssues = jiraIssueManager.getIssueObjects(issuesCaptureCF);
        LOGGER.privacySafe().info("Found " + foundIssues.size() + " from " + issuesCaptureCF.size() + " issues without TestingStatus");

        for(Issue issue : foundIssues) {

            //Try to read value from DB, Capture will insert any missing values
            //Requires Reading feature flag to be enabled, if not this method will calculate TestingStatus value instead of reading from DB
            testingStatusService.getTestingStatus(issue);
        }

        LOGGER.privacySafe().info("Finished upgrade");
        LOGGER.privacySafe().info("Execution time: " + (System.currentTimeMillis() - start) + " ms");

        upgradeTaskKit.markTaskAsDoneViaPS("2.1000.0", TestingStatusUpgradeTask.class);
    }

    @Override
    public void runDowngrade(Connection connection) {
    }

    public List<Long> getIssuesWithMissingTestingStatusValues(Long testingStatusCfId) {
        return queryDslAccessor.executeQuery(dbConnection ->
                dbConnection.newSqlQuery()
                        .distinct()
                        .select(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue)
                        .from(QCustomFieldValue.CUSTOM_FIELD_VALUE)
                        .join(QCustomField.CUSTOM_FIELD)
                        .on(QCustomField.CUSTOM_FIELD.id.eq(QCustomFieldValue.CUSTOM_FIELD_VALUE.customfield)
                        .and(QCustomField.CUSTOM_FIELD.customfieldtypekey
                                .in(BONFIRE_MULTI_SESSION, BONFIRE_SESSION, BONFIRE_TEXT))
                        .and(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue
                                .notIn(SQLExpressions.select(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue)
                                        .from(QCustomFieldValue.CUSTOM_FIELD_VALUE)
                                        .where(QCustomFieldValue.CUSTOM_FIELD_VALUE.customfield.eq(testingStatusCfId)))))
                        .fetch());
    }
}