package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.addon.AddonInfoService;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.SettingsAllRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
public class SettingsController {

    @Autowired
    private Logger log;

    @Autowired
    private AddonInfoService addonInfoService;

    @RequestMapping(value = "/rest/api/{user}/settings/", method = RequestMethod.PUT,consumes = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOuUpdateGeneralConfigPageSettimgs(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                                           @PathVariable String user, @RequestBody SettingsAllRequest settingsAllRequest) {
        log.info("Create or update General the General Configuration Page settings for the user: " + user);
        try {
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.convertValue(settingsAllRequest, JsonNode.class);
            addonInfoService.createOrUpdateProperty(acHostModel, "captureGenPageSettings", node);
        } catch (Exception exception) {
            log.error("Error during Get General Configuration Page settings for the user: " + user, exception);

        }
        return true;
    }

    @RequestMapping(value = "/rest/api/{user}/settings/", method = RequestMethod.DELETE)
    public Boolean deleteGeneralConfigPageSettings(@AuthenticationPrincipal AtlassianHostUser hostUser,
                                                   @PathVariable String user) {
        log.info("Get General Configuration Page settings for the user: " + user);
        try {
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            addonInfoService.deleteProperty(acHostModel, "captureGenPageSettings");
        } catch (Exception exception) {
            log.error("Error during delete General Configuration Page settings for the user: " + user, exception);

        }
        return true;
    }


}
