package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.exception.UnauthorizedException;
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

    boolean validateLicense() throws UnauthorizedException;

    AddonInfo getAddonInfo() throws UnauthorizedException;

    Optional<AddonInfo> getAddonInfo(AcHostModel acHostModel) throws UnauthorizedException;

    Status getLicenseStatus() throws UnauthorizedException;
    boolean isCaptureActivated() throws UnauthorizedException;

}
