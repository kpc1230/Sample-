package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.model.AddonInfo;

/**
 * Created by Masud on 8/17/17.
 */
public interface LicenseService {
      static enum Status {
        ACTIVE, INACTIVE
        }

    boolean validateLicense();

    AddonInfo getAddonInfo();

    Status getLicenseStatus();
    boolean isCaptureActivated();

}
