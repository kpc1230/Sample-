package it.com.atlassian.bonfire.webdriver.admin;

import com.atlassian.bonfire.pageobjects.admin.CaptureLicensePage;
import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.pageobjects.elements.query.Poller;
import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;
import org.junit.Test;
import util.BonfireTestingConstants;

import static org.junit.Assert.assertNotNull;

@RestoreOnce("empty.xml")
public class CaptureAdminTest extends AbstractBonfireWebTest {
    private static final String BONFIRE_INVALID_LICENSE = "This is an invalid license";

    @Test
    public void testBonfireLicensePageFailure() {
        CaptureLicensePage page = navigateToPage(CaptureLicensePage.class);
        assertNotNull(page);
        Poller.waitUntilFalse("Expected no error is shown", page.hasError());
        page = page.enterLicense(BONFIRE_INVALID_LICENSE);
        Poller.waitUntilTrue("Expected error shown for invalid license", page.hasError());
    }

    @Test
    public void testBonfireLicensePage() {
        CaptureLicensePage page = navigateToPage(CaptureLicensePage.class);
        assertNotNull(page);
        Poller.waitUntilFalse("Expected no error is shown", page.hasError());
        page = page.enterLicense(BonfireTestingConstants.BONFIRE_COMMERCIAL_LICENSE);
        Poller.waitUntilFalse("Expected no error is shown", page.hasError());
    }
}
