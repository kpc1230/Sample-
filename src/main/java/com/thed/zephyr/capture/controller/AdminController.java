package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.IgnoreJwt;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.util.CaptureUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by aliakseimatsarski on 9/8/17.
 */
@RestController
public class AdminController {

    @Autowired
    private Logger log;
    @Autowired
    private AtlassianHostRepository atlassianHostRepository;

    @IgnoreJwt
    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public String ping(){
        return "pong";
    }

    @IgnoreJwt
    @RequestMapping(value = "/admin/is/migrated", method = RequestMethod.GET)
    public ResponseEntity<?> isMigrated(@RequestParam String jira_url){
        Optional<AtlassianHost> atlassianHost = atlassianHostRepository.findFirstByBaseUrl(jira_url);
        Map<String, Boolean> resMap = new HashMap<>();
        if(atlassianHost.isPresent()){
            AcHostModel acHostModel = (AcHostModel)atlassianHost.get();
        	resMap.put("isGDPR", acHostModel.getMigrated() != null ? acHostModel.getMigrated() == AcHostModel.GDPRMigrationStatus.GDPR : false);
        	resMap.put("migrated", acHostModel.getStatus() != AcHostModel.TenantStatus.TEMPORARY? true : false);
        }
        return ResponseEntity.ok().body(resMap);
    }
    
    @GetMapping(value = "/isGdpr")
    public ResponseEntity<?> isTenantGDPRComplaint() {
    	boolean isTenantGDRPComplaint = CaptureUtil.isTenantGDPRComplaint();
    	Map<String, Boolean> resMap = new HashMap<>();
    	resMap.put("isGDPR", isTenantGDRPComplaint);
    	return ResponseEntity.ok().body(resMap);
    }
}
