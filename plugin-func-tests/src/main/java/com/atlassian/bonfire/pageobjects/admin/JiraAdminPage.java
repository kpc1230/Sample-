package com.atlassian.bonfire.pageobjects.admin;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import org.openqa.selenium.By;

import java.util.List;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.1
 */
public class JiraAdminPage extends CaptureAbstractJiraPageObject {
    private static final String URL = "/secure/AdminSummary.jspa";

    @ElementBy(id = "admin-summary-panel", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement adminSummaryPanel;

    @ElementBy(id = "testing_section")
    private PageElement testingSection;


    @Override
    public TimedCondition isAt() {
        return Conditions.and(adminSummaryPanel.timed().isPresent());
    }

    public String getUrl() {
        return URL;
    }

    public boolean isBonfireLicenseLinkVisible() {
        List<PageElement> elements = testingSection.findAll(By.tagName("a"));
        for (PageElement link : elements) {
            if (link.getAttribute("href").contains("upm")) return true;
        }
        return false;
    }

    public boolean isBonfireSettingsLinkVisible() {
        List<PageElement> elements = testingSection.findAll(By.tagName("a"));
        for (PageElement link : elements) {
            if (link.getAttribute("href").contains("BonfireSettings")) return true;
        }
        return false;
    }

}
