package com.atlassian.borrowed.greenhopper.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IndexException;

/**
 * <p>
 * Service for encapsulating custom field logic, especially the rather complex JIRA logic behind creating and working with these things.
 * </p>
 * <p>
 * originally used in GH
 * </p>
 *
 * @author ahennecke
 */
public interface CustomFieldService {
    public static final String SERVICE = "excalibur-customFieldService";

    /**
     * Create a new custom field in JIRA with the given configuration
     */
    public CustomField createCustomField(CustomFieldMetadata fieldMetadata);

    /**
     * @return the {@link CustomField} instance for the given ID, or null if it doesn't exist
     */
    public CustomField getCustomField(Long id);

    /**
     * <p>
     * Associate the custom field with the "Default Screen".
     * </p>
     * Note that the first screen tab found will be used.
     */
    public void associateWithDefaultScreen(CustomField customField);

    /**
     * Cheating method for refreshing the indecies
     */
    public void reindexSingleIssue(Issue issue) throws IndexException;
}
