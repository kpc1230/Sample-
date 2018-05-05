package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.annotation.LicenseCheck;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Masud on 10/18/17.
 */
@Controller
public class AdgController {

    @Autowired
    private Logger log;

    @Autowired
    private ITenantAwareCache tenantAwareCache;

    @LicenseCheck
    @GetMapping(value = "/adg-config")
    public String getAdgConfig(@AuthenticationPrincipal AtlassianHostUser hostUser, Model model){
        model.addAttribute("userKey",hostUser.getUserKey().get());
        return "adgConfig";
    }

    @DeleteMapping(value = "/private/admin/adg/cache")
    public ResponseEntity<?> deleteAdgFlag(@AuthenticationPrincipal AtlassianHostUser hostUser){
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        String key = CaptureUtil.createADGFlagCacheKey(hostUser.getUserKey().get());
        tenantAwareCache.remove(acHostModel, key);
        log.info("ADG3 flag cache was deleted for user:{}", hostUser.getUserKey().get());
        return ResponseEntity.ok(true);
    }
}
