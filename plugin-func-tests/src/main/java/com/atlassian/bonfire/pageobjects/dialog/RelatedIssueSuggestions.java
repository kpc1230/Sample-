package com.atlassian.bonfire.pageobjects.dialog;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraDialogPageObject;
import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.openqa.selenium.By;

public class RelatedIssueSuggestions extends CaptureAbstractJiraDialogPageObject {
    @ElementBy(id = "ex-issueKey-suggestions")
    private PageElement suggestions;

    public RelatedIssueSuggestions clickASuggestion() {
        final PageElement historySearchPopup = pageElementFinder.find(By.id("current-search"));
        Poller.waitUntilTrue("History search element must be present", historySearchPopup.timed().isPresent());

        final PageElement relatedIssueLink = historySearchPopup.find(By.className("aui-iconised-link"));
        Poller.waitUntilTrue("Related issue link must be present", relatedIssueLink.timed().isPresent());

        relatedIssueLink.click();
        pageBinder.bind(FotherSelection.class);
        return this;
    }

    public static class FotherSelection extends CaptureAbstractJiraPageObject {
        @ElementBy(cssSelector = "span.value-text")
        private PageElement lozenge;

        public String getUrl() {
            return "";
        }

        @Override
        public TimedCondition isAt() {
            return lozenge.timed().isPresent();
        }
    }
}
