package com.atlassian.bonfire.pageobjects.gettingstarted;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

/**
 * A Getting Started Page
 *
 * @since v2.8.5
 */
public class GettingStartedPage extends CaptureAbstractJiraPageObject {
    private static final String URL = "/secure/BonfireGettingStarted.jspa";

    @ElementBy(cssSelector = ".bf-getting-started-heading", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement captureHeading;

    @ElementBy(cssSelector = ".bf-getting-started-description", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement captureGettingStartedMessage;

    @Override
    public TimedCondition isAt() {
        return captureGettingStartedMessage.timed().isPresent();
    }

    @Override
    public String getUrl() {
        return URL;
    }

    /**
     * Check whether capture menu item on top of the page is present
     */
    public TimedQuery<Boolean> isCaptureMenuLinkVisible() {
        return captureHeading.timed().isVisible();
    }
}
