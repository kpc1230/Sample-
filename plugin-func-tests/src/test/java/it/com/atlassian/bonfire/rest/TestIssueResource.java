package it.com.atlassian.bonfire.rest;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.json.JSONObject;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

import java.io.IOException;

public class TestIssueResource extends BonfireFuncTestCase {
    @Override
    public void setUpTest() {
        restoreData("capture-rest-data.xml");
        runUpgradeTasks();
    }

    // This test relies on a JIRA rest endpoint. I hope they don't change it too often
    @Test
    public void testAddComment() throws IOException {
        JSONObject commentResponse = getCommentsFromJIRA("admin", "admin", "TST-1");
        int length = commentResponse.getJSONArray("comments").length();
        assertEquals(0, length);

        postCommentToJIRA("admin", "admin", "TST-1", "{\"comment\":\"test 1\"}");

        commentResponse = getCommentsFromJIRA("admin", "admin", "TST-1");
        length = commentResponse.getJSONArray("comments").length();
        assertEquals(1, length);

        postCommentToJIRA("admin", "admin", "TST-1", "{\"comment\":\"test 2\",\"visibilityType\":\"group\"}");

        commentResponse = getCommentsFromJIRA("admin", "admin", "TST-1");
        length = commentResponse.getJSONArray("comments").length();
        assertEquals(2, length);

        postCommentToJIRA("admin", "admin", "TST-1", "{\"comment\":\"test 3\",\"visibilityType\":\"role\"}");

        commentResponse = getCommentsFromJIRA("admin", "admin", "TST-1");
        length = commentResponse.getJSONArray("comments").length();
        assertEquals(3, length);

        // Restricted comment to admin
        postCommentToJIRA("admin", "admin", "TST-1", "{\"comment\":\"test 4\",\"visibilityType\":\"role\", \"roleId\":\"10002\"}");

        commentResponse = getCommentsFromJIRA("admin", "admin", "TST-1");
        length = commentResponse.getJSONArray("comments").length();
        assertEquals(4, length);

        commentResponse = getCommentsFromJIRA("fry", "fry", "TST-1");
        length = commentResponse.getJSONArray("comments").length();
        assertEquals(3, length);
    }

    private JSONObject getCommentsFromJIRA(String username, String password, String issueKey) throws IOException {
        final HttpGet get = buildGet("http://localhost:2990/jira/rest/api/2/issue/" + issueKey + "/comment", username, password);
        HttpResponse response = new DefaultHttpClient().execute(get);
        return JSONKit.to(response.getEntity().getContent());
    }

    private JSONObject postCommentToJIRA(String username, String password, String issueKey, String jsonString) throws IOException {
        final HttpPost post = buildPost("http://localhost:2990/jira/rest/bonfire/1.0/issue/" + issueKey + "/comment", username, password);
        post.setEntity(new StringEntity(jsonString));
        HttpResponse response = new DefaultHttpClient().execute(post);
        return JSONKit.to(response.getEntity().getContent());
    }
}
