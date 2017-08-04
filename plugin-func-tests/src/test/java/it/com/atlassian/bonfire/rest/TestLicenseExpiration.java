package it.com.atlassian.bonfire.rest;

import com.atlassian.json.JSONObject;
import com.sun.jersey.api.client.Client;
import it.com.atlassian.bonfire.util.BonfireFuncTestCase;

public class TestLicenseExpiration extends BonfireFuncTestCase {
    private Client client;

    @Override
    public void setUpTest() {
        client = Client.create();
    }

    @Override
    public void tearDownTest() {
        client.destroy();
    }

    public void testVerifyExpiredMaintenanceLicenseWithOlderBuildGetsLicenseStatus() {

        restoreData("capture-maintenance-expired-after-build-license.xml");
        runUpgradeTasks();

        final JSONObject response = getJSON(client, "http://localhost:2990/jira/rest/bonfire/latest/plugin/details");

        assertEquals("activated", response.getString("licenceStatus"));
    }

    public void testVerifyExpiredMaintenanceLicenseWithNewerBuildGetsLicenseStatus() {

        restoreData("capture-maintenance-expired-before-build-license.xml");
        runUpgradeTasks();

        final JSONObject response = getJSON(client, "http://localhost:2990/jira/rest/bonfire/latest/plugin/details");

        assertEquals("maintenanceExpired", response.getString("licenceStatus"));
    }
}