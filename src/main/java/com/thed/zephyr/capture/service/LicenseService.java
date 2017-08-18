package com.thed.zephyr.capture.service;

/**
 * Created by Masud on 8/17/17.
 */
public interface LicenseService {
      static enum Status {
        ACTIVE, INACTIVE
        }
    boolean validateLicense();
    Status getLicenseStatus();
    boolean isCaptureActivated();

}
