package com.atlassian.bonfire.pageobjects.dialog;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraDialogPageObject;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTestSessionsTab;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

public class CaptureCreateSessionDialog extends CaptureAbstractJiraDialogPageObject {
    private final String projectKey;

    @ElementBy(id = "ex-session-name", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement sessionNameInput;

    @ElementBy(id = "ex-submit", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement submitButton;

    @ElementBy(id = "ex-session-info", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement additionalInfoInput;

    @ElementBy(id = "ex-issueKey-textarea", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement relatedIssue;

    @ElementBy(id = "capture-create-test-session")
    private PageElement formElement;

    public CaptureCreateSessionDialog(String projectKey) {
        this.projectKey = projectKey;
        this.id = "capture-create-test-session";
    }

    public String getUrl() {
        return "";
    }

    public CaptureCreateSessionDialog addRelatedIssue(CharSequence issueKey) {
        relatedIssue.type(issueKey);
        RelatedIssueSuggestions suggestions = pageBinder.bind(RelatedIssueSuggestions.class);
        suggestions.clickASuggestion();
        return this;
    }

    public CaptureCreateSessionDialog addName(CharSequence name) {
        sessionNameInput.type(name);
        return this;
    }

    public CaptureCreateSessionDialog addAdditionalInfo(CharSequence addInfo) {
        additionalInfoInput.type(addInfo);
        return this;
    }

    public CaptureProjectTestSessionsTab submitForm() {
        waitForPageReload.afterExecuting(new Runnable() {
            @Override
            public void run() {
                submitButton.click();
            }
        });
        return pageBinder.bind(CaptureProjectTestSessionsTab.class, projectKey);
    }
}
