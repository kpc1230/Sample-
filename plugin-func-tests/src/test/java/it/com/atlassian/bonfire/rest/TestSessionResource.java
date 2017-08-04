package it.com.atlassian.bonfire.rest;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
import util.BonfireBasicAuthFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test the Session REST resource
 *
 * @since v1.0
 */
public class TestSessionResource extends BonfireFuncTestCase {
    @Override
    public void setUpTest() {
        restoreData("capture-rest-data.xml");
        runUpgradeTasks();
    }

    public void testGetUsersSessions() throws IOException {

        JSONObject sessions = getSessionsForAdmin();
        JSONArray privateSessions = sessions.getJSONArray("privateSessions");

        assertEquals(2, privateSessions.length());
        for (int i = 0; i != privateSessions.length(); i++) {
            assertThat(privateSessions.getJSONObject(i).getString("user"), is("admin"));
        }
    }

    public void testGetSessionById() throws IOException {
        long id = 10007L;

        JSONObject session = getSpecificSessionForAdmin(id);

        assertThat(session.getLong("id"), is(id));
    }

    public void testJoinSharedSessionCreatedByAdmin() throws IOException {
        String name = "Shared Session Created From Integration Test";

        // ADMIN: Create a shared session and get it's id.
        JSONObject session = new JSONObject().put("name", name).put("projectKey", "TST").put("shared", "true");
        JSONObject sharedSession = createSessionAsAdmin(session);

        startSessionWithAdmin(sharedSession);
        joinSessionWithFry(sharedSession);

        // FRY: Check that I joined
        JSONArray frySharedSessions = getSessionsForFry().getJSONArray("sharedSessions");
        System.out.println(frySharedSessions);

        //I expect this condition to change
        assertThat(frySharedSessions.getJSONObject(0).getJSONArray("participants").length(), is(1));
        assertThat(frySharedSessions.getJSONObject(0).getJSONArray("participants").getJSONObject(0).getBoolean("hasLeft"), is(false));
    }

    public void testJoinSharedSessionCreatedByNonAdmin() throws IOException {
        String name = "Shared Session Created From Integration Test";
        JSONObject session = new JSONObject().put("name", name).put("projectKey", "TST").put("shared", "true");
        JSONObject sharedSession = createSessionAsFry(session);

        startSessionWithFry(sharedSession);
        joinSessionWithAdmin(sharedSession);

        // ADMIN: Check that I joined
        JSONArray adminSharedSessions = getSessionsForAdmin().getJSONArray("sharedSessions");

        // I expect this condition to change
        assertThat(adminSharedSessions.getJSONObject(0).getJSONArray("participants").length(), is(1));
        assertThat(adminSharedSessions.getJSONObject(0).getJSONArray("participants").getJSONObject(0).getBoolean("hasLeft"), is(false));
    }

    public void testLeaveSharedSession() throws IOException {
        String name = "Shared Session Created From Integration Test";
        JSONObject session = new JSONObject().put("name", name).put("projectKey", "TST").put("shared", "true");
        JSONObject sharedSession = createSessionAsAdmin(session);

        startSessionWithAdmin(sharedSession);
        joinSessionWithFry(sharedSession);

        // FRY: leave session
        leaveSessionWithFry(sharedSession);

        // FRY: check that I left
        JSONArray frySharedSessions = getSessionsForFry().getJSONArray("sharedSessions");

        //I expect this condition to change
        assertThat(frySharedSessions.getJSONObject(0).getInt("participantCount"), is(1));
    }

