package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.service.BonfireLicenseService.Status;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.PluginLicense;
import org.apache.log4j.Logger;

public abstract class BonfireLicenseStatusUtil {
    private static final Logger log = Logger.getLogger(BonfireLicenseStatusUtil.class);

    /**
     * Maps UPM error status code to a Capture error status code
     */
    private enum CaptureErrorLicenseStatus {

        // License seats between JIRA and Capture does not match.
        // Capture should have equal or more seats in license (up to unlimited)
        USER_MISMATCH(LicenseError.USER_MISMATCH, Status.userMismatch),

        // The license type of the plugin is incompatible with the license type of the application
        // (e.g., the plugin has an academic license but the application has a commercial license).
        // Capture should report this as an invalid license
        TYPE_MISMATCH(LicenseError.TYPE_MISMATCH, Status.invalid),

        // JIRA should report a LicenseError.USER_MISMATCH instead of a LicenseError.EDITION_MISMATCH
        // in this scenario.
        // Capture will return an invalid license for this UPM error as it should never be returned for JIRA.
        EDITION_MISMATCH(LicenseError.EDITION_MISMATCH, Status.invalid),

        // BON-2246: This version was built after the maintenance period on the license expired.
        // Capture should not allow a customer to upgrade anymore until a new license is bought.
        VERSION_MISMATCH(LicenseError.VERSION_MISMATCH, Status.maintenanceExpired),

        // Special value for an error that we can not map to a specific status
        INVALID(null, Status.invalid),

        // For an expired license we need to check whether it's still active or just an evaluation
        // to show a proper error message in the UI
        EXPIRED(LicenseError.EXPIRED, null) {
            @Override
            Status getStatus(PluginLicense pluginLicense) {
                if (pluginLicense.isEvaluation()) {
                    return Status.evalExpired;
                }
                if (!pluginLicense.isActive()) {
                    return Status.unactivated;
                } else {
                    return Status.expired;
                }
            }
        };

        private final Status status;
        private final LicenseError licenseError;

        CaptureErrorLicenseStatus(LicenseError licenseError, Status status) {
            this.licenseError = licenseError;
            this.status = status;
        }

        Status getStatus(PluginLicense pluginLicense) {
            return this.status;
        }

        static CaptureErrorLicenseStatus from(LicenseError error) {
            for (CaptureErrorLicenseStatus item : CaptureErrorLicenseStatus.values()) {
                if (item.licenseError == error) {
                    return item;
                }
            }
            log.info("License error is not mapped. error = '" + error + "'");
            return INVALID;
        }
    }

    public static Status convert(PluginLicense pluginLicense) {
        if (pluginLicense == null) {
            // no license yet set
            return Status.unactivated;
        }
        if (!pluginLicense.isValid()) {
            return CaptureErrorLicenseStatus.from(pluginLicense.getError().get()).getStatus(pluginLicense);
        }
        return Status.activated;
    }
}
