package com.thed.zephyr.capture.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.SettingsAllRequest;
import com.thed.zephyr.capture.service.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class SettingsController {

    @Autowired
    private Logger log;

    @Autowired
    private UserService jiraUserService;


    @RequestMapping(value = "rest/api/{username}/settings/", method = RequestMethod.PUT)
    public Boolean createOuUpdateGeneralConfigPageSettimgs(@PathVariable String username, @RequestBody SettingsAllRequest settingsAllRequest) {
        log.info("Create or update General the General Configuration Page settings for the username : " + username);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.convertValue(settingsAllRequest, JsonNode.class);
            jiraUserService.createOrUpdateUserProperty(username, "captureGenPageSettings", node);
        } catch (Exception exception) {
            log.error("Error during Get General Configuration Page settings for the username : " + username, exception);

        }
        return true;
    }

    @RequestMapping(value = "rest/api/{username}/settings/", method = RequestMethod.DELETE)
    public Boolean deleteGeneralConfigPageSettings(@PathVariable String username) {
        log.info("Get General Configuration Page settings for the username : " + username);
        try {
            jiraUserService.deleteUserProperty(username, "captureGenPageSettings");
        } catch (Exception exception) {
            log.error("Error during delete General Configuration Page settings for the username : " + username, exception);

        }
        return true;
    }


}
