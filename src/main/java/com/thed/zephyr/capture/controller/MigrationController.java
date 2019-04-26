package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.IgnoreJwt;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.gdpr.MigrateService;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.UniqueIdGenerator;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class MigrationController {

    @Autowired
    private Logger log;
    @Autowired
    private MigrateService migrateService;
    @Autowired
    private DynamicProperty dynamicProperty;

    @IgnoreJwt
    @RequestMapping(value = "/migrateApplication")
    @ResponseBody
    public ResponseEntity<?> clearCache(@AuthenticationPrincipal AtlassianHostUser hostUser) {
        try {
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            String jobProgressId = new UniqueIdGenerator().getStringId();
            migrateService.migrateData(hostUser, acHostModel, jobProgressId);
            log.debug("jobProgressId :{} for the base url : {} ", jobProgressId, acHostModel.getBaseUrl());
            Map<String, String> map = new HashedMap();
            map.put("status", "success");
            return ResponseEntity.ok(map);
        } catch (Exception ex) {
            log.error("Erro in Data migration() -> ", ex);
            throw new CaptureRuntimeException(ex);
        }
    }
}
