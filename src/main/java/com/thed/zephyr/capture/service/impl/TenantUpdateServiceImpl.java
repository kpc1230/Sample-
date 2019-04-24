package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.nimbusds.jwt.JWTClaimsSet;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AddonInfo;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import com.thed.zephyr.capture.service.TenantUpdateService;
import com.thed.zephyr.capture.service.data.LicenseService;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by Masud on 4/23/19.
 */

@Service
public class TenantUpdateServiceImpl implements TenantUpdateService {

    @Autowired
    private Logger log;

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private AcHostModelRepository acHostModelRepository;

    @Override
    public void runAllTenantStatusUpdate() {
        acHostModelRepository.findAll()
                .forEach(acHostModel -> {
                    JwtAuthentication jwtAuthentication = new JwtAuthentication(new AtlassianHostUser(acHostModel, Optional.ofNullable(null)), new JWTClaimsSet.Builder().build());
                    SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                    try {
                        updateTenantStatus(acHostModel);
                    }catch (Exception ex){
                        log.error("Error during update tenant status {}", ex.getMessage());
                    }
                });
    }

    @Override
    public void updateTenantStatus(AcHostModel acHostModel) {
        AcHostModel.TenantStatus tenantStatus = AcHostModel.TenantStatus.HOST_UNREACHABLE;
        try {
            Optional<AddonInfo> addonInfo = licenseService.getAddonInfo(acHostModel);
            if(addonInfo != null && addonInfo.isPresent()){
                AddonInfo addonInfo1 = addonInfo.get();
                if(addonInfo1.getState() != null){
                    switch(addonInfo1.getState()) {
                        case "ENABLED":
                            tenantStatus = AcHostModel.TenantStatus.ACTIVE;
                            break;
                        case "DISABLED":
                            tenantStatus = AcHostModel.TenantStatus.PLUGIN_DISABLED;
                            break;
                        default:
                            tenantStatus = AcHostModel.TenantStatus.LIC_EXPIRED;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error during update tenant status {} ", e.getMessage());
        }finally {
            //update current license status
            acHostModel.setStatus(tenantStatus);
            acHostModelRepository.save(acHostModel);
            log.debug("Successfully updated tenant status {} {} {}", acHostModel.getBaseUrl(), acHostModel.getClientKey(), acHostModel.getStatus());
        }

    }


}
