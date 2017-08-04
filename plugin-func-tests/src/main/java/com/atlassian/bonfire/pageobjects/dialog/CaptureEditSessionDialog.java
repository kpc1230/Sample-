package com.atlassian.bonfire.pageobjects.dialog;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraDialogPageObject;
import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.bonfire.pageobjects.session.CaptureSessionOwnerPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

public class CaptureEditSessionDialog extends CaptureAbstractJiraDialogPageObject {
    private final String sessionId;
    private final String projectKey;

    @ElementBy(id = "ex-session-name", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement sessionNameInput;

    @ElementBy(id = "ex-issueKey-textarea")
    private PageElement issueKeyInput;

    @ElementBy(id = "ex-submit", timeoutType = TimeoutType.DIALOG_LOAD)
    private PageElement submitButton;

    public CaptureEditSessionDialog(String sessionId, String projectKey) {
        this.sessionId = sessionId;
        this.projectKey = projectKey;
    }

    public String getUrl() {
        return "";
    }

    public CaptureEditSessionDialog changeName(CharSequence name) {
        sessionNameInput.clear();
        sessionNameInput.type(name);
        return this;
    }

    public CaptureEditSessionDialog clearRelatedIssue() {
        pageBinder.bind(DeleteFrotherToken.class).deleteToken();
        return this;
    }

    public CaptureEditSessionDialog addRelatedIssue(CharSequence issue) {
        issueKeyInput.type(issue);
        RelatedIssueSuggestions suggestions = pageBinder.bind(RelatedIssueSuggestions.class);
        suggestions.clickASuggestion();
        return this;
    }

    public CaptureSessionOwnerPage submitForm() {
        Poller.waitUntilFalse(submitButton.timed().hasAttribute("disabled", "disabled"));
        waitForPageReload.afterExecuting(new Runnable() {
            @Override
            public void run() {
                submitButton.click();
            }
        });
        return pageBinder.bind(CaptureSessionOwnerPage.class, sessionId, projectKey);
    }

    private void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static class DeleteFrotherToken extends CaptureAbstractJiraPageObject {
        @ElementBy(cssSelector = "em.item-delete")
        private PageElement token;

        public String getUrl() {
            return "";
        }

        @Override
        public TimedCondition isAt() {
            return token.timed().isPresent();
        }

        public DeleteFrotherToken deleteToken() {
            token.click();
            Poller.waitUntilFalse(token.timed().isPresent());
            return this;
        }
    }
}
