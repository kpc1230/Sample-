package it.com.atlassian.bonfire.rest.search;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONObject;
import com.sun.jersey.api.client.Client;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Tests JIRA Capture auto-complete resource
 *
 * @since v2.9
 */
public class TestSearchResource extends BonfireFuncTestCase {
    private static final String BASE_CAPTURE_SEARCH_URL = "http://localhost:2990/jira/rest/bonfire/1.0/issueSearch/";
    private static final String ISSUE_AUTOCOMPLETE_URL = BASE_CAPTURE_SEARCH_URL + "autocomplete?term=";
    private static final String EPIC_AUTOCOMPLETE_URL = BASE_CAPTURE_SEARCH_URL + "autocompleteEpic?term=";

    private Client client;

    @Override
    public void setUpTest() {
        restoreData("capture-jira-agile-epics-data.xml");
        runUpgradeTasks();

        client = Client.create();
    }

    @Override
    public void tearDownTest() {
        client.destroy();
    }

    public void testIssueAutocomplete() throws IOException {
        final JSONObject response = getJsonForAdmin(issueSearchUrl("FULL-5"));

        assertFalse("Error indicated by a status-code in response", response.has("status-code"));

        final String value = response.getJSONArray("searchResult").getJSONObject(0).getString("value");
        assertEquals("FULL-5", value);
    }

    public void testIssueAutocompleteNotFound() throws IOException {
        final JSONObject response = getJsonForAdmin(issueSearchUrl("NOTFOUND"));

        assertFalse("Error indicated by a status-code in response", response.has("status-code"));

        final String label = response.getJSONArray("searchResult").getJSONObject(0).getString("label");
        assertEquals("No matches", label);
    }

    public void testEpicAutocompleteByProjectFound() throws IOException {
        final JSONObject response = getJsonForAdmin(epicSearchUrl("FULL"));

        assertFalse("Error indicated by a status-code in response", response.has("status-code"));

        final String value = response.getJSONArray("searchResult").getJSONObject(0).getString("value");
        assertEquals("FULL-5", value);
    }

    public void testEpicAutocompleteBySummaryFound() throws IOException {
        final JSONObject response = getJsonForAdmin(epicSearchUrl("A very important"));

        assertFalse("Error indicated by a status-code in response", response.has("status-code"));

        final String value = response.getJSONArray("searchResult").getJSONObject(0).getString("value");
        assertEquals("FULL-5", value);
    }

    public void testEpicAutocompleteByEpicKeyFound() throws IOException {
        final JSONObject response = getJsonForAdmin(epicSearchUrl("FULL-5"));

        assertFalse("Error indicated by a status-code in response", response.has("status-code"));

        final String value = response.getJSONArray("searchResult").getJSONObject(0).getString("value");
        assertEquals("FULL-5", value);
    }

    public void testEpicAutocompleteNotFound() throws IOException {
        final JSONObject response = getJsonForAdmin(epicSearchUrl("NOTFOUND"));

        assertFalse("Error indicated by a status-code in response", response.has("status-code"));

        final String label = response.getJSONArray("searchResult").getJSONObject(0).getString("label");
        assertEquals("No matches", label);
    }

    public void testEpicAutocompleteNotSuggestingIssues() throws IOException {
        final JSONObject response = getJsonForAdmin(epicSearchUrl("FULL-2"));

        assertFalse("Error indicated by a status-code in response", response.has("status-code"));

        final String label = response.getJSONArray("searchResult").getJSONObject(0).getString("label");
        assertEquals("No matches", label);
    }


    private JSONObject getJsonForAdmin(String url) throws IOException {
        final HttpGet get = buildGet(url, "admin", "admin");
        HttpResponse response = new DefaultHttpClient().execute(get);
        return JSONKit.to(response.getEntity().getContent());
    }

    private String issueSearchUrl(String term) throws UnsupportedEncodingException {
        return ISSUE_AUTOCOMPLETE_URL + URLEncoder.encode(term, "UTF-8");
    }

    private String epicSearchUrl(String term) {
        try {
            return EPIC_AUTOCOMPLETE_URL + URLEncoder.encode(term, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
