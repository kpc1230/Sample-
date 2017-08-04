package it.com.atlassian.bonfire.util;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.locator.IdLocator;
import com.atlassian.jira.functest.rule.data.SqlDataImporter;
import com.atlassian.jira.functest.rule.tenant.TestTenantContext;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONObject;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import util.BonfireBasicAuthFilter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.bonfire.util.CaptureTenantDatabaseProvisioner;

import static com.atlassian.bonfire.util.RestoreJiraFromBackupCopy.LICENSE;

/**
 */
public abstract class BonfireFuncTestCase extends FuncTestCase {
    private static final Pattern PATTERN_BUILD_NUMBER = Pattern.compile("v(\\d+).(\\d+)");

    private CaptureTenantDatabaseProvisioner databaseProvisioner;

    /**
     * @return the JIRA version in play
     */
    public int[] getJiraVersion() {
        final IdLocator idLocator = new IdLocator(tester, "footer-build-information");
        final String buildInfo = idLocator.getText();

        if (StringUtils.isBlank(buildInfo)) {
            throw new RuntimeException("Unable to find build information in the footer.");
        }

        final Matcher matcher = PATTERN_BUILD_NUMBER.matcher(buildInfo);
        if (!matcher.find()) {
            throw new RuntimeException("Unable to find build number from the footer.");
        }

        try {
            String major = matcher.group(1);
            String min = matcher.group(2);
            return new int[]{Integer.parseInt(major), Integer.parseInt(min)};
        } catch (NumberFormatException e) {
            throw new RuntimeException("Unable to find builder number from the footer.", e);
        }
    }


    public void runUpgradeTasks() {
        final Client client = setupClientConfig();

        WebResource postResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/runUpgrade");
        postResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        postResource.post();
    }

    public Client setupClientConfig() {
        ClientConfig clientConfig = new DefaultClientConfig();

        return Client.create(clientConfig);
    }

    protected JSONArray getJSONArray(Client client, final String restURL) {
        final WebResource resource = client.resource(restURL);
        resource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        return JSONKit.toArray(resource.get(String.class));
    }

    protected JSONObject getJSON(Client client, final String url) {
        final WebResource resource = client.resource(url);
        resource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        return JSONKit.to(resource.get(String.class));
    }

    protected HttpGet buildGet(String uri, String username, String password) {
        final HttpGet get = new HttpGet(uri);
        get.setHeader("Authorization", new BonfireBasicAuthFilter(username, password).getAuthenticationString());
        get.setHeader("Accept", "application/json");
        return get;
    }

    protected HttpPost buildPost(String uri, String username, String password) {
        final HttpPost post = new HttpPost(uri);
        post.setHeader("Authorization", new BonfireBasicAuthFilter(username, password).getAuthenticationString());
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");
        return post;
    }

    public void restoreData(String fileName) {
        if(!environmentData.isVertigoMode()){
            Backdoor backdoor = new Backdoor(environmentData);
            backdoor.restoreDataFromResource(fileName, LICENSE);
        } else {
            Optional<String> tenantId = TestTenantContext.getCloudTenantId();
            if(!tenantId.isPresent()){
                CaptureTenantDatabaseProvisioner.setupVertigoTestTenant();
                tenantId = TestTenantContext.getCloudTenantId();
            }
            if(databaseProvisioner == null) {
                databaseProvisioner = new CaptureTenantDatabaseProvisioner(new SqlDataImporter(), environmentData);
            }
            databaseProvisioner.create(tenantId.get(), fileName.replace(".xml", ".sql"));
        }

    }
}
