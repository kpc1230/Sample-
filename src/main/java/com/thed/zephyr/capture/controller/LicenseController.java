package com.thed.zephyr.capture.controller;

import com.thed.zephyr.capture.model.AddonInfo;
import com.thed.zephyr.capture.service.data.LicenseService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by niravshah on 9/15/17.
 */
@RestController
@RequestMapping(value = "/license")
public class LicenseController {

    @Autowired
    private Logger log;
    @Autowired
    private LicenseService licenseService;


    @RequestMapping(method = RequestMethod.GET)
    public AddonInfo getAddonInfo() {
        try {
            AddonInfo addonInfo = licenseService.getAddonInfo();
            return addonInfo;
        } catch (Exception e) {
            log.error("getAddonInfo: Error fetching Addon info: " + e.getMessage());
        }
        return null;
    }

    @RequestMapping(value = "/licenseInfo", method = RequestMethod.GET)
    public AddonInfo.License getLicenseInfo() {
        AddonInfo.License licenseInfo = licenseService.getAddonInfo().getLicense();
        if (licenseInfo != null) {
            return licenseInfo;
        } else {
            return null;
        }
    }
}
