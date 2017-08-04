package com.atlassian.jira.ext.issue;

import com.atlassian.jira.ext.rest.IssueLinks;
import com.atlassian.jira.ext.rest.TimeTracking;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.fields.IssueLinksSystemField;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;

import java.util.List;

/**
 * This class adds the setters and getters for the system fields that are missing from IssueInputParameters.
 */
public class IssueInputParametersExt extends IssueInputParametersImpl {
    public void setLabels(List<String> labels) {
        String[] labelsArray = labels != null ? labels.toArray(new String[labels.size()]) : null;
        getActionParameters().put(SystemSearchConstants.forLabels().getFieldId(), labelsArray);
    }

    public void setTimeTracking(TimeTracking timeTracking) {
        if (timeTracking != null) {
            if (timeTracking.originalEstimate != null) {
                getActionParameters().put(TimeTrackingSystemField.TIMETRACKING_ORIGINALESTIMATE, new String[]{timeTracking.originalEstimate});
            }

            if (timeTracking.remainingEstimate != null) {
                getActionParameters().put(TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE, new String[]{timeTracking.remainingEstimate});
            }
        }
    }

    public void setIssueLinks(IssueLinks issueLinks) {
        if (issueLinks != null) {
            if (issueLinks.linktype != null && issueLinks.issues != null && issueLinks.issues.length > 0) {
                getActionParameters().put(IssueLinksSystemField.PARAMS_LINK_TYPE, new String[]{issueLinks.linktype});
                getActionParameters().put(IssueLinksSystemField.PARAMS_ISSUE_KEYS, issueLinks.issues);
            }
        }
    }
}
