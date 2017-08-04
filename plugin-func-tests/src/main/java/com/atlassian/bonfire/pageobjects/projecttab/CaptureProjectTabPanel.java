package com.atlassian.bonfire.pageobjects.projecttab;

import org.openqa.selenium.By;
import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

public class CaptureProjectTabPanel extends CaptureAbstractJiraPageObject {
    private final String projectKey;

    @ElementBy(partialLinkText = "Test sessions", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement testSessionTab;

    @ElementBy(cssSelector = ".icon-sidebar-test-sessions", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement projectCentricSessionTab;

    @ElementBy(cssSelector = ".aui-avatar-project", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement projectIcon;

    @ElementBy(id = "bonfire-project-tab-menu-holder", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    protected PageElement tabMenu;

    @ElementBy(id = "session-filter", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement sessionFilter;

    @ElementBy(id = "filterSubmit", timeoutType = TimeoutType.SLOW_PAGE_LOAD)
    private PageElement sessionFilterSubmit;

    public CaptureProjectTabPanel(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getUrl() {
        return "/browse/" + projectKey;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TimedCondition isAt() {
        return projectIcon.timed().isPresent();
    }

    /**
     * Available only since JIRA 6.4
     *
     * @return timed condition representing presence of the link
     */
    public TimedCondition isTestSessionsLinkPresent() {
        return projectCentricSessionTab.timed().isPresent();
    }

    public CaptureProjectTestSessionsTab gotoSessionTab() {
        // This is executed after page load timeout, either of links must be present.
        // Supporting different JIRAs with different navigation patterns.
        if (testSessionTab.isPresent()) {
            // JIRA 6.3 (and earlier) testSessionTab
            testSessionTab.click();
        } else {
            // JIRA 6.4: projectCentricSessionTab
            Poller.waitUntilTrue(projectCentricSessionTab.timed().isVisible());
            projectCentricSessionTab.click();
        }
        return pageBinder.bind(CaptureProjectTestSessionsTab.class, this.projectKey);
    }

    public String getProjectKey() {
        return projectKey;
    }

    public PageElement getTabMenu() {
        return tabMenu;
    }

    public void goToNotesView(){
        String currentTab = tabMenu.find(By.tagName("strong")).getText();
        if ("Sessions".equals(currentTab)){
            tabMenu.find(By.tagName("a")).click();
        }
    }

    public void goToSessionsView() {
        String currentTab = tabMenu.find(By.tagName("strong")).getText();
        if ("Notes".equals(currentTab)){
            tabMenu.find(By.tagName("a")).click();
        }
    }

    public TimedCondition isOnNotesPage() {
        return body.find(By.className("test-session-notes")).timed().isPresent();
    }

    public void applyFilter() {
        sessionFilter.click();
        Poller.waitUntilTrue(sessionFilterSubmit.timed().isPresent());
        sessionFilterSubmit.click();
    }
}
