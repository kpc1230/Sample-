package com.atlassian.bonfire.web;

import com.atlassian.bonfire.service.BonfireLicenseService;
import com.atlassian.upm.api.license.entity.Organization;
import com.atlassian.upm.api.license.entity.PluginLicense;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * UI helper to show licence details
 */
public class BonfireLicenseInfo {

    private final boolean activated;
    private final boolean expired;
    private final String daysTilExpiry;
    private final String maintenanceExpiry;
    private final String datePurchased;

    private final String licenseType;
    private final boolean unlimitedNumberOfUsers;
    private final int maximumNumberOfUsers;
    private final String supportEntitlementNumber;
    private final String organisationName;


    BonfireLicenseInfo(BonfireLicenseService.Status bonfireLicenseStatus, PluginLicense bonfireLicense) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        activated = BonfireLicenseService.Status.activated.equals(bonfireLicenseStatus);
        expired = BonfireLicenseService.Status.expired.equals(bonfireLicenseStatus);
        if (bonfireLicense != null) {
            licenseType = bonfireLicense.getLicenseType().name();
            unlimitedNumberOfUsers = bonfireLicense.isUnlimitedEdition();
            maximumNumberOfUsers = bonfireLicense.getEdition().getOrElse(0);
            supportEntitlementNumber = bonfireLicense.getSupportEntitlementNumber().getOrElse((String) null);
            Organization organization = bonfireLicense.getOrganization();
            organisationName = organization != null ? organization.getName() : "Unknown";
            if (activated) {
                DateTime expiryDate = bonfireLicense.getExpiryDate().getOrElse((DateTime) null);
                daysTilExpiry = expiryDate != null ? Integer.toString(Days.daysBetween(new DateTime(), expiryDate).getDays() + 1) : "";

                DateTime mExpiryDate = bonfireLicense.getMaintenanceExpiryDate().getOrElse((DateTime) null);
                maintenanceExpiry = mExpiryDate != null ? formatter.print(mExpiryDate) : "";

                DateTime purchaseDate = bonfireLicense.getPurchaseDate();
                datePurchased = purchaseDate != null ? formatter.print(purchaseDate) : "";
            } else {
                daysTilExpiry = "";
                maintenanceExpiry = "";
                datePurchased = "";
            }
        } else {
            // Immutability trying to be too smart.
            licenseType = null;
            unlimitedNumberOfUsers = false;
            maximumNumberOfUsers = 0;
            supportEntitlementNumber = null;
            organisationName = null;
            daysTilExpiry = "";
            maintenanceExpiry = "";
            datePurchased = "";
        }
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isExpired() {
        return expired;
    }

    public String getDaysTilExpiry() {
        return daysTilExpiry;
    }

    public String getMaintenanceExpiry() {
        return maintenanceExpiry;
    }

    public String getDatePurchased() {
        return datePurchased;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public boolean isUnlimitedNumberOfUsers() {
        return unlimitedNumberOfUsers;
    }

    public int getMaximumNumberOfUsers() {
        return maximumNumberOfUsers;
    }

    public String getSupportEntitlementNumber() {
        return supportEntitlementNumber;
    }
}
