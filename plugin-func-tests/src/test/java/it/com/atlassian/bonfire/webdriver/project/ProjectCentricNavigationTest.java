package it.com.atlassian.bonfire.webdriver.project;

import com.atlassian.bonfire.pageobjects.admin.CaptureAdminSettingsPage;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTabPanel;
import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.pageobjects.elements.query.Poller;
import it.com.atlassian.bonfire.webdriver.AbstractBonfireWebTest;
import org.junit.Test;

/**
 * Tests Capture project sidebar visibility
 * <p>
 * IMPORTANT! To be tested with JIRA 7.0.0 and later!
 * </p>
 */
@Restore("capture-different-project-types.xml")
public class ProjectCentricNavigationTest extends AbstractBonfireWebTest {
    private static final String BUSINESS_PROJECT = "PEXPRESS";
    private static final String SOFTWARE_PROJECT = "TST";
    private static final boolean SHOW = true;

    @Test
    public void verifyCaptureToolbarVisibilityForBusinessAndSoftwareProjects() {
        final CaptureProjectTabPanel softwareProjectPage = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT);
        Poller.waitUntilTrue("Project Link has to be shown for software projects. Always.", softwareProjectPage.isTestSessionsLinkPresent());

        final CaptureProjectTabPanel businessProjectPage = navigateToPage(CaptureProjectTabPanel.class, BUSINESS_PROJECT);
        Poller.waitUntilFalse("Project Link has to be hidden for business projects. Because it's hidden by default.", businessProjectPage.isTestSessionsLinkPresent());

        final CaptureAdminSettingsPage settingsPage = navigateToPage(CaptureAdminSettingsPage.class);
        // Set Test Session Links to always be displayed for Business projects
        settingsPage.showCaptureForBusinessProjects(SHOW)
                .submit();
        Poller.waitUntilTrue("Settings have to be saved.", settingsPage.isSettingSaved());

        final CaptureProjectTabPanel softwareProjectPageAfter = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT);
        Poller.waitUntilTrue("Project Link has to be present for software projects. Always.", softwareProjectPageAfter.isTestSessionsLinkPresent());

        // JIRA doesn't get the new configuration values for Capture instantly, it needs more time to propagate the changes.
        // We verify whether the link for business projects exists after we do it for software projects to
        // give JIRA some time to propagate the new configuration for Capture.
        final CaptureProjectTabPanel businessProjectPageAfter = navigateToPage(CaptureProjectTabPanel.class, BUSINESS_PROJECT);
        Poller.waitUntilTrue("Project Link has to be shown for business projects after the correspondent checkbox is set. See MON-76.", businessProjectPageAfter.isTestSessionsLinkPresent());
    }

    // BON-43031 Unable to access Notes from JIRA Project in JIRA Capture
    @Test
    public void verifyLinkToNotesViewOnProjectPageAndUserProfile() {
        final CaptureProjectTabPanel softwareProjectPage = navigateToProjectNotesView();
        Poller.waitUntilTrue("Link to Notes page should work correctly", softwareProjectPage.isOnNotesPage());
    }

    // BON-43321 Page unavailable when filtering test sessions from JIRA project
    @Test
    public void verifyFilteringOnProjectPageAndUserProfile() {
        final CaptureProjectTabPanel softwareProjectPage = navigateToProjectNotesView();
        softwareProjectPage.applyFilter();
        Poller.waitUntilTrue("Applying filter should redirect to Notes view page", softwareProjectPage.isOnNotesPage());
    }

    private CaptureProjectTabPanel navigateToProjectNotesView() {
        final CaptureProjectTabPanel softwareProjectPage = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT);
        Poller.waitUntilTrue("Project Link has to be shown for software projects. Always.", softwareProjectPage.isTestSessionsLinkPresent());

        softwareProjectPage.gotoSessionTab();
        softwareProjectPage.goToNotesView();
        return softwareProjectPage;
    }
}
