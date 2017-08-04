package it.com.atlassian.bonfire.toolbar;

import com.atlassian.bonfire.pageobjects.gettingstarted.DownloadBrowserExtensionPage;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.test.categories.OnDemandAcceptanceTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertNotNull;

/**
 * OD smoke test to check whether JIRA Capture is accessible
 *
 * @since v2.8.5
 */
@Category(OnDemandAcceptanceTest.class)
public class CaptureToolbarLinkAcceptanceTest extends BaseJiraWebTest {
    @Test
    @LoginAs(admin = true)
    public void testDownloadExtensionPage() throws Exception {
        final DownloadBrowserExtensionPage downloadPage = BaseJiraWebTest.jira.visit(DownloadBrowserExtensionPage.class);
        assertNotNull("Download page must not be null", downloadPage);
        Poller.waitUntilTrue("Capture Download link is not present", downloadPage.isDownloadLinkVisible());
    }
}
