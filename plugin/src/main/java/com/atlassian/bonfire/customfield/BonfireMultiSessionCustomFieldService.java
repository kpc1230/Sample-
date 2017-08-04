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
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service(BonfireMultiSessionCustomFieldService.SERVICE)
public class BonfireMultiSessionCustomFieldService {
    public static final String SERVICE = "bonfire-multiSessionCustomFieldService";

    public static final String MULTI_SESSION_DELIMITER = ", ";

    private static final String RELATED_TO_NAME = "bonfire.customfield.session.relatedto.name";
    private static final String RELATED_TO_DESCRIPTION = "bonfire.customfield.session.relatedto.desc";

    // IMPORTANT - if we change the plugin key, this will need to change too.
    private static final String BONFIRE_MULTI_SESSION_CUSTOMFIELD_KEY = "com.atlassian.bonfire.plugin:bonfire-multi-session-cft";
    private static final String SESSION_SEARCHER_KEY = "com.atlassian.bonfire.plugin:bonfire-session-searcher";

    private static final String BONFIRE_RELATED_TO_PROPERTY_SET_KEY = "bonfire.session.relatedto.custom.field";

    /**
     * the metadata needed to define the custom field in JIRA
     */
    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Immutable tenantless constant")
    private static final CustomFieldMetadata RELATED_TO_METADATA = new CustomFieldMetadata(RELATED_TO_NAME, RELATED_TO_DESCRIPTION,
            BONFIRE_MULTI_SESSION_CUSTOMFIELD_KEY, SESSION_SEARCHER_KEY);

    @Resource(name = CustomFieldService.SERVICE)
    private CustomFieldService customFieldService;

    @Resource(name = PropertyDao.SERVICE)
    private PropertyDao propertyDao;

    /**
     * This will always return a custom field, even if it has to go and create one
     */
    public CustomField getRelatedToSessionCustomField() {
        Long relatedToCFId = propertyDao.getLongProperty(BONFIRE_RELATED_TO_PROPERTY_SET_KEY);
        CustomField field = null;
        if (relatedToCFId != null) {
            field = customFieldService.getCustomField(relatedToCFId);
        }
        // If no field could be retrieved from the custom field service
        if (field == null) {
            // Create another custom field
            field = createRelatedToSessionCustomField();
            // Place the id of the new field into the property sets
            propertyDao.setLongProperty(BONFIRE_RELATED_TO_PROPERTY_SET_KEY, field.getIdAsLong());
        }

        return field;
    }

    // Accept a session here instead of an id
    public void addRelatedToValue(Issue issue, Session session) {
        CustomField field = getRelatedToSessionCustomField();
        String value = field.getValueFromIssue(issue);
        StringBuilder sb;
        if (StringUtils.isEmpty(value)) {
            sb = new StringBuilder();
        } else {
            sb = new StringBuilder(value);
        }
        sb.append(session.getId()).append(MULTI_SESSION_DELIMITER);
        field.getCustomFieldType().updateValue(field, issue, sb.toString());
        reindexIssue(issue);
    }

    // Accept a session here instead of an id
    public void deleteRelatedToValue(Issue issue, Session session) {
        CustomField field = getRelatedToSessionCustomField();
        String value = field.getValueFromIssue(issue);
        StringBuilder sb = new StringBuilder();
        String[] split = value.split(MULTI_SESSION_DELIMITER);
        for (String s : split) {
            s = s.trim();
            if (!StringUtils.isEmpty(s) && !s.equals(session.getId().toString())) {
                sb.append(s).append(MULTI_SESSION_DELIMITER);
            }
        }
        value = sb.toString();
        if (StringUtils.isEmpty(value)) {
            value = null;
        }
        field.getCustomFieldType().updateValue(field, issue, value);
        reindexIssue(issue);
    }

    public void clearRelatedToValue(Issue issue) {
        CustomField field = getRelatedToSessionCustomField();
        field.getCustomFieldType().updateValue(field, issue, null);
        reindexIssue(issue);
    }

    /**
     * Blindly creates the Related To Session Custom Field and associate it with the default screen
     */
    private CustomField createRelatedToSessionCustomField() {
        // Create the field
        CustomField field = customFieldService.createCustomField(RELATED_TO_METADATA);
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