    public void testJoinAnotherSharedSession() throws IOException {
        JSONObject adminsSession = new JSONObject().put("name", "Shared Session Created From Integration Test").put("projectKey", "TST").put("shared", "true");
        JSONObject adminsSharedSession = createSessionAsAdmin(adminsSession);
        startSessionWithAdmin(adminsSharedSession);

        JSONObject farnsworthsSession = new JSONObject().put("name", "Another Shared Session Created From Integration Test").put("projectKey", "TST").put("shared", "true");
        JSONObject farnsworthsSharedSession = createSessionAsFarnsworth(farnsworthsSession);
        startSessionWithFarnsworth(farnsworthsSharedSession);

        joinSessionWithFry(adminsSharedSession);
        JSONArray frysSessions = getSessionsForFry().getJSONArray("sharedSessions");
        assertThat(frysSessions.getJSONObject(1).getJSONArray("participants").length(), is(1));
        assertThat(frysSessions.getJSONObject(1).getInt("participantCount"), is(2));
        assertThat(frysSessions.getJSONObject(1).getJSONArray("participants").getJSONObject(0).getBoolean("hasLeft"), is(false));
        assertThat(frysSessions.getJSONObject(0).getInt("participantCount"), is(1));

        joinSessionWithFry(farnsworthsSharedSession);
        JSONArray frysUpdatedSessions = getSessionsForFry().getJSONArray("sharedSessions");
        assertThat(frysUpdatedSessions.getJSONObject(1).getInt("participantCount"), is(1));
        assertThat(frysUpdatedSessions.getJSONObject(0).getJSONArray("participants").length(), is(1));
        assertThat(frysUpdatedSessions.getJSONObject(0).getInt("participantCount"), is(2));
        assertThat(frysUpdatedSessions.getJSONObject(0).getJSONArray("participants").getJSONObject(0).getBoolean("hasLeft"), is(false));
    }

    public void testPauseSharedSession() throws IOException {
        String name = "Shared Session Created From Integration Test";
        JSONObject sessionJson = (new JSONObject()).put("name", name).put("projectKey", "TST").put("shared", "true");
        JSONObject session = createSessionAsAdmin(sessionJson);

        startSessionWithAdmin(session);
        joinSessionWithFry(session);
        joinSessionWithFarnsworth(session);

        // ADMIN: pause the session
        pauseSessionWithAdmin(session);

        JSONObject sessions = getSessionsForAdmin();
        JSONArray adminSessions = sessions.getJSONArray("privateSessions");
        assertThat(adminSessions.getJSONObject(0).getInt("participantCount"), is(0));
    }

    public void testAddSessionNote() throws JSONException, InterruptedException, IOException {

        Long id = 10005L;
        JSONObject session = getSpecificSessionForAdmin(id);

        // First check the note isn't present
        assertThat("Session note is already present!", session.getInt("noteCount") > 0, is(false));

        // Add the session note
        String noteString = "This is a test note. There are many notes like it, but this note is for testing.";
        JSONObject note = new JSONObject().put("note", noteString);
        createSessionNote(id, note);


        // Check the note is now present - Bit dodgy, but it works.
        JSONObject sessionWithNote = getSpecificSessionForAdmin(id);
        assertThat(sessionWithNote.getInt("noteCount"), is(1));
    }

    public void testAddEmptySessionNoteReturns400() throws JSONException, IOException {
        Long id = 10005L;
        JSONObject note = new JSONObject().put("note", "");

        // Add an empty session note - returns 400
        HttpResponse response = createSessionNote(id, note);
        assertThat(response.getStatusLine().getStatusCode(), is(400));

    }

    @Test
    public void testCreateSessionWithMinimalData() throws JSONException, InterruptedException, IOException {
        String sessionName = "Session Created From Integration Test";

        JSONObject jsonSession = new JSONObject()
                .put("name", sessionName)
                .put("projectKey", "TST");

        JSONObject session = createSessionAsAdmin(jsonSession);

        assertThat(session.getString("name"), is(sessionName));
        assertThat(session.has("id"), is(true));
    }

