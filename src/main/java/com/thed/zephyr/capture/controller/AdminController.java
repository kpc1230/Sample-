package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.IgnoreJwt;
import com.thed.zephyr.capture.model.AcHostModel;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String isMigrated(@RequestParam String jira_url){
        Optional<AtlassianHost> atlassianHost = atlassianHostRepository.findFirstByBaseUrl(jira_url);
        if(atlassianHost.isPresent()){
            AcHostModel acHostModel = (AcHostModel)atlassianHost.get();
            return acHostModel.getStatus() != AcHostModel.TenantStatus.TEMPORARY?"true":"false";
        }

        return "false";
    }
}
