package com.atlassian.bonfire.pageobjects.issue;

import com.atlassian.bonfire.pageobjects.dialog.CaptureCompleteSessionDialog;
import com.atlassian.bonfire.pageobjects.dialog.CaptureCreateSessionDialog;
import com.atlassian.bonfire.pageobjects.dialog.CaptureDeleteSessionDialog;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.List;

public class CaptureIssuePage extends ViewIssuePage {

    @ElementBy(id = "bonfiremodule")
    PageElement capturePanel;

    @ElementBy(cssSelector = ".ajs-layer.active")
    PageElement auiContextMenu;

    @ElementBy( className = "bf-testing-status-cell")
    PageElement testingstatus;

    @Inject
    private PageBinder pageBinder;

    public CaptureIssuePage(String issueKey) {
        super(issueKey);
    }

    public CaptureIssuePage(String issueKey, String anchor) {
        super(issueKey, anchor);
    }

    public CaptureCreateSessionDialog openCreateSessionDialog() {
        return getIssueMenu().invoke(new CreateTestSessionOperation(), CaptureCreateSessionDialog.class, getProject());
    }

    public CaptureCompleteSessionDialog openCompleteSessionDialog(String sessionName) {
        final PageElement sessionElement = getSessionByName(sessionName);
        clickCog(sessionElement);
        auiContextMenu.find(By.linkText("Complete")).click();
        return pageBinder.delayedBind(CaptureCompleteSessionDialog.class, getIssueKey()).waitUntil().bind();
    }

    public CaptureDeleteSessionDialog openDeleteSessionDialog(String sessionName) {
        final PageElement sessionElement = getSessionByName(sessionName);
        clickCog(sessionElement);
        auiContextMenu.find(By.linkText("Delete")).click();
        return pageBinder.delayedBind(CaptureDeleteSessionDialog.class, getIssueKey()).waitUntil().bind();
    }

    public CaptureIssuePage startSession(String sessionName) {
        final PageElement sessionElement = getSessionByName(sessionName);
        clickCog(sessionElement);
        auiContextMenu.find(By.linkText("Start")).click();

        final PageElement inlineStatus = sessionElement.find(By.className("bf-inline-status"));
        Poller.waitUntil("Session should be started now", inlineStatus.timed().getText(), Matchers.containsString("Started"));
        return this;
    }

    public CaptureIssuePage pauseSession(String sessionName) {
        final PageElement sessionElement = getSessionByName(sessionName);
        clickCog(sessionElement);
        auiContextMenu.find(By.linkText("Pause")).click();

        final PageElement inlineStatus = sessionElement.find(By.className("bf-inline-status"));
        Poller.waitUntil("Session should be started now", inlineStatus.timed().getText(), Matchers.containsString("Paused"));
        return this;
    }

    public TimedQuery<String> getSessionStatus(String sessionName) {
        final PageElement sessionElement = getSessionByName(sessionName);
        final PageElement inlineStatus = sessionElement.find(By.className("bf-inline-status"));
        return inlineStatus.timed().getText();
    }

    private void clickCog(PageElement sessionElement) {
        Poller.waitUntilTrue("Cog context menu should be visible", sessionElement.find(By.cssSelector(".bf-more-trigger")).timed().isPresent());
        final PageElement sessionCog = sessionElement.find(By.cssSelector(".bf-more-trigger"));
        sessionCog.click();
        Poller.waitUntilTrue("Cog context menu should be open", auiContextMenu.timed().isVisible());
    }

    private PageElement getSessionByName(String sessionName) {
        for (PageElement webElement : getSessions()) {
            final PageElement sessionNameElement = webElement.find(By.cssSelector(".bf-single-session-row"));
            if (sessionNameElement != null && sessionName.equals(sessionNameElement.getText())) {
                return webElement;
            }
        }
        Assert.fail("Session with name " + sessionName + " cannot be find on the screen");
        return null;
    }

    private List<PageElement> getSessions() {
        Poller.waitUntilTrue("Test session panel should be visible", capturePanel.timed().isVisible());
        return capturePanel.findAll(By.cssSelector("div.bf-table"));
    }

    public TimedQuery<Boolean> getCapturePanelVisibility() {
        return capturePanel.timed().isVisible();
    }

    public PageElement getCapturePanel() {
        return capturePanel;
    }

    public String getTestingStatus() {
        Poller.waitUntilTrue("Test session panel should be visible", capturePanel.timed().isVisible());
        if(testingstatus.isPresent()) {
            return testingstatus.find(By.tagName("strong")).getText();
        }
        return null;
    }
}
