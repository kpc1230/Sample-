package com.atlassian.bonfire.pageobjects.projecttab;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.bonfire.pageobjects.dialog.CaptureCreateSessionDialog;
import com.atlassian.bonfire.pageobjects.session.CaptureSessionOwnerPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import java.util.List;

public class CaptureProjectTestSessionsTab extends CaptureAbstractJiraPageObject {
    @ElementBy(id = "create-test-session", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement createSessionButton;

    private CaptureCreateSessionDialog createDialog;

    private final String projectKey;

    public CaptureProjectTestSessionsTab(String projectKey) {
        this.projectKey = projectKey;
    }

    @Override
    public TimedCondition isAt() {
        return Conditions.and(
                createSessionButton.timed().isPresent()
        );
    }

    @Override
    public String getUrl() {
        return null;
    }

    public CaptureProjectTestSessionsTab clickCreateSessionButton() {
        createSessionButton.click();
        createDialog = pageBinder.bind(CaptureCreateSessionDialog.class, projectKey);
        return this;
    }

    public CaptureProjectTestSessionsTab addName(CharSequence name) {
        createDialog.addName(name);
        return this;
    }

    public CaptureProjectTestSessionsTab addRelatedIssue(CharSequence issueKey) {
        createDialog.addRelatedIssue(issueKey);
        return this;
    }

    public CaptureProjectTestSessionsTab addAdditionalInfo(CharSequence addInfo) {
        createDialog.addAdditionalInfo(addInfo);
        return this;
    }

    public CaptureProjectTestSessionsTab submitForm() {
        CaptureProjectTestSessionsTab panel = createDialog.submitForm();
        return panel;
    }

    public int testSessionCount() {
        return pageBinder.bind(TestSessionsBox.class, projectKey).testSessionCount();
    }

    public CaptureSessionOwnerPage goToFirstSession() {
        return pageBinder.bind(TestSessionsBox.class, projectKey).goToFirstSession();
    }

    public <T extends CaptureAbstractJiraPageObject> T goToFirstSession(Class<T> pageObjectClass) {
        return pageBinder.bind(TestSessionsBox.class, projectKey).goToFirstSession(pageObjectClass);
    }

    public TimedQuery<Boolean> isCreateButtonPresent() {
        return createSessionButton.timed().isPresent();
    }

    public static class TestSessionsBox extends CaptureAbstractJiraPageObject {
        private final String sessionBoxProjectKey;

        @ElementBy(cssSelector = ".test-sessions", timeoutType = TimeoutType.DIALOG_LOAD)
        private PageElement testSessionsBox;

        public TestSessionsBox(String sessionBoxProjectKey) {
            this.sessionBoxProjectKey = sessionBoxProjectKey;
        }

        public String getUrl() {
            return "";
        }

        @Override
        public TimedCondition isAt() {
            return testSessionsBox.timed().isPresent();
        }

        public int testSessionCount() {
            return pageElementFinder.findAll(By.className("session-title")).size();
        }

        public CaptureSessionOwnerPage goToFirstSession() {
            return this.goToFirstSession(CaptureSessionOwnerPage.class);
        }

        public <T extends CaptureAbstractJiraPageObject> T goToFirstSession(Class<T> pageObjectClass) {
            List<PageElement> foundTitles = pageElementFinder.findAll(By.className("session-container"));
            if (!foundTitles.isEmpty()) {
                PageElement singleSessionBox = foundTitles.get(0);
                String id = singleSessionBox.getAttribute("id");
                singleSessionBox.find(By.cssSelector(".session-title a")).click();
                return pageBinder.bind(pageObjectClass, id, sessionBoxProjectKey);
            }
            // If they npe above well... too bad coz the session list is empty
            return null;
        }
    }

}
