package com.thed.zephyr.capture.upgradetasks;

import java.util.List;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.atlassian.featureflag.client.api.FeatureFlag.BooleanFlag;
import com.atlassian.jira.database.QueryDslAccessor;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.log.clean.LaasLogger;
import com.atlassian.jira.log.clean.LaasLoggerFactory;
import com.atlassian.jira.model.querydsl.QCustomField;
import com.atlassian.jira.model.querydsl.QCustomFieldValue;
import com.atlassian.jira.task.adhoc.AdHocUpgradeDefinition;
import com.atlassian.jira.task.progress.ProgressUpdateWriter;
import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.TestingStatusCustomFieldService;
import com.thed.zephyr.capture.service.TestingStatusService;

public class AdHocTestingStatusDuplicatesRemovalUpgradeTask implements AdHocUpgradeDefinition {
    private static final String TASK_KEY = "adHocTestingStatusDuplicatesRemovalUpgradeTask";
    private static final LaasLogger log = LaasLoggerFactory.getLogger(AdHocTestingStatusDuplicatesRemovalUpgradeTask.class);

    private static final BooleanFlag flag = new BooleanFlag("capture.adhoc.remove.testing.status.duplicates", false);

    private final JiraFeatureFlagService featureFlagService;
    private final QueryDslAccessor queryDslAccessor;
    private final TestingStatusService testingStatusService;
    private final TestingStatusCustomFieldService testingStatusCustomFieldService;
    private final BonfireMultiSessionCustomFieldService multiSessionCustomFieldService;
    private final IssueManager jiraIssueManager;

    public AdHocTestingStatusDuplicatesRemovalUpgradeTask(final JiraFeatureFlagService featureFlagService, final QueryDslAccessor queryDslAccessor,
                                                          final TestingStatusService testingStatusService, final TestingStatusCustomFieldService testingStatusCustomFieldService,
                                                          final BonfireMultiSessionCustomFieldService multiSessionCustomFieldService, final IssueManager jiraIssueManager) {
        this.featureFlagService = featureFlagService;
        this.queryDslAccessor = queryDslAccessor;
        this.testingStatusService = testingStatusService;
        this.testingStatusCustomFieldService = testingStatusCustomFieldService;
        this.multiSessionCustomFieldService = multiSessionCustomFieldService;
        this.jiraIssueManager = jiraIssueManager;
    }

    @Override
    public String getTaskKey() {
        return TASK_KEY;
    }

    @Override
    public UpgradeResult runUpgradeTask(ProgressUpdateWriter taskProgressWriter) {

        long start = System.currentTimeMillis();

        taskProgressWriter.setProgressMessage("Starting Upgrade: remove Testing Status customfield duplicates");
        log.privacySafe().info("Starting Upgrade: remove Testing Status customfield duplicates");

        final CustomField testingStatusCf = testingStatusCustomFieldService.getTestingStatusSessionCustomField();
        final Long multisessionCfId = multiSessionCustomFieldService.getRelatedToSessionCustomField().getIdAsLong();

        List<Long> duplicatedIssues = getIssuesWithDuplicatedTestingStatusValues(multisessionCfId);

        log.privacySafe().info("Updating TestingStatus value for " + duplicatedIssues.size() + " issues");

        long mid = System.currentTimeMillis();
        log.privacySafe().info("Preparation time: " + (mid-start) + " ms");

        int i = 1;
        for (Long issueId : duplicatedIssues) {
            MutableIssue issue = jiraIssueManager.getIssueObject(issueId);

            TestingStatusService.TestingStatus testingStatus = testingStatusService.calculateTestingStatus(issue);

            //This will remove old values and insert calculated one
            testingStatusCf.getCustomFieldType()
                    .updateValue(testingStatusCf, issue, testingStatus.getI18nKey());

            log.privacySafe().info("Updated " + i + " issues");
            taskProgressWriter.setProgressAsRatio(i, duplicatedIssues.size());
            i++;
        }
        log.privacySafe().info("Execution time: " + (System.currentTimeMillis()-mid) + " ms");

        log.privacySafe().info("Finished upgrade");
        return new UpgradeResult(UpgradeStatus.COMPLETED);
    }

    public List<Long> getIssuesWithDuplicatedTestingStatusValues(Long multisessionCfId) {
        SQLQuery<Long> issuesWithRelatedTo = SQLExpressions.select(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue)
                .from(QCustomFieldValue.CUSTOM_FIELD_VALUE)
                .where(QCustomFieldValue.CUSTOM_FIELD_VALUE.customfield.eq(multisessionCfId));

        return queryDslAccessor.executeQuery(dbConnection ->
                dbConnection.newSqlQuery()
                        .select(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue)
                        .distinct()
                        .from(QCustomFieldValue.CUSTOM_FIELD_VALUE)
                        .join(QCustomField.CUSTOM_FIELD)
                        .on(QCustomField.CUSTOM_FIELD.id.eq(QCustomFieldValue.CUSTOM_FIELD_VALUE.customfield)
                                .and(QCustomField.CUSTOM_FIELD.customfieldtypekey
                                        .in("com.atlassian.bonfire.plugin:bonfire-text", "com.atlassian.bonfire.plugin:bonfire-session-cft"))
                                .and(QCustomFieldValue.CUSTOM_FIELD_VALUE.issue.in(issuesWithRelatedTo)))
                        .fetch());
    }

    @Override
    public String getDescription() {
        return "Unicorn only. Remove duplicates values in TestingStatus customfield";
    }

    @Override
    public boolean isEnabled() {
        return featureFlagService.getBooleanValue(flag);
    }

    @Override
    public boolean shouldAutoExecute() {
        return true;
    }

    public static BooleanFlag getFlag() {
        return flag;
    }

}
