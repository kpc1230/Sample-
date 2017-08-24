package com.thed.zephyr.capture.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.service.jira.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.springframework.core.env.Environment;
import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * Created by snurulla on 8/16/2017.
 */
@Controller
public class ApplicationController {
    @Autowired
    private Logger log;

    @Autowired
    private UserService jiraUserService;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private Environment env;

    @RequestMapping(value = "/adminGenConf")
    public String getGeneralConfigurationPage(@RequestParam String user_id, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        log.debug("Requesting the general configuration page with username : " + user_id);
        JsonNode jsonNode = jiraUserService.getUserProperty(user_id, "captureGenPageSettings");
        JsonNode resp = null;
        if (jsonNode != null) {
            resp = jsonNode.get("value");
        }
        model.addAttribute("generalConfigData", resp);
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        log.debug("Ending Requesting the general configuration page with username : " + user_id + "with resp : " + resp);
        return "generalConfigPage";
    }

    @RequestMapping(value = "/browseTestSessions")
    public String getSessionNavigatorPage(@RequestParam String projectId, @RequestParam String projectKey, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        log.debug("Requesting the Browse Test Sessions page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        log.debug("Ending Requesting the Browse Test Sessions page");
        return "sessionNavigator";
    }

    @RequestMapping(value = "/viewSession")
    public String getViewSessionPage(@RequestParam String projectId, @RequestParam String projectKey, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        log.debug("Requesting the Session Navigator page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        log.debug("Ending Requesting the Session Navigator page");
        return "viewSession";
    }

    @RequestMapping(value = "/public/rest/testing")
    public String getTestingIssueView(@RequestParam String projectId, @RequestParam String projectKey, @RequestParam String issueId, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        log.debug("Requesting the Testing Issue View page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        model.addAttribute("issueId", issueId);
        log.debug("Ending Requesting the Testing Issue View page");
        return "testingIssueView";
    }

    @RequestMapping(value = "/projectTestSessions")
    public String projectTestSessions(@RequestParam String projectId, @RequestParam String projectKey, Model model) {
        String captureUIBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTUREUI_BASE_URL, env.getProperty(ApplicationConstants.CAPTUREUI_BASE_URL)).getValue();
        log.debug("Requesting the Project Test Sessions page");
        model.addAttribute("captureUIBaseUrl", captureUIBaseUrl);
        model.addAttribute("projectKey", projectKey);
        model.addAttribute("projectId", projectId);
        log.debug("Ending Requesting the Project Test Sessions page");
        return "projectTestSessions";
    }

}
