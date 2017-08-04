package com.atlassian.bonfire.customfield.searcher;

import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.TestingStatusService.TestingStatus;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.IndexValueConverter;

/**
 * Converts what the user input into the string we want to search against
 */
public class TestingStatusValueConverter implements IndexValueConverter {
    private final String i18ndNotStarted;
    private final String i18ndInProgress;
    private final String i18ndCompleted;
    private final String i18ndIncomplete;

    public TestingStatusValueConverter(BonfireI18nService i18n) {
        this.i18ndNotStarted = i18n.getText(TestingStatus.NOT_STARTED.getI18nKey());
        this.i18ndInProgress = i18n.getText(TestingStatus.IN_PROGRESS.getI18nKey());
        this.i18ndCompleted = i18n.getText(TestingStatus.COMPLETED.getI18nKey());
        this.i18ndIncomplete = i18n.getText(TestingStatus.INCOMPLETE.getI18nKey());
    }

    public String convertToIndexValue(QueryLiteral rawValue) {
        String rawString = rawValue.asString();
        if (i18ndNotStarted.equals(rawString)) {
            return i18ndNotStarted;
        } else if (i18ndInProgress.equals(rawString)) {
            return i18ndInProgress;
        } else if (i18ndCompleted.equals(rawString)) {
            return i18ndCompleted;
        } else if (i18ndIncomplete.equals(rawString)) {
            return i18ndIncomplete;
        }
        return null;
    }
}
