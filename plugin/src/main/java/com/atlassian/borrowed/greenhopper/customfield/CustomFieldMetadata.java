package com.atlassian.borrowed.greenhopper.customfield;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import com.atlassian.jira.issue.issuetype.IssueType;

/**
 * Describes the configuration keys necessary for managing a custom field in JIRA
 *
 * @author ahennecke
 */
public class CustomFieldMetadata {
    private static final String ISSUE_TYPE_ANY = "-1";

    private final String fieldName;
    private final String fieldDescription;
    private final String fieldType;
    private final String fieldSearcher;
    private String[] issueTypes;

    public CustomFieldMetadata(String fieldName, String fieldDescription, String fieldType, String fieldSearcher, String... issueTypes) {
        this.fieldName = fieldName;
        this.fieldDescription = fieldDescription;
        this.fieldType = fieldType;
        this.fieldSearcher = fieldSearcher;
        this.issueTypes = issueTypes;

        if (this.issueTypes.length == 0) this.issueTypes = new String[]{ISSUE_TYPE_ANY};
    }

    /**
     * the custom field name used by JIRA
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * the custom field description used by JIRA
     */
    public String getFieldDescription() {
        return fieldDescription;
    }

    /**
     * the key of the custom field declaration in atlassian-plugins.xml
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * the key of the custom field searcher declaration in atlassian-plugins.xml
     */
    public String getFieldSearcher() {
        return fieldSearcher;
    }

    /**
     * the {@link IssueType} keys this customField is assigned to.
     */
    public String[] getIssueTypes() {
        return issueTypes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("fieldName", fieldName)
                .append("fieldDescription", fieldDescription)
                .append("fieldType", fieldType)
                .append("fieldSearcher", fieldSearcher)
                .append("issueTypes", issueTypes)
                .toString();
    }
}
