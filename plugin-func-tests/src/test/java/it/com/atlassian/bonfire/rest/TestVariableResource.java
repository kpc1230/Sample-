package it.com.atlassian.bonfire.rest;


import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import util.BonfireBasicAuthFilter;

import javax.ws.rs.core.MediaType;

/**
 * Tests for the VariableResource
 *
 * @since v1.9
 */
public class TestVariableResource extends BonfireFuncTestCase {
    private Client client;

    @Override
    public void setUpTest() {
        restoreData("capture-rest-data.xml");
        runUpgradeTasks();

        client = Client.create();
    }

    @Override
    protected void tearDownTest() {
        client.destroy();
    }

    private void createVariable(Client client, JSONObject variableJSON) {
        createVariableAsAUser(client, variableJSON, new BonfireBasicAuthFilter("admin", "admin"));
    }

    private void createVariableAsAUser(Client client, JSONObject variableJSON, ClientFilter clientFilter) {
        // TODO Should this look at properties and determine domain name, port, and context path?
        final WebResource variablesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/variables");

        variablesResource.addFilter(clientFilter);

        variablesResource.type(MediaType.APPLICATION_JSON_TYPE).post(variableJSON.toString());
    }

    private void createTemplate(Client client, JSONObject templateJSON) {
        final WebResource templatesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates");
        templatesResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        templatesResource.type(MediaType.APPLICATION_JSON_TYPE).post(templateJSON.toString());
    }

    public void testCreateVariable() throws Exception {
        final WebResource variablesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/variables");
        variablesResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject resultData = JSONKit.to(variablesResource.get(String.class));
        JSONArray result = resultData.getJSONArray("variables");

        assertTrue("Only default variables should be present", result.length() == 4);

        JSONObject variableJSON = new JSONObject().put("name", "foo").put("value", "bar");
        createVariable(client, variableJSON);

        resultData = JSONKit.to(variablesResource.get(String.class));
        result = resultData.getJSONArray("variables");

        assertTrue("Newly created variable should be present", result.length() == 5);
    }

    public void testUpdateVariable() throws Exception {
        final WebResource variablesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/variables");
        variablesResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject resultData = JSONKit.to(variablesResource.get(String.class));
        JSONArray result = resultData.getJSONArray("variables");

        assertTrue("Only default variables should be present", result.length() == 4);

        JSONObject variableJSON = new JSONObject().put("name", "foo").put("value", "bar");
        createVariable(client, variableJSON);

        resultData = JSONKit.to(variablesResource.get(String.class));
        result = resultData.getJSONArray("variables");

        assertTrue("Newly created variable should be present", result.length() == 5);

        // Now edit
        JSONObject variable = (JSONObject) result.get(4);
        variable.put("name", "foobar");
        variablesResource.type(MediaType.APPLICATION_JSON).put(variable.toString());

        resultData = JSONKit.to(variablesResource.get(String.class));
        result = resultData.getJSONArray("variables");

        assertTrue("Newly created variable should be present", variable.equals(result.get(4)));
    }

    public void testUpdateVariableWithoutPermission() throws Exception {
        int errorStatusCode = -1;

        final WebResource variablesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/variables");
        variablesResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject resultData = JSONKit.to(variablesResource.get(String.class));
        JSONArray result = resultData.getJSONArray("variables");

        JSONObject variable = (JSONObject) result.get(0);
        variable.put("name", "foobar");

        variablesResource.addFilter(new HTTPBasicAuthFilter("fry", "fry"));

        try {
            variablesResource.type(MediaType.APPLICATION_JSON).put(variable.toString());
        } catch (UniformInterfaceException exception) {
            errorStatusCode = exception.getResponse().getStatus();
        }

        boolean correctResult = (errorStatusCode == 400 || errorStatusCode == 401);
        assertTrue("Status code from response should be 400 or 401", correctResult);
    }

    // TEMPLATE INTERPLAY TESTS

    // Test create variable, create template that uses that variable (should see var show up in template)

    public void testCreateTemplateUsingVariable() throws Exception {
        createVariable(client, new JSONObject().put("name", "foo").put("value", "bar"));
        JSONObject templateJSON = new JSONObject(
                "{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"{foo}\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");

        createTemplate(client, templateJSON);

        final WebResource getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user");
        getResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(getResource.get(String.class));
        JSONArray templates = resultObj.getJSONArray("templates");

        JSONObject template = (JSONObject) templates.get(0);
        assertTrue("Single variable should be present in template", template.getJSONArray("variables").length() == 1);
    }

    // Test create template with a variable reference, create variable that satisfies that reference (should see var show up in template)

    public void testCreateTemplateUsingLaterCreatedVariable() throws Exception {
        JSONObject templateJSON = new JSONObject(
                "{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"{foo}\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");

        createTemplate(client, templateJSON);
        createVariable(client, new JSONObject().put("name", "foo").put("value", "bar"));

        final WebResource getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user");
        getResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(getResource.get(String.class));
        JSONArray templates = resultObj.getJSONArray("templates");

        JSONObject template = (JSONObject) templates.get(0);
        assertTrue("Single variable should be present in template", template.getJSONArray("variables").length() == 1);
    }

    // Test create variable, create template that uses that variable, delete variable (should see var deleted from template) - might not be able to do this

    // Test create variable, create template that uses that variable, update variable (should see new var show up in template)
    public void testUpdateVariableUsedInTemplate() throws Exception {
        createVariable(client, new JSONObject().put("name", "foo").put("value", "bar"));
        JSONObject templateJSON = new JSONObject(
                "{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"{foo}\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");

        createTemplate(client, templateJSON);

        final WebResource templatesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user");
        templatesResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(templatesResource.get(String.class));
        JSONArray templates = resultObj.getJSONArray("templates");

        JSONObject template = (JSONObject) templates.get(0);
        assertTrue("Single variable should be present in template", template.getJSONArray("variables").length() == 1);

        // Now edit
        final WebResource variablesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/variables");
        variablesResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject variable = (JSONObject) template.getJSONArray("variables").get(0);
        variable.put("value", "foobar");
        variablesResource.type(MediaType.APPLICATION_JSON).put(variable.toString());

        // And verify the new variable is in the template
        resultObj = JSONKit.to(templatesResource.get(String.class));
        templates = resultObj.getJSONArray("templates");

        template = (JSONObject) templates.get(0);
        assertTrue("Single variable should be present in template", template.getJSONArray("variables").length() == 1);
        assertEquals("Updated variable should be present in the template", ((JSONObject) template.getJSONArray("variables").get(0)).get("value"), "foobar");
    }

    public void testLoadAllVariablesForAdmin() throws Exception {
        // First - browse the default variables resource, it will create variables if ones do not exist yet
        final WebResource variablesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/variables");
        variablesResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        JSONObject resultData = JSONKit.to(variablesResource.get(String.class));
        JSONArray result = resultData.getJSONArray("variables");

        assertEquals("Only default variables should be present", 4, result.length());

        JSONObject variableJSON = new JSONObject().put("name", "foo").put("value", "bar");
        createVariableAsAUser(client, variableJSON, new BonfireBasicAuthFilter("fry", "fry"));

        variableJSON = new JSONObject().put("name", "dee").put("value", "dum");
        createVariable(client, variableJSON);

        // Later - browse the admin variables resource to see all variables that are created
        final WebResource variablesAdminResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/variables/admin");
        variablesAdminResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        resultData = JSONKit.to(variablesAdminResource.get(String.class));
        result = resultData.getJSONArray("variables");

        assertEquals("Newly created variable should be present", 6, result.length());
    }
}
