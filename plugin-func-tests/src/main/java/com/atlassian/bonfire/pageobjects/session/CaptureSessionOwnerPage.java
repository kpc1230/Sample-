package com.atlassian.bonfire.pageobjects.session;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.bonfire.pageobjects.dialog.CaptureCompleteSessionDialog;
import com.atlassian.bonfire.pageobjects.dialog.CaptureDeleteSessionDialog;
import com.atlassian.bonfire.pageobjects.dialog.CaptureEditSessionDialog;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTabPanel;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

public class CaptureSessionOwnerPage extends CaptureAbstractJiraPageObject {
    private final String sessionId;

    private final String projectKey;

    @ElementBy(id = "bonfire-session-main", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement mainSessionPage;

    @ElementBy(id = "statuschange-test-session", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement statusButton;

    @ElementBy(id = "bonfire-session-status", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement hiddenStatus;

    @ElementBy(id = "session-name", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement sessionName;

    @ElementBy(id = "edit-test-session", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement editButton;

    @ElementBy(id = "gf-complete-dialog", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement completeButton;

    @ElementBy(id = "session-related-issue", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement relatedIssueModule;

    @ElementBy(id = "capture-test-session-actions", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement moreActions;

    @ElementBy(id = "delete-test-session", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement deleteTestSession;

    private CaptureEditSessionDialog editDialog;
    private CaptureCompleteSessionDialog completeDialog;
    private CaptureDeleteSessionDialog deleteDialog;

    public CaptureSessionOwnerPage(String sessionId, String projectKey) {
        this.sessionId = sessionId;
        this.projectKey = projectKey;
    }

    public String getUrl() {
        return "/secure/ViewSession.jspa?testSessionId=" + sessionId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TimedCondition isAt() {
        return Conditions.and(mainSessionPage.timed().isVisible(), editButton.timed().isVisible(), sessionName.timed().isPresent());
    }

    public CaptureSessionOwnerPage clickEditSession() {
        editButton.click();
        editDialog = pageBinder.bind(CaptureEditSessionDialog.class, sessionId, projectKey);
        return this;
    }

    public CaptureSessionOwnerPage clickCompleteSession() {
        completeButton.click();
        completeDialog = pageBinder.bind(CaptureCompleteSessionDialog.class, sessionId, projectKey);
        return this;
    }

    public CaptureSessionOwnerPage clickMoreActions() {
        moreActions.click();
        return this;
    }

    public CaptureSessionOwnerPage clickDeleteTestSession() {
        deleteTestSession.click();
        deleteDialog = pageBinder.bind(CaptureDeleteSessionDialog.class, projectKey);
        return this;
    }

    public CaptureSessionOwnerPage editName(String name) {
        editDialog.changeName(name);
        return this;
    }

    public CaptureSessionOwnerPage clearRelatedIssue() {
        editDialog.clearRelatedIssue();
        return this;
    }

    public CaptureSessionOwnerPage addRelatedIssueEdit(String relatedIssue) {
        editDialog.addRelatedIssue(relatedIssue);
        return this;
    }

    public CaptureSessionOwnerPage submitComplete() {
        CaptureSessionOwnerPage newPage = completeDialog.submitComplete();
        return newPage;
    }

    public CaptureSessionOwnerPage submitEdit() {
        CaptureSessionOwnerPage newPage = editDialog.submitForm();
        return newPage;
    }

    public CaptureProjectTabPanel submitDelete() {
        CaptureProjectTabPanel newPage = deleteDialog.submitDelete();
        return newPage;
    }

    public CaptureSessionOwnerPage changeStatus() {
        statusButton.click();
        return this;
    }

    public TimedQuery<Boolean> isChangeStatusButtonPresent() {
        return statusButton.timed().isPresent();
    }

    public TimedQuery<Boolean> isCompleteSessionButtonPresent() {
        return completeButton.timed().isPresent();
    }

    public TimedQuery<Boolean> isRelatedIssueVisible() {
        return relatedIssueModule.timed().isVisible();
    }

    public TimedQuery<Boolean> isMoreActionsVisible() {
        return moreActions.timed().isVisible();
    }

    public TimedQuery<Boolean> isDeleteTestSession() {
        return deleteTestSession.timed().isVisible();
    }

    public TimedQuery<String> getCurrentStatus() {
        return hiddenStatus.timed().getValue();
    }

    public TimedQuery<String> getSessionName() {
        return sessionName.timed().getText();
    }

    public String getSessionId() {
        return sessionId;
    }
}
