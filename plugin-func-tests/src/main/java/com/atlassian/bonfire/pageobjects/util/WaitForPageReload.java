package com.atlassian.bonfire.pageobjects.util;

import com.atlassian.webdriver.utils.element.WebDriverPoller;
import com.google.common.base.Function;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.inject.Inject;

/**
 * Waits for the page reload
 * <p>
 * This code is borrowed from JIRA &amp; rewritten to support Java 7 language level
 * Original class: com.atlassian.jira.pageobjects.util.WaitForPageReload
 * </p>
 * @since v2.9.1
 */
public class WaitForPageReload {
    @Inject
    private WebDriver driver;

    @Inject
    private WebDriverPoller poller;

    /**
     * Performs given action and waits for page reload using dummy javascript variable. It's useful when a form submit
     * is done with javascript in a {@code setTimeout()} function. Due to the code being executed in a separate
     * execution context the page may not be immediately reloaded. This method ensures the page has been reloaded. It
     * does that by setting some javascript document variable, performing given action and waiting until this variable
     * is undefined (or a timeout occurred).
     *
     * @param action the action to be performed, typically clicking a button
     */
    public void afterExecuting(final Runnable action) {
        if (!(driver instanceof JavascriptExecutor)) {
            throw new IllegalStateException(driver + " does not support Javascript");
        }
        final JavascriptExecutor executor = (JavascriptExecutor) driver;
        final String documentDirtyVariable = String.format("document.WaitForPageReload_dirty_page_marker_%d", System.currentTimeMillis());
        // assign boolean value to dummy property
        executor.executeScript(documentDirtyVariable + "=true");
        // perform the action (typically click a button)
        action.run();
        // wait until dummy property is undefined which means the page has been reloaded
        poller.waitUntil(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return "undefined".equals(executor.executeScript("return typeof(" + documentDirtyVariable + ")"));
            }
        });
    }
}
