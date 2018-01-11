package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.data.BackUpService;
import com.thed.zephyr.capture.util.UniqueIdGenerator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class BackUpController {
    @Autowired
    private Logger log;
    @Autowired
    private BackUpService backUpService;

    @GetMapping(value = "/createBackup", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createBackUp(@RequestParam("fileName") String fileName, @AuthenticationPrincipal AtlassianHostUser hostUser) {
        try {
            log.info("createBackUp request invoked with file name : {} ", fileName);
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            String jobProgressId = new UniqueIdGenerator().getStringId();
            backUpService.createBackUp(acHostModel, fileName, jobProgressId, getUser());
            log.info("createBackUp request processes started with file nane : {} ", fileName);
            Map<String, String> response = new HashMap<>();
            response.put("jobProgressId", jobProgressId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Erro in createBackUp ", ex);
            throw new CaptureRuntimeException(ex);
        }
    }

    @GetMapping(value = "/restoreFromBackUp", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> restoreFromBackUp(@RequestParam("fileName") String fileName, @AuthenticationPrincipal AtlassianHostUser hostUser) {
        try {
            log.info("restoreFromBackUp request invoked with file name : {} ", fileName);
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            String jobProgressId = new UniqueIdGenerator().getStringId();
            backUpService.restoreData(acHostModel, fileName, null, null, jobProgressId, getUser());
            log.info("restoreFromBackUp request processes started with file nane : {} ", fileName);
            Map<String, String> response = new HashMap<>();
            response.put("jobProgressId", jobProgressId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Erro in restoreFromBackUp ", ex);
            throw new CaptureRuntimeException(ex);
        }
    }

    protected String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String userKey = host.getUserKey().get();
        return userKey;
    }

}
