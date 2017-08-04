package it.com.atlassian.bonfire.rest;

import com.atlassian.core.util.ClassLoaderUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;

import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestIssueImageAttachmentResource extends BonfireFuncTestCase {
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

    public void testAttachSingleImage() throws IOException {
        // Check there aren't any attachments first

        final WebResource getResource = client.resource(getEnvironmentData().getBaseUrl().toExternalForm() + "/rest/api/latest/issue/TST-1");

        getResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        final String issueWithoutAttachment = getResource.get(new GenericType<String>() {
        });

        // TODO Make this test more robust - should be pulling in the JSON Object and asserting that there are no attachments
        assertFalse(issueWithoutAttachment.contains("screenshot.png"));

        // Attach the image

        final WebResource postResource = client.resource(getEnvironmentData().getBaseUrl().toExternalForm() + "/rest/bonfire/1.0/issue-attach?issueKey=TST-1");

        postResource.addFilter(new HTTPBasicAuthFilter("admin", "admin"));

        postResource.type(MediaType.APPLICATION_JSON_TYPE).post(loadJsonFromFile("singleImageUpload.json"));

        // Now we need to check that the attachment is present on the issue
        // Unfortunately, issue attachment JSON breaks the parsing into Issue, so we'll do a less robust check here.

        final String issueWithAttachment = getResource.get(new GenericType<String>() {
        });

        assertTrue(issueWithAttachment.contains("screenshot.png"));
    }

    public String loadJsonFromFile(String filename) throws IOException {
        BufferedReader singleImageUploadJsonReader = null;
        StringBuilder singleImageUploadJson = new StringBuilder();

        try {
            singleImageUploadJsonReader = new BufferedReader(new InputStreamReader(ClassLoaderUtils.getResourceAsStream(filename, TestIssueImageAttachmentResource.class)));
            String line;

            while ((line = singleImageUploadJsonReader.readLine()) != null) {
                singleImageUploadJson.append(line);
            }
        } finally {
            singleImageUploadJsonReader.close();
        }
        return singleImageUploadJson.toString();
    }
}
