package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;

import java.util.Optional;

/**
 * Created by Masud on 8/17/17.
 */
public interface LicenseService {
      static enum Status {
        ACTIVE, INACTIVE
        }

    boolean validateLicense();

    AddonInfo getAddonInfo();

    Optional<AddonInfo> getAddonInfo(AcHostModel acHostModel);

    Status getLicenseStatus();
    boolean isCaptureActivated();

}
