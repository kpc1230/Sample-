package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import com.thed.zephyr.capture.service.data.LicenseService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Created by Masud on 8/17/17.
 */
@Service
public class LicenseServiceImpl implements LicenseService {

    @Autowired
    private Logger log;

    @Autowired
    private AddonInfoService addonInfoService;

    @Override
    public boolean validateLicense() {
        boolean valid = false;
        AddonInfo addonInfo = getAddonInfo();
        if(addonInfo.getLicense().isActive()){
            log.debug("Found active license.");
            valid = true;
        }
        return valid;
    }

    private AddonInfo getAddonInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        AddonInfo addonInfo = addonInfoService.getAddonInfo(acHostModel);
        return addonInfo;
    }

    @Override
    public Status getLicenseStatus() {
        Status status = Status.INACTIVE;
        AddonInfo addonInfo = getAddonInfo();
        if(addonInfo.getLicense().isActive()){
            status = Status.ACTIVE;
        }
        return status;
    }

    @Override
    public boolean isCaptureActivated() {
        AddonInfo addonInfo = getAddonInfo();
        if(addonInfo.getLicense().isEvaluation()){
            return false;
        }else {
            return true;
        }
    }
}
