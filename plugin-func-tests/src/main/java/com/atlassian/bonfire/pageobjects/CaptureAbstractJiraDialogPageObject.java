package com.atlassian.bonfire.pageobjects;

import com.atlassian.bonfire.pageobjects.util.WaitForPageReload;
import com.atlassian.jira.pageobjects.dialogs.JiraDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.inject.Inject;
import org.hamcrest.Matchers;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntil;

/**
 * The base page object to JIRA dialogs in JIRA Capture
 *
 * @since v2.9.1
 */
public class CaptureAbstractJiraDialogPageObject extends JiraDialog {
    @Inject
    protected AtlassianWebDriver driver;

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder pageElementFinder;

    @Inject
    protected WaitForPageReload waitForPageReload;

    @WaitUntil
    public void doWait() {
        final long pageLoadTimeout = timeouts.timeoutFor(TimeoutType.DIALOG_LOAD);
        waitUntil("Waiting for page load failed for the inline dialog ",
                isOpen(), Matchers.sameInstance(true), Poller.by(pageLoadTimeout));
    }
}
