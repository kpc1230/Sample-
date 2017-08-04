package it.com.atlassian.bonfire.rest;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import junit.framework.Assert;

import javax.ws.rs.core.MediaType;

public class TestNoteResource extends BonfireFuncTestCase {
    @Override
    public void setUpTest() {
        restoreData("capture-rest-data-with-notes.xml");
        runUpgradeTasks();
    }

    public void testToggleNoteResolution() throws JSONException, InterruptedException {
        long id = 10012;

        final Client client = setupClientConfig();

        WebResource post1 = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/notes/" + id + "/toggleResolution.json");
        post1.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        String postRespose = post1.type(MediaType.APPLICATION_JSON_TYPE).post(String.class, "");
        JSONObject note = JSONKit.to(postRespose);
        assertNotNull(note);
        Assert.assertEquals("COMPLETED", note.get("resolutionState"));

        WebResource post2 = client.resource("http://localhost:2990/jira/rest/bonfire/1.0/notes/" + note.get("id") + "/toggleResolution.json");
        post2.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
        note = JSONKit.to(post2.type(MediaType.APPLICATION_JSON_TYPE).post(String.class, ""));
        assertNotNull(note);
        Assert.assertEquals("INITIAL", note.get("resolutionState"));
    }
}
