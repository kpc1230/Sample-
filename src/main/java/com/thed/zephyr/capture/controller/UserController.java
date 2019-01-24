package com.thed.zephyr.capture.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.jira.FieldOption;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.CaptureUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    private Logger log;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/userSearch")
    public ResponseEntity<?> getAssignableUsersByProject(@RequestParam String projectKey, @RequestParam String term) {
        log.info("Start of getAssignableUsersByProject() --> params - projectKey " + projectKey + "term " + term);
        JsonNode userNode = userService.getAssignableUserByProjectKey(projectKey, term);
        List<FieldOption> userBeans = new ArrayList<>();
        userNode.forEach(jsonNode1 -> {
            if(CaptureUtil.isTenantGDPRComplaint()) {
            	userBeans.add(new FieldOption(
                        jsonNode1.get("displayName").asText(),
                        jsonNode1.get("accountId").asText()
                ));
            } else {
            	userBeans.add(new FieldOption(
                        jsonNode1.get("displayName").asText(),
                        jsonNode1.get("key").asText()
                ));
            }

        });
        log.info("End of getAssignableUsersByProject()");
        return ResponseEntity.ok(userBeans);
    }

}
