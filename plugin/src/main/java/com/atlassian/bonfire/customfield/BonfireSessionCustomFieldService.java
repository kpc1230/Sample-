package com.atlassian.bonfire.customfield;

import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.customfield.CustomFieldMetadata;
import com.atlassian.borrowed.greenhopper.customfield.CustomFieldService;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.dao.PropertyDao;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IndexException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service(BonfireSessionCustomFieldService.SERVICE)
public class BonfireSessionCustomFieldService {
    public static final String SERVICE = "bonfire-sessionCustomFieldService";

    private static final String RAISED_IN_NAME = "bonfire.customfield.session.raisedin.name";
    private static final String RAISED_IN_DESCRIPTION = "bonfire.customfield.session.raisedin.desc";

    // IMPORTANT - if we change the plugin key, this will need to change too.
    private static final String BONFIRE_SESSION_CUSTOMFIELD_KEY = "com.atlassian.bonfire.plugin:bonfire-session-cft";
    private static final String SESSION_SEARCHER_KEY = "com.atlassian.bonfire.plugin:bonfire-session-searcher";

    private static final String BONFIRE_RAISED_IN_PROPERTY_SET_KEY = "bonfire.session.raisedin.custom.field";

    /**
     * the metadata needed to define the custom field in JIRA
     */
    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata RAISED_IN_METADATA = new CustomFieldMetadata(RAISED_IN_NAME, RAISED_IN_DESCRIPTION,
            BONFIRE_SESSION_CUSTOMFIELD_KEY, SESSION_SEARCHER_KEY);

    @Resource(name = CustomFieldService.SERVICE)
    private CustomFieldService customFieldService;

    @Resource(name = PropertyDao.SERVICE)
    private PropertyDao propertyDao;

    /**
     * This will always return a custom field, even if it has to go and create one
     */
    public CustomField getRaisedInSessionCustomField() {
        Long raisedInCFId = propertyDao.getLongProperty(BONFIRE_RAISED_IN_PROPERTY_SET_KEY);
        CustomField field = null;
        if (raisedInCFId != null) {
            field = customFieldService.getCustomField(raisedInCFId);
        }
        // If no field could be retrieved from the custom field service
        if (field == null) {
            // Create another custom field
            field = createRaisedInSessionCustomField();
            // Place the id of the new field into the property sets
            propertyDao.setLongProperty(BONFIRE_RAISED_IN_PROPERTY_SET_KEY, field.getIdAsLong());
        }

        return field;
    }

    public void addRaisedInValue(Issue issue, Session session) {
        CustomField field = getRaisedInSessionCustomField();
        field.getCustomFieldType().updateValue(field, issue, session.getId().toString());
        reindexIssue(issue);
    }

    public void deleteRaisedInValue(Issue issue) {
        CustomField field = getRaisedInSessionCustomField();
        field.getCustomFieldType().updateValue(field, issue, null);
        reindexIssue(issue);
    }

    /**
     * Blindly creates the Raised In Session Custom Field and associate it with the default screen
     */
    private CustomField createRaisedInSessionCustomField() {
        // Create the field
        CustomField field = customFieldService.createCustomField(RAISED_IN_METADATA);
        return field;
    }

    /**
     * Try and reindex and fail silently if it doesn't work
     */
    private void reindexIssue(Issue issue) {
        try {
            customFieldService.reindexSingleIssue(issue);
        } catch (IndexException index) {
            // Shh.. we wanna be silent
        }
    }
}
