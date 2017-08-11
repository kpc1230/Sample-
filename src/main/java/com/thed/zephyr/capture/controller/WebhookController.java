package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class WebhookController {

    @Autowired
    private Logger log;

    @Autowired
    private AddonInfoService addonInfoService;

    @Autowired
    private Environment env;

    @Autowired
    private DynamicProperty dynamicProperty;

    @RequestMapping(value = "/rest/event/plugin/enabled", method = RequestMethod.POST)
    public ResponseEntity pluginEnableEvent(@AuthenticationPrincipal AtlassianHostUser hostUser){
        String tenantId = hostUser.getHost().getClientKey();
        log.info("Plugin enable event for tenantId:{}", tenantId);
        try {
            //BACKEND DYNAMODB CODE
        } catch (Exception exception) {
            log.error("Error during plugin enable event.", exception);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/rest/event/plugin/disabled", method = RequestMethod.POST)
    public ResponseEntity pluginDisabledEvent(@AuthenticationPrincipal AtlassianHostUser hostUser){
        String tenantId = hostUser.getHost().getClientKey();
        log.info("Plugin disabled event for tenantId:{}", tenantId);
        try {
            //BACKEND DYNAMODB CODE
        } catch (Exception exception) {
            log.error("Error during plugin enable event.", exception);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
}
