package com.atlassian.bonfire.pageobjects.gettingstarted;

import com.atlassian.bonfire.pageobjects.CaptureAbstractJiraPageObject;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;

/**
 * A page for downloading browser extension
 *
 * @since v2.8.5
 */
public class DownloadBrowserExtensionPage extends CaptureAbstractJiraPageObject {
    private static final String URL = "/secure/GetBonfire.jspa";

    @ElementBy(id = "capture-extension-download-message", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement captureDownloadMessage;

    @ElementBy(id = "capture-download-button", timeoutType = TimeoutType.PAGE_LOAD)
    private PageElement captureDownloadLink;

    @Override
    public TimedCondition isAt() {
        return captureDownloadMessage.timed().isPresent();
    }

    @Override
    public String getUrl() {
        return URL;
    }

    /**
     * Check whether capture download link is present
     *
     * @return true if there's a link
     */
    public TimedCondition isDownloadLinkVisible() {
        return captureDownloadLink.timed().isVisible();
    }
}
