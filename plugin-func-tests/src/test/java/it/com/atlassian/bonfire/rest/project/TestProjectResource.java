package it.com.atlassian.bonfire.rest.project;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import junit.framework.Assert;


public class TestProjectResource extends BonfireFuncTestCase {
    @Override
    public void setUpTest() {
        restoreData("capture-rest-data.xml");
        runUpgradeTasks();
    }

    public void testGetProjects() {
        final Client client = setupClientConfig();

        // All issue types are configured to have an empty screen
        final WebResource resource = client.resource(getEnvironmentData().getBaseUrl().toExternalForm() + "/rest/bonfire/1.0/project");

        resource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject projectsRoot = JSONKit.to(resource.get(String.class));
        JSONArray projects = projectsRoot.getJSONArray("projects");

        assertEquals(2, projects.length());

        assertContainsProject(projects, "10001", "PEXPRESS", "Planet Express");
        assertContainsProject(projects, "10000", "TST", "Test");
    }

    private void assertContainsProject(JSONArray projects, String id, String key, String name) {
        for (int i = 0; i != projects.length(); i++) {
            JSONObject project = projects.getJSONObject(i);
            if (id.equals(project.get("id"))) {
                Assert.assertEquals(key, project.get("key"));
                Assert.assertEquals(name, project.get("name"));
                return;
            }
        }
        fail("Cant find desired project " + key);
    }

}
