package com.atlassian.bonfire.service;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.plugin.license.PluginLicenseManager;
import com.atlassian.upm.api.license.entity.PluginLicense;

/**
 * Service for licensing
 */
public interface BonfireLicenseService extends PluginLicenseManager {
    public static final String SERVICE = "bonfire-license-service";

    public static enum Status {
        unactivated, activated, invalid, userMismatch, expired, evalExpired, maintenanceExpired, requiresRestart
    }

    public ErrorCollection getLicenseStatusErrors();

    public void validateLicense(ErrorCollection errorCollection, String license);

    public Status getLicenseStatus();

    public PluginLicense getLicense();

    /**
     * This is called by JIRA relectively to validate and set a licence into Bonfire.  Its a bit of a nod and wink that JIRA knows about Bonfire and will
     * help get Bonfire into play if the user licks the right links.
     *
     * @param jiraErrorCollection the JIRA ErrorCollection to fill out
     * @param license             the licence string to validate and set
     */
    public void validateAndSetLicense(com.atlassian.jira.util.ErrorCollection jiraErrorCollection, String license);

    public boolean isBonfireActivated();
}
