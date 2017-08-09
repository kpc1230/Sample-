package com.thed.zephyr.capture.customfield.searcher;

import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.log.clean.LaasLogger;
import com.atlassian.jira.log.clean.LaasLoggerFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import com.thed.zephyr.capture.service.BonfireI18nService;
import com.thed.zephyr.capture.service.TestingStatusService;
import com.thed.zephyr.capture.util.BonfireServiceAccessor;

import static com.atlassian.bonfire.features.CaptureFeatureFlags.*;

public class TestingStatusCustomFieldIndexer extends AbstractCustomFieldIndexer {
    private static final LaasLogger LOGGER = LaasLoggerFactory.getLogger(TestingStatusService.class);

    private TestingStatusService testingStatusService;

    private BonfireI18nService i18n;
    private JiraFeatureFlagService jiraFeatureFlagService;

    protected TestingStatusCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField customField, JiraFeatureFlagService jiraFeatureFlagService) {
        super(fieldVisibilityManager, customField);
        this.jiraFeatureFlagService = jiraFeatureFlagService;
        i18n = BonfireServiceAccessor.getInstance().getI18n();
        testingStatusService = BonfireServiceAccessor.getInstance().getTestingStatusService();
    }

    @Override
    public void addDocumentFieldsSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED);
    }

    @Override
    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
        String i18nKey;
        if(jiraFeatureFlagService.getBooleanValue(TESTING_STATUS_INDEXER_READ_FROM_DB.asFlag())) {
            if(jiraFeatureFlagService.getBooleanValue(TESTING_STATUS_DB_PRIMARY.asFlag())){
                LOGGER.privacySafe().info("Reindexing: reading Testing status from DB");
            } else {
                LOGGER.privacySafe().warn("Reindexing: not all flags are enabled! Calculating Testing status");
            }
            i18nKey = testingStatusService.getTestingStatus(issue).getI18nKey();
        } else {
            i18nKey = testingStatusService.calculateTestingStatus(issue).getI18nKey();
        }
        String statusString = i18n.getText(i18nKey);
        if (statusString == null) {
            return;
        }

        doc.add(new Field(getDocumentFieldId(), statusString, Field.Store.YES, indexType));
    }
}
