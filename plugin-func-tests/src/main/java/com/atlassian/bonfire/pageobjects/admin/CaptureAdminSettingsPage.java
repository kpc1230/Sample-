package com.atlassian.bonfire.pageobjects.admin;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

/**
 * Page with admin settings for Capture
 *
 * @since v2.9.5
 */
public class CaptureAdminSettingsPage extends CaptureAbstractJiraPageObject {
    private static final String URI = "/secure/BonfireSettings.jspa?decorator=admin";

    @ElementBy(className = "bf-settings-submit", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement submitButton;

    @ElementBy(className = "capture-business-projects-setting", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement showInBusinessProjectsCheckbox;

    @Override
    public TimedCondition isAt() {
        return submitButton.timed().isPresent();
    }

    @Override
    public String getUrl() {
        return URI;
    }

    public CaptureAdminSettingsPage submit() {
        submitButton.click();
        return this;
    }

    public TimedCondition isSettingSaved() {
        return elementFinder.find(By.className("bf-settings-success")).timed().isPresent();
    }

    public CaptureAdminSettingsPage showCaptureForBusinessProjects(boolean state) {
        setCheckbox(showInBusinessProjectsCheckbox, state);
        return this;
    }

    private void setCheckbox(final PageElement checkbox, final boolean state) {
        if (checkbox.isSelected() != state) {
            checkbox.toggle();
        }
    }
}
