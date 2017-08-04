package com.atlassian.bonfire.customfield;

import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.customfield.CustomFieldMetadata;
import com.atlassian.borrowed.greenhopper.customfield.CustomFieldService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.service.dao.PropertyDao;
import com.atlassian.jira.featureflag.JiraFeatureFlagService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.log.clean.LaasLogger;
import com.atlassian.jira.log.clean.LaasLoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import com.atlassian.bonfire.service.TestingStatusService;
import com.atlassian.bonfire.service.TestingStatusService.TestingStatus;

import static com.atlassian.bonfire.features.CaptureFeatureFlags.TESTING_STATUS_UPDATE_IN_DB;


@Service(TestingStatusCustomFieldService.SERVICE)
public class TestingStatusCustomFieldService {
    private static final LaasLogger LOGGER = LaasLoggerFactory.getLogger(TestingStatusCustomFieldService.class);

    public static final String SERVICE = "bonfire-testingStatusCustomFieldService";

    private static final String TESTING_STATUS_NAME = "bonfire.customfield.session.testingstatus.name";
    private static final String TESTING_STATUS_DESCRIPTION = "bonfire.customfield.session.testingstatus.desc";

    // IMPORTANT - if we change the plugin key, this will need to change too.
    public static final String BONFIRE_TESTING_STATUS_CUSTOMFIELD_KEY = "com.atlassian.bonfire.plugin:bonfire-testing-status-cft";
    private static final String TESTING_STATUS_SEARCHER_KEY = "com.atlassian.bonfire.plugin:bonfire-testingstatus-searcher";

    private static final String BONFIRE_TESTING_STATUS_PROPERTY_SET_KEY = "bonfire.session.testingstatus.custom.field";

    /**
     * the metadata needed to define the custom field in JIRA
     */
    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata TESTING_STATUS_METADATA = new CustomFieldMetadata(TESTING_STATUS_NAME, TESTING_STATUS_DESCRIPTION,
            BONFIRE_TESTING_STATUS_CUSTOMFIELD_KEY, TESTING_STATUS_SEARCHER_KEY);

    @Resource(name = CustomFieldService.SERVICE)
    private CustomFieldService customFieldService;

    @Resource(name = PropertyDao.SERVICE)
    private PropertyDao propertyDao;

    @Resource(name = TestingStatusService.SERVICE)
    private TestingStatusService statusService;

    @JIRAResource
    private JiraFeatureFlagService jiraFeatureFlagService;

    /**
     * This will always return a custom field, even if it has to go and create one
     */
    public CustomField getTestingStatusSessionCustomField() {
        Long testingStatusCFId = propertyDao.getLongProperty(BONFIRE_TESTING_STATUS_PROPERTY_SET_KEY);
        CustomField field = null;
        if (testingStatusCFId != null) {
            field = customFieldService.getCustomField(testingStatusCFId);
        }
        // If no field could be retrieved from the custom field service
        if (field == null) {
            // Create another custom field
            field = createTestingStatusCustomField();
            // Place the id of the new field into the property sets
            propertyDao.setLongProperty(BONFIRE_TESTING_STATUS_PROPERTY_SET_KEY, field.getIdAsLong());
        }

        return field;
    }

    /**
     * Blindly creates the Testing Status Custom Field
     */
    private CustomField createTestingStatusCustomField() {
        // Create the field
        CustomField field = customFieldService.createCustomField(TESTING_STATUS_METADATA);
        return field;
    }

    public void updateTestingStatus(Issue issue) {
        if(jiraFeatureFlagService.isEnabled(TESTING_STATUS_UPDATE_IN_DB.asFlag())) {
            CustomField field = getTestingStatusSessionCustomField();
            TestingStatus testingStatus = statusService.calculateTestingStatus(issue);
            LOGGER.privacySafe().info("Updating Testing Status value in DB for issue with id: " + issue.getId() + ", and custom field with id: " + field.getId());
            field.getCustomFieldType()
                    .updateValue(field, issue, testingStatus.getI18nKey());
        }
    }
}
