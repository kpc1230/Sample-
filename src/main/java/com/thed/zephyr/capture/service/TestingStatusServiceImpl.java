package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.customfield.BonfireContextCustomFieldsService;
import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.BonfireSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.TestingStatusCustomFieldService;
import com.thed.zephyr.capture.model.LightSession;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.log.clean.LaasLogger;
import com.atlassian.jira.log.clean.LaasLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.atlassian.bonfire.features.CaptureFeatureFlags.TESTING_STATUS_DB_PRIMARY;

@Service(TestingStatusService.SERVICE)
public class TestingStatusServiceImpl implements TestingStatusService {
    private static final LaasLogger LOGGER = LaasLoggerFactory.getLogger(TestingStatusService.class);

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = TestingStatusCustomFieldService.SERVICE)
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @JIRAResource
    private JiraFeatureFlagService jiraFeatureFlagService;

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = BonfireContextCustomFieldsService.SERVICE)
    private BonfireContextCustomFieldsService bonfireContextCustomFieldsService;

    @Override
    public TestingStatus calculateTestingStatus(Issue issue) {
        CustomField relatedToField = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        String rawSessionIds = relatedToField.getValueFromIssue(issue);
        if (StringUtils.isNotBlank(rawSessionIds)) {
            String[] split = rawSessionIds.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
            int startedCount = 0,
                    completedCount = 0;
            for (String s : split) {
                LightSession session = sessionController.getLightSession(s);
                if (session != null) {
                    if (Status.CREATED.equals(session.getStatus())) {
                        startedCount++;
                    } else if (Status.COMPLETED.equals(session.getStatus())) {
                        completedCount++;
                    } else {
                        // If a status other than created and completed appears, then it is in progress
                        return TestingStatus.IN_PROGRESS;
                    }
                }
            }
            if (startedCount == 0 && completedCount != 0) {
                // If all the sessions are 'completed' then return complete
                return TestingStatus.COMPLETED;
            } else if (startedCount != 0 && completedCount == 0) {
                // If all the sessions are 'created' then return not started
                return TestingStatus.NOT_STARTED;
            } else {
                // Otherwise the sessions are either 'completed' or 'created'
                return TestingStatus.INCOMPLETE;
            }
        }

        // If there are no test sessions, return not started
        return TestingStatus.NOT_STARTED;
    }

    public TestingStatus getTestingStatus(Issue issue) {
        CustomField testingStatusCF = testingStatusCustomFieldService.getTestingStatusSessionCustomField();
        if(jiraFeatureFlagService.isEnabled(TESTING_STATUS_DB_PRIMARY.asFlag())) {
            String testingStatusString = testingStatusCF.getValueFromIssue(issue);
            TestingStatus testingStatus = TestingStatus.fromi18nKey(testingStatusString);

            if (testingStatus == null) {
                TestingStatus status = calculateTestingStatus(issue);
                if(hasAnyCaptureCustomFieldValue(issue)) {
                    LOGGER.privacySafe().error(String.format("TestingStatus value for issue %d not found, returning calculated value: %s", issue.getId(), status.getI18nKey()));
                    testingStatusCF.getCustomFieldType().updateValue(testingStatusCF, issue, status.getI18nKey());
                }
                return status;
            }
            return testingStatus;
        }
        else {
            return calculateTestingStatus(issue);
        }
    }

     private boolean hasAnyCaptureCustomFieldValue(Issue issue) {
        CustomField raisedIn = bonfireSessionCustomFieldService.getRaisedInSessionCustomField();
        CustomField relatedTo = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        // If either of these have a value then TestingStatus should be stored in DB
        boolean hasRelatedToValue = relatedTo.hasValue(issue);
        boolean hasRaisedInValue = raisedIn.hasValue(issue);
        boolean hasContextValue = bonfireContextCustomFieldsService.hasContextValues(issue);

        if(hasContextValue) {
            LOGGER.privacySafe().warn("TestingStatus for Issue with id: " + issue.getId() + " not found but has value for Context customfields" );
        }
        if(hasRelatedToValue) {
            LOGGER.privacySafe().warn("TestingStatus for Issue with id: " + issue.getId() + " not found but has value for " + relatedTo.getFieldName() +" customfield");
        }
        if(hasRaisedInValue) {
            LOGGER.privacySafe().warn("TestingStatus for Issue with id: " + issue.getId() + " not found but has value for " + raisedIn.getFieldName() +" customfield");
        }

        return (hasRelatedToValue || hasRaisedInValue || hasContextValue);
    }

    public TestingStatusBar getTestingStatusBar(Issue issue) {
        int notStartedPercent = 0, inProgressPercent = 0, completePercent = 0;
        double notStartedCount = 0, inprogressCount = 0, completedCount = 0, totalCount = 0;
        CustomField relatedToField = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        String rawSessionIds = relatedToField.getValueFromIssue(issue);
        if (StringUtils.isNotBlank(rawSessionIds)) {
            String[] split = rawSessionIds.split(BonfireMultiSessionCustomFieldService.MULTI_SESSION_DELIMITER);
            for (String s : split) {
                LightSession session = sessionController.getLightSession(s);
                if (session != null) {
                    switch (session.getStatus()) {
                        case CREATED:
                            notStartedCount++;
                            break;
                        case COMPLETED:
                            completedCount++;
                            break;
                        default:
                            inprogressCount++;
                            break;
                    }

                    totalCount++;
                }
            }

            notStartedPercent = (int) Math.floor((notStartedCount / totalCount) * 100);
            inProgressPercent = (int) Math.floor((inprogressCount / totalCount) * 100);
            completePercent = (int) Math.floor((completedCount / totalCount) * 100);
        }

        return new TestingStatusBar(notStartedPercent, inProgressPercent, completePercent, (int) completedCount, (int) totalCount);
    }
}
