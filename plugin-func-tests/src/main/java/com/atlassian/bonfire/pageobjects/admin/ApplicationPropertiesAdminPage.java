package com.atlassian.bonfire.pageobjects.admin;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.1
 */
public class ApplicationPropertiesAdminPage extends CaptureAbstractJiraPageObject {
    @ElementBy(id = "studio-admin-menu", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement adminMenu;

    @Override
    public TimedCondition isAt() {
        return Conditions.and(adminMenu.timed().isPresent());
    }

    public String getUrl() {
        return "/secure/admin/jira/ViewApplicationProperties.jspa";
    }

    public boolean isBonfireLicenseLinkVisible() {
        return adminMenu.find(By.linkText("Bonfire license")).isPresent();
    }

    public boolean isBonfireSettingsLinkVisible() {
        return adminMenu.find(By.linkText("Bonfire settings")).isPresent();
    }

}
