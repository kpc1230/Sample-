package it.com.atlassian.bonfire.rest;

import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONObject;
import com.sun.jersey.api.client.Client;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;

public class TestFieldsResource extends BonfireFuncTestCase {
    private Client client;

    @Override
    public void setUpTest() {
        restoreData("capture-field-test.xml");
        runUpgradeTasks();

        client = Client.create();
    }

    @Override
    public void tearDownTest() {
        client.destroy();
    }

    public void testEmptyFields() {

        // All issue types are configured to have an empty screen
        final JSONObject response = getJSON(client, "http://localhost:2990/jira/rest/bonfire/1.0/fields/10001");

        assertEquals(0, response.getJSONArray("fieldListBeans").getJSONObject(0).getJSONArray("fields").length());
    }


    public void testDefaultFields() {
        final Client client = setupClientConfig();

        // All issue types are configured to have the default fields
        final JSONObject response = getJSON(client, "http://localhost:2990/jira/rest/bonfire/1.0/fields/10000");

        JSONArray fields = response.getJSONArray("fieldListBeans").getJSONObject(0).getJSONArray("fields");
        JSONObject fieldDetails = response.getJSONObject("fieldDetails");

        assertEquals(13, fields.length());

        JSONObject summary = fields.getJSONObject(0);
        JSONObject summaryField = fieldDetails.getJSONObject(summary.getString("id"));
        assertEquals("issue.field.summary", summaryField.getString("typeKey"));
        assertEquals(0, summaryField.getJSONArray("options").length());

        JSONObject priority = fields.getJSONObject(1);
        JSONObject priorityField = fieldDetails.getJSONObject(priority.getString("id"));
        assertEquals("issue.field.priority", priorityField.getString("typeKey"));
        assertEquals(5, priorityField.getJSONArray("options").length());

        JSONObject dueDate = fields.getJSONObject(2);
        JSONObject dueDateField = fieldDetails.getJSONObject(dueDate.getString("id"));
        assertEquals("issue.field.duedate", dueDateField.getString("typeKey"));
        assertEquals(0, dueDateField.getJSONArray("options").length());

        JSONObject components = fields.getJSONObject(3);
        JSONObject componentsField = fieldDetails.getJSONObject(components.getString("id"));
        assertEquals("issue.field.components", componentsField.getString("typeKey"));
        assertEquals(0, componentsField.getJSONArray("options").length());

        JSONObject affectsVersion = fields.getJSONObject(4);
        JSONObject affectsVersionField = fieldDetails.getJSONObject(affectsVersion.getString("id"));
        assertEquals("issue.field.affectsversions", affectsVersionField.getString("typeKey"));
        assertEquals(0, affectsVersionField.getJSONArray("options").length());

        JSONObject fixVersion = fields.getJSONObject(5);
        JSONObject fixVersionField = fieldDetails.getJSONObject(fixVersion.getString("id"));
        assertEquals("issue.field.fixversions", fixVersionField.getString("typeKey"));
        assertEquals(0, fixVersionField.getJSONArray("options").length());

        JSONObject assignee = fields.getJSONObject(6);
        JSONObject assigneeField = fieldDetails.getJSONObject(assignee.getString("id"));
        assertEquals("issue.field.assignee", assigneeField.getString("typeKey"));
        assertEquals(2, assigneeField.getJSONArray("options").length());

        JSONObject reporter = fields.getJSONObject(7);
        JSONObject reporterField = fieldDetails.getJSONObject(reporter.getString("id"));
        assertEquals("issue.field.reporter", reporterField.getString("typeKey"));
        assertEquals(0, reporterField.getJSONArray("options").length());

        JSONObject environment = fields.getJSONObject(8);
        JSONObject environmentField = fieldDetails.getJSONObject(environment.getString("id"));
        assertEquals("issue.field.environment", environmentField.getString("typeKey"));
        assertEquals(0, environmentField.getJSONArray("options").length());

        JSONObject description = fields.getJSONObject(9);
        JSONObject descriptionField = fieldDetails.getJSONObject(description.getString("id"));
        assertEquals("issue.field.description", descriptionField.getString("typeKey"));
        assertEquals(0, descriptionField.getJSONArray("options").length());

        JSONObject timeTracking = fields.getJSONObject(10);
        JSONObject timeTrackingField = fieldDetails.getJSONObject(timeTracking.getString("id"));
        assertEquals("issue.field.timetracking", timeTrackingField.getString("typeKey"));
        assertEquals(0, timeTrackingField.getJSONArray("options").length());

        JSONObject attachment = fields.getJSONObject(11);
        JSONObject attachmentField = fieldDetails.getJSONObject(attachment.getString("id"));
        assertEquals("issue.field.attachment", attachmentField.getString("typeKey"));
        assertEquals(0, attachmentField.getJSONArray("options").length());

        JSONObject labels = fields.getJSONObject(12);
        JSONObject labelsField = fieldDetails.getJSONObject(labels.getString("id"));
        assertEquals("issue.field.labels", labelsField.getString("typeKey"));
        assertEquals(0, labelsField.getJSONArray("options").length());
    }

