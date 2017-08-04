package it.com.atlassian.bonfire.toolbar;

import com.atlassian.bonfire.pageobjects.admin.CaptureAdminSettingsPage;
import com.atlassian.bonfire.pageobjects.projecttab.CaptureProjectTabPanel;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.config.LoginAs;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.test.categories.OnDemandAcceptanceTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Verifies Capture for JIRA is hidden for non-software projects
 *
 * @since v2.9.5
 */
@Category(OnDemandAcceptanceTest.class)
public class ProjectCentricNavigationAcceptanceTest extends BaseJiraWebTest {
    private static final String BUSINESS_PROJECT_KEY = "CPEXPRESS";
    private static final String BUSINESS_PROJECT_NAME = "Planet Express";

    private static final String SOFTWARE_PROJECT_KEY = "CETST";
    private static final String SOFTWARE_PROJECT_NAME = "Test Project";
    private static final String BUSINESS_PROJECT_TYPE = "business";
    private static final String SOFTWARE_PROJECT_TYPE = "software";

    private static final boolean SHOW = true;
    private static final boolean HIDE = false;

    @Before
    public void beforeTest() throws JSONException {
        final CaptureAdminSettingsPage settingsPage = navigateToPage(CaptureAdminSettingsPage.class);
        // Set Test Session Links to always be displayed for Business projects
        settingsPage.showCaptureForBusinessProjects(HIDE)
                .submit();

        ProjectDeleteHelper.deleteProjectSilently(jira.backdoor(), SOFTWARE_PROJECT_KEY);
        jira.backdoor().project().addProject(SOFTWARE_PROJECT_NAME, SOFTWARE_PROJECT_KEY, "admin", SOFTWARE_PROJECT_TYPE);

        ProjectDeleteHelper.deleteProjectSilently(jira.backdoor(), BUSINESS_PROJECT_KEY);
        jira.backdoor().project().addProject(BUSINESS_PROJECT_NAME, BUSINESS_PROJECT_KEY, "admin", BUSINESS_PROJECT_TYPE);
    }

    @After
    public void afterTest() throws Exception {
        ProjectDeleteHelper.deleteProjectSilently(jira.backdoor(), SOFTWARE_PROJECT_KEY);
        ProjectDeleteHelper.deleteProjectSilently(jira.backdoor(), BUSINESS_PROJECT_KEY);
    }

    @Test
    @LoginAs(admin = true)
    public void verifyCaptureToolbarVisibilityForBusinessAndSoftwareProjects() {
        final CaptureProjectTabPanel softwareProjectPage = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
        Poller.waitUntilTrue("Project Link has to be shown for software projects. Always.", softwareProjectPage.isTestSessionsLinkPresent());

        final CaptureProjectTabPanel businessProjectPage = navigateToPage(CaptureProjectTabPanel.class, BUSINESS_PROJECT_KEY);
        Poller.waitUntilFalse("Project Link has to be hidden for business projects. Because it's hidden by default.", businessProjectPage.isTestSessionsLinkPresent());

        final CaptureAdminSettingsPage settingsPage = navigateToPage(CaptureAdminSettingsPage.class);
        // Set Test Session Links to always be displayed for Business projects
        settingsPage.showCaptureForBusinessProjects(SHOW)
                .submit();
        Poller.waitUntilTrue("Settings have to be saved.", settingsPage.isSettingSaved());

        final CaptureProjectTabPanel softwareProjectPageAfter = navigateToPage(CaptureProjectTabPanel.class, SOFTWARE_PROJECT_KEY);
        Poller.waitUntilTrue("Project Link has to be present for software projects. Always.", softwareProjectPageAfter.isTestSessionsLinkPresent());

        // JIRA doesn't get the new configuration values for Capture instantly, it needs more time to propagate the changes.
        // We verify whether the link for business projects exists after we do it for software projects to
        // give JIRA some time to propagate the new configuration for Capture.
        final CaptureProjectTabPanel businessProjectPageAfter = navigateToPage(CaptureProjectTabPanel.class, BUSINESS_PROJECT_KEY);
        Poller.waitUntilTrue("Project Link has to be shown for business projects after the correspondent checkbox is set. See MON-76.", businessProjectPageAfter.isTestSessionsLinkPresent());
    }

    private <P extends Page> P navigateToPage(Class<P> pageClass, Object... args) {
        return jira.visit(pageClass, args);
    }
}
