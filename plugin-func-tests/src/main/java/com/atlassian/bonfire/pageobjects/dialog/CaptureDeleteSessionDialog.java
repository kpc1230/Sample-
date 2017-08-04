package com.atlassian.bonfire.pageobjects.dialog;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.bonfire.pageobjects.issue.CaptureIssuePage;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTabPanel;
import com.atlassian.bonfire.pageobjects.util.WaitForPageReload;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.google.inject.Inject;

/**
 * @since v2.9
 */
public class CaptureDeleteSessionDialog extends CaptureAbstractJiraPageObject {
    private final String projectOrIssueKey;

    @ElementBy(id = "ex-submit", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement deleteButton;

    @Inject
    protected WaitForPageReload waitForPageReload;

    public CaptureDeleteSessionDialog(String projectOrIssueKey) {
        this.projectOrIssueKey = projectOrIssueKey;
    }

    @Override
    public TimedCondition isAt() {
        return Conditions.and(deleteButton.timed().isPresent());
    }

    @Override
    public String getUrl() {
        return "";
    }

    public CaptureProjectTabPanel submitDelete() {
        waitForPageReload.afterExecuting(new Runnable() {
            @Override
            public void run() {
                deleteButton.click();
            }
        });
        return pageBinder.bind(CaptureProjectTabPanel.class, projectOrIssueKey);
    }

    public CaptureIssuePage submitDeleteOnIssuePage() {
        waitForPageReload.afterExecuting(new Runnable() {
            @Override
            public void run() {
                deleteButton.click();
            }
        });
        return pageBinder.bind(CaptureIssuePage.class, projectOrIssueKey);
    }
}