    public void testVariousFields() {
        // Project 10010, Bug
        final JSONObject response = getJSON(client, "http://localhost:2990/jira/rest/bonfire/1.0/fields/10010");

        JSONArray fieldListBeans = response.getJSONArray("fieldListBeans");
        JSONObject fieldDetails = response.getJSONObject("fieldDetails");

        for (int i = 0; i < fieldListBeans.length(); i++) {
            JSONArray fields = fieldListBeans.getJSONObject(i).getJSONArray("fields");
            JSONObject issueType = fieldListBeans.getJSONObject(i).getJSONObject("issueType");

            if (issueType.getString("id").equals("1")) {
                assertEquals(1, fields.length());

                JSONObject field = fieldDetails.getJSONObject(fields.getJSONObject(0).getString("id"));

                assertEquals("issue.field.securitylevel", field.getString("typeKey"));
                assertEquals(2, field.getJSONArray("options").length());
            }
            if (issueType.getString("id").equals("2")) {
                assertEquals(1, fields.length());

                JSONObject field = fieldDetails.getJSONObject(fields.getJSONObject(0).getString("id"));

                assertEquals("issue.field.components", field.getString("typeKey"));
                assertEquals(3, field.getJSONArray("options").length());
            }
        }

    }

    public void testDefaultFieldsWithIssueSecurityScheme() {
        // BON-2247: Should return fields array, not a server error page when issue has default security level set
        restoreData("capture-field-issue-security-defaults-test.xml");

        final JSONObject response = getJSON(client, "http://localhost:2990/jira/rest/bonfire/1.0/fields/10010");

        JSONArray fieldListBeans = response.getJSONArray("fieldListBeans");
        JSONObject fieldDetails = response.getJSONObject("fieldDetails");

        for (int i = 0; i < fieldListBeans.length(); i++) {
            JSONArray fields = fieldListBeans.getJSONObject(i).getJSONArray("fields");
            JSONObject issueType = fieldListBeans.getJSONObject(i).getJSONObject("issueType");

            if (issueType.getString("id").equals("1")) {
                assertEquals(1, fields.length());

                JSONObject field = fieldDetails.getJSONObject(fields.getJSONObject(0).getString("id"));

                assertEquals("issue.field.securitylevel", field.getString("typeKey"));
                assertEquals(2, field.getJSONArray("options").length());
            }
            if (issueType.getString("id").equals("2")) {
                assertEquals(1, fields.length());

                JSONObject field = fieldDetails.getJSONObject(fields.getJSONObject(0).getString("id"));

                assertEquals("issue.field.components", field.getString("typeKey"));
                assertEquals(3, field.getJSONArray("options").length());
            }
        }

    }

}
