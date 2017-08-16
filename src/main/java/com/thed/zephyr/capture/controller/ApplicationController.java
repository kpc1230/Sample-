package com.thed.zephyr.capture.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.service.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by snurulla on 8/16/2017.
 */
@Controller
public class ApplicationController {
    @Autowired
    private Logger log;

    @Autowired
    private UserService jiraUserService;

    @RequestMapping(value = "/adminGenConf")
    public String getGeneralConfigurationPage(@RequestParam String user_id, Model model) {
        log.debug("Requesting the general configuration page with username : " + user_id);
        JsonNode jsonNode = jiraUserService.getUserProperty(user_id, "captureGenPageSettings");
        JsonNode resp = null;
        if (jsonNode != null) {
            resp = jsonNode.get("value");
        }
        model.addAttribute("generalConfigData", resp);
        log.debug("Ending Requesting the general configuration page with username : " + user_id + "with resp : " + resp);
        return "generalConfigPage";
    }


}
