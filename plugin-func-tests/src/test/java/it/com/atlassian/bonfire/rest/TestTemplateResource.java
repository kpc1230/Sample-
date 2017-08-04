package it.com.atlassian.bonfire.rest;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import util.BonfireBasicAuthFilter;

import javax.ws.rs.core.MediaType;

/**
 * Tests for the Template Resource
 *
 * @since v1.7
 */
public class TestTemplateResource extends BonfireFuncTestCase {
    private Client client;

    @Override
    public void setUpTest() {
        restoreData("capture-rest-data.xml");
        runUpgradeTasks();

        client = Client.create();
    }

    @Override
    public void tearDownTest() {
        client.destroy();
    }

    private void createTemplate(Client client, JSONObject templateJSON) {
        createTemplateAsUser(client, templateJSON, new BonfireBasicAuthFilter("admin", "admin"));
    }

    private void createTemplateAsUser(Client client, JSONObject templateJSON, ClientFilter clientFilter) {
        // TODO Should this look at properties and determine domain name, port, and context path?
        final WebResource templatesResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates");

        templatesResource.addFilter(clientFilter);

        templatesResource.type(MediaType.APPLICATION_JSON_TYPE).post(templateJSON.toString());
    }

    public void testCreateTemplate() {
        JSONObject templateJSON = JSONKit
                .to("{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");
        createTemplate(client, templateJSON);

        final WebResource getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user");
        getResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(getResource.get(String.class));
        JSONArray result = resultObj.getJSONArray("templates");

        // And verify that it is present
        assertTrue("Newly created template should be present", result.length() == 1);
    }

    public void testUpdateTemplate() throws JSONException {
        JSONObject originalTemplateJSON = JSONKit
                .to("{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");
        createTemplate(client, originalTemplateJSON);

        // Get out the resulting JSON
        final WebResource getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user");
        getResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(getResource.get(String.class));
        JSONArray templates = resultObj.getJSONArray("templates");
        JSONObject templateJSON = (JSONObject) templates.get(0);
        templateJSON.put("summary", "Updated template");

        final WebResource templateResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/");
        templateResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        // And put it back to the server
        try {

            templateResource.type(MediaType.APPLICATION_JSON_TYPE).put(templateJSON.toString());

        } catch (UniformInterfaceException exception) {
            assertTrue(false);
        }

        JSONObject endResultObj = JSONKit.to(getResource.get(String.class));
        JSONArray endResult = endResultObj.getJSONArray("templates");

        // Verify that it has been modified
        JSONObject resultTemplateJson = (JSONObject) endResult.get(0);
        assertTrue("Modified template should be present", resultTemplateJson.get("source").equals(templateJSON.get("source")));
    }

    public void testUpdateTemplateWithoutPermission() throws JSONException {
        int errorStatusCode = -1;

        JSONObject originalTemplateJSON = JSONKit
                .to("{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");
        createTemplate(client, originalTemplateJSON);

        // Get out the resulting JSON
        final WebResource getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user");
        getResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(getResource.get(String.class));
        JSONArray templates = resultObj.getJSONArray("templates");
        JSONObject templateJSON = (JSONObject) templates.get(0);
        templateJSON.put("summary", "Updated template");


        final WebResource templateResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/");
        templateResource.addFilter(new BonfireBasicAuthFilter("fry", "fry"));

        // And put it back to the server
        try {
            templateResource.type(MediaType.APPLICATION_JSON_TYPE).put(templateJSON.toString());
        } catch (UniformInterfaceException exception) {
            errorStatusCode = exception.getResponse().getStatus();
        }

        boolean correctResult = (errorStatusCode == 400 || errorStatusCode == 401);
        assertTrue("Status code from response should be 400 or 401", correctResult);
    }

    public void testTemplatePagination() throws JSONException {
        // Create two templates with different names
        JSONObject originalTemplateJSON = JSONKit
                .to("{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");
        createTemplate(client, originalTemplateJSON);
        originalTemplateJSON = JSONKit
                .to("{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"\",\"name\":\"test2\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");
        createTemplate(client, originalTemplateJSON);

        // Get templates startAt 0 length 1
        WebResource getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user?startAt=0&maxResults=1");
        getResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(getResource.get(String.class));
        JSONArray result = resultObj.getJSONArray("templates");
        JSONObject templateResult = (JSONObject) result.get(0);
        JSONObject templateObj = JSONKit.to(templateResult.getString("source"));
        assertEquals("test2", templateObj.getString("name"));
        assertTrue(resultObj.getBoolean("hasMore"));

        // Get templates startAt 0 length 1
        getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/user?startAt=1&maxResults=1");
        getResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        resultObj = JSONKit.to(getResource.get(String.class));
        result = resultObj.getJSONArray("templates");
        templateResult = (JSONObject) result.get(0);
        templateObj = JSONKit.to(templateResult.getString("source"));
        assertEquals("test", templateObj.getString("name"));
        assertFalse(resultObj.getBoolean("hasMore"));
    }


    public void testLoadAllTemplatesForAdmin() throws JSONException {
        // Template created by a fry user
        JSONObject originalTemplateJSON = JSONKit
                .to("{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"\",\"name\":\"test\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");
        createTemplateAsUser(client, originalTemplateJSON, new BonfireBasicAuthFilter("fry", "fry"));

        // Template created by a admin user
        originalTemplateJSON = JSONKit
                .to("{\"summary\":\"\",\"remainingEstimate\":\"\",\"screenshot\":true,\"customfields\":\"\",\"issueType\":\"1\",\"labels\":\"\",\"assignee\":\"-1\",\"fixVersions\":{},\"timeSpent\":\"\",\"originalEstimate\":\"\",\"project\":{\"screenIndex\":-1,\"visible\":true,\"value\":\"10001\"},\"versions\":{},\"environment\":\"\",\"priority\":\"3\",\"description\":\"\",\"name\":\"test2\",\"components\":{},\"started\":\"\",\"dueDate\":\"\",\"key\":\"10009\",\"securityLevel\":\"\"}");
        createTemplate(client, originalTemplateJSON);

        // Get templates startAt 0 length 1
        WebResource getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/admin?startAt=0&maxResults=1");
        getResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        JSONObject resultObj = JSONKit.to(getResource.get(String.class));
        JSONArray result = resultObj.getJSONArray("templates");
        JSONObject templateResult = (JSONObject) result.get(0);
        JSONObject templateObj = JSONKit.to(templateResult.getString("source"));
        assertEquals("test", templateObj.getString("name"));
        assertTrue(resultObj.getBoolean("hasMore"));

        // Get templates startAt 0 length 1
        getResource = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/templates/admin?startAt=1&maxResults=1");
        getResource.addFilter(new BonfireBasicAuthFilter("admin", "admin"));

        resultObj = JSONKit.to(getResource.get(String.class));
        result = resultObj.getJSONArray("templates");
        templateResult = (JSONObject) result.get(0);
        templateObj = JSONKit.to(templateResult.getString("source"));
        assertEquals("test2", templateObj.getString("name"));
        assertFalse(resultObj.getBoolean("hasMore"));
    }
}