    @Test
    public void testCreateSessionWithAllFields() throws IOException {
        String sessionName = "Session Created From Integration Test With All Fields";

        JSONObject jsonSession = new JSONObject()
                .put("name", sessionName)
                .put("projectKey", "TST")
                .put("assignee", "admin")
                .put("shared", "true")
                .put("defaultTemplateId", "1")
                .put("additionalInfo", "some kind of notes");

        JSONObject session = createSessionAsAdmin(jsonSession);

        assertThat(session.getString("name"), is(sessionName));
        assertThat(session.getBoolean("shared"), is(true));
        assertThat(session.has("id"), is(true));
    }

    @Test
    public void testCreateSessionWithOneRelatedIssue() throws IOException {
        String sessionName = "Session Created From Integration Test";
        List<String> relatedIssues = new ArrayList<String>();
        relatedIssues.add("TST-1");

        JSONObject jsonSession = new JSONObject()
                .put("name", sessionName)
                .put("projectKey", "TST")
                .put("issueKey", relatedIssues);

        JSONObject sessionResponse = createSessionAsAdmin(jsonSession);
        JSONObject session = getSpecificActiveSessionForAdmin(sessionResponse.getLong("id"));

        JSONArray issueKeys = session.getJSONArray("relatedIssues");
        assertThat(issueKeys.length(), is(1));

        String relatedIssueId = issueKeys.getJSONObject(0).getString("key");
        assertThat(relatedIssueId, is("TST-1"));
    }

    @Test
    public void testCreateSessionWithMultiRelatedIssues() throws IOException {
        String sessionName = "Session Created From Integration Test";
        List<String> relatedIssues = new ArrayList<String>();
        relatedIssues.add("TST-1");
        relatedIssues.add("TST-2");
        relatedIssues.add("TST-3");

        JSONObject jsonSession = new JSONObject()
                .put("name", sessionName)
                .put("projectKey", "TST")
                .put("issueKey", relatedIssues);

        JSONObject sessionResponse = createSessionAsAdmin(jsonSession);
        JSONObject session = getSpecificActiveSessionForAdmin(sessionResponse.getLong("id"));

        JSONArray issueKeys = session.getJSONArray("relatedIssues");
        assertThat(issueKeys.length(), is(3));

        assertThat(issueKeys.getJSONObject(2).getString("key"), is("TST-1"));
        assertThat(issueKeys.getJSONObject(1).getString("key"), is("TST-2"));
        assertThat(issueKeys.getJSONObject(0).getString("key"), is("TST-3"));
    }

    @Test
    public void testCreateSessionWithInvalidJson() throws IOException {
        HttpPost httpPost = buildCreateSessionPost("admin", "admin");
        httpPost.setEntity(new StringEntity("invalid"));
        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        assertThat(response.getStatusLine().getStatusCode(), is(400));
    }

    @Test
    public void testCreateSessionWithMissingFields() throws IOException {
        JSONObject jsonSession = new JSONObject();

        JSONObject session = createSessionAsAdmin(jsonSession);

        assertThat(session.has("errors"), is(true));
        assertThat(session.getJSONArray("errors").getJSONObject(0).getString("errorMessage"), is("Malformed session json"));
    }

    private JSONObject getSessions(String username, String password) throws IOException {
        final HttpGet get = buildGet("http://localhost:2990/jira/rest/bonfire/1.0/sessions/user", username, password);
        HttpResponse response = new DefaultHttpClient().execute(get);
        return JSONKit.to(response.getEntity().getContent());
    }

    private JSONObject getSessionsForFry() throws IOException {
        return getSessions("fry", "fry");
    }

    private HttpResponse createSessionNote(Long id, JSONObject note) throws IOException {
        HttpPost httpPost = buildPost("http://localhost:2990/jira/rest/bonfire/1.0/sessions/" + id + "/note", "admin", "admin");
        httpPost.setEntity(new StringEntity(note.toString()));

        return new DefaultHttpClient().execute(httpPost);
    }

    private JSONObject getSessionsForAdmin() throws IOException {
        return getSessions("admin", "admin");
    }

