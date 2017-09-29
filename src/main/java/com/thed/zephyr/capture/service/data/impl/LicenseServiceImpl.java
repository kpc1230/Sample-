package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.exception.UnauthorizedException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import com.thed.zephyr.capture.service.data.LicenseService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
    public boolean validateLicense() throws UnauthorizedException {
        boolean valid = false;
        AddonInfo addonInfo = getAddonInfo();
        if(addonInfo.getLicense().isActive()){
            log.debug("Found active license.");
            valid = true;
        }
        return valid;
    }

    @Override
    public AddonInfo getAddonInfo() throws UnauthorizedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AcHostModel acHostModel = (AcHostModel) host.getHost();
        AddonInfo addonInfo = addonInfoService.getAddonInfo(acHostModel);
        return addonInfo;
    }

    @Override
    public Optional<AddonInfo> getAddonInfo(AcHostModel acHostModel) throws UnauthorizedException {
        AddonInfo addonInfo = addonInfoService.getAddonInfo(acHostModel);
        return Optional.ofNullable(addonInfo);
    }

    @Override
    public Status getLicenseStatus() throws UnauthorizedException {
        Status status = Status.INACTIVE;
        AddonInfo addonInfo = getAddonInfo();
        if(addonInfo.getLicense().isActive()){
            status = Status.ACTIVE;
        }
        return status;
    }

    @Override
    public boolean isCaptureActivated() throws UnauthorizedException {
        AddonInfo addonInfo = getAddonInfo();
        if(addonInfo.getLicense().isEvaluation()){
            return false;
        }else {
            return true;
        }
    }
}
