package com.atlassian.bonfire.pageobjects.dialog;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraDialogPageObject;
import com.atlassian.bonfire.pageobjects.issue.CaptureIssuePage;
import com.atlassian.bonfire.pageobjects.session.CaptureSessionOwnerPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

public class CaptureCompleteSessionDialog extends CaptureAbstractJiraDialogPageObject {
    private final String sessionId;
    private final String projectKey;
    private final String issueKey;

    @ElementBy(id = "bonfire-complete-button", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement submitButton;

    public CaptureCompleteSessionDialog(String sessionId, String projectKey) {
        this(sessionId, projectKey, null);
    }

    public CaptureCompleteSessionDialog(String issueKey) {
        this(null, null, issueKey);
    }

    public CaptureCompleteSessionDialog(String sessionId, String projectKey, String issueKey) {
        this.id = "capture-complete-test-session";
        this.sessionId = sessionId;
        this.projectKey = projectKey;
        this.issueKey = issueKey;
    }

    public String getUrl() {
        return "";
    }


    public CaptureSessionOwnerPage submitComplete() {
        waitForPageReload.afterExecuting(new Runnable() {
            @Override
            public void run() {
                submitButton.click();
            }
        });
        return pageBinder.bind(CaptureSessionOwnerPage.class, sessionId, projectKey);
    }

    public CaptureIssuePage submitCompleteToIssuePage() {
        waitForPageReload.afterExecuting(new Runnable() {
            @Override
            public void run() {
                submitButton.click();
            }
        });
        return pageBinder.bind(CaptureIssuePage.class, issueKey);
    }
}