    private JSONObject getSpecificActiveSessionForAdmin(long id) throws IOException {
        final HttpGet get = buildGet("http://localhost:2990/jira/rest/bonfire/1.0/sessions/" + id + "?force=true", "admin", "admin");
        HttpResponse response = new DefaultHttpClient().execute(get);
        return JSONKit.to(response.getEntity().getContent());
    }

    private JSONObject getSpecificSessionForAdmin(long id) throws IOException {
        final HttpGet get = buildGet("http://localhost:2990/jira/rest/bonfire/1.0/sessions/" + id, "admin", "admin");
        HttpResponse response = new DefaultHttpClient().execute(get);
        return JSONKit.to(response.getEntity().getContent());
    }

    private HttpPost buildCreateSessionPost(String username, String password) {
        final HttpPost post = buildPost("http://localhost:2990/jira/rest/bonfire/1.0/sessions", username, password);
        return post;
    }

    private void leaveSessionWithFry(JSONObject sharedSession) throws IOException {
        HttpDelete delete = leaveSession(sharedSession, "fry", "fry");
        new DefaultHttpClient().execute(delete);
    }

    private HttpDelete leaveSession(JSONObject sharedSession, String username, String password) {
        HttpDelete delete = new HttpDelete("http://localhost:2990/jira/rest/bonfire/1.0/sessions/" + sharedSession.getString("id") + "/participate");
        delete.setHeader("Authorization", new BonfireBasicAuthFilter(username, password).getAuthenticationString());
        return delete;
    }

    private JSONObject createSessionAsAdmin(JSONObject session) throws IOException {
        return createSession(session, "admin", "admin");
    }

    private JSONObject createSessionAsFarnsworth(JSONObject session) throws IOException {
        return createSession(session, "farnsworth", "farnsworth");
    }

    private JSONObject createSessionAsFry(JSONObject session) throws IOException {
        return createSession(session, "fry", "fry");
    }

    private JSONObject createSession(JSONObject session, String username, String password) throws IOException {
        final HttpPost post = buildCreateSessionPost(username, password);
        post.setEntity(new StringEntity(session.toString()));
        HttpResponse response = new DefaultHttpClient().execute(post);
        return JSONKit.to(response.getEntity().getContent());
    }


    private void joinSessionWithFry(JSONObject session) throws IOException {
        joinSession(session, "fry", "fry");
    }

    private void joinSessionWithFarnsworth(JSONObject session) throws IOException {
        joinSession(session, "farnsworth", "farnsworth");

    }

    private void joinSession(JSONObject sharedSession, String username, String password) throws IOException {
        HttpPost post = buildPost("http://localhost:2990/jira/rest/bonfire/1.0/sessions/" + sharedSession.getString("id") + "/participate", username, password);
        new DefaultHttpClient().execute(post);
    }

    private void joinSessionWithAdmin(JSONObject sharedSession) throws IOException {
        joinSession(sharedSession, "admin", "admin");
    }

    private void pauseSessionWithAdmin(JSONObject session) throws IOException {
        pauseSession(session, "admin", "admin");
    }

    private void pauseSession(JSONObject session, String username, String password) throws IOException {
        HttpPost httpPost = buildPost("http://localhost:2990/jira/rest/bonfire/1.0/sessions/" + session.getString("id") + "/pause", username, password);
        new DefaultHttpClient().execute(httpPost);
    }

    private void startSession(JSONObject session, String username, String password) throws IOException {
        HttpPost httpPost = buildPost("http://localhost:2990/jira/rest/bonfire/1.0/sessions/" + session.getString("id") + "/start", username, password);
        new DefaultHttpClient().execute(httpPost);
    }

    private void startSessionWithFarnsworth(JSONObject session) throws IOException {
        startSession(session, "farnsworth", "farnsworth");
    }

    private void startSessionWithFry(JSONObject session) throws IOException {
        startSession(session, "fry", "fry");
    }

    private void startSessionWithAdmin(JSONObject session) throws IOException {
        startSession(session, "admin", "admin");

    }


}
