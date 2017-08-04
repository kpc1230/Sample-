package com.atlassian.bonfire.customfield.searcher;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;

/**
 * Adapted from SprintIndexValueConverter in GreenHopper. Converts what the user input into the string we want to search against
 */
public class SessionIndexValueConverter implements IndexValueConverter {
    public SessionIndexValueConverter() {
    }

    public String convertToIndexValue(final QueryLiteral rawValue) {
        if (rawValue.isEmpty()) {
            return null;
        }

        return rawValue.asString();
    }
}
