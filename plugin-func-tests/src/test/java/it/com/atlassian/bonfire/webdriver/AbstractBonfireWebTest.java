package it.com.atlassian.bonfire.webdriver;

import java.util.Optional;
import com.atlassian.bonfire.CaptureTestRunner;
import com.atlassian.bonfire.util.CaptureTenantDatabaseProvisioner;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.rule.data.SqlDataImporter;
import com.atlassian.jira.functest.rule.tenant.TestTenantContext;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.webdriver.AtlassianWebDriver;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import it.com.atlassian.bonfire.webdriver.testrule.WebTestMethodRules;

import static com.atlassian.bonfire.util.RestoreJiraFromBackupCopy.LICENSE;

@RunWith(CaptureTestRunner.class)
public class AbstractBonfireWebTest {

    @Inject
    protected JiraTestedProduct jira;

    @Inject
    protected static AtlassianWebDriver webDriver;

    @Inject
    private CaptureTenantDatabaseProvisioner databaseProvisioner;

    @Rule
    public TestRule testRule = WebTestMethodRules.createRules(webDriver);

    @Before
    public void prepare() {
        jira.quickLoginAsSysadmin();
        jira.goToAdminHomePage();
    }

    /**
     * navigate to a page, if necessary
     */
    @SuppressWarnings("unchecked")
    protected <P extends AbstractJiraPage> P navigateToPage(Class<P> cls, Object... args) {
        return (P) jira.visit(cls, args);
    }

    public void restoreData(String fileName) {
        if(!jira.environmentData().isVertigoMode()){
            Backdoor backdoor = new Backdoor(jira.environmentData());
            backdoor.restoreDataFromResource(fileName, LICENSE);
        } else {
            Optional<String> tenantId = TestTenantContext.getCloudTenantId();
            if(!tenantId.isPresent()){
                CaptureTenantDatabaseProvisioner.setupVertigoTestTenant();
                tenantId = TestTenantContext.getCloudTenantId();
            }
            if(databaseProvisioner == null) {
                databaseProvisioner = new CaptureTenantDatabaseProvisioner(new SqlDataImporter(), jira.environmentData());
            }
            databaseProvisioner.create(tenantId.get(), fileName.replace(".xml", ".sql"));
        }
    }

    public void runUpgradeTasks() {
        ClientConfig clientConfig = new DefaultClientConfig();
        final Client client = Client.create(clientConfig);

        WebResource postResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/runUpgrade");
        postResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        postResource.post();
    }
}