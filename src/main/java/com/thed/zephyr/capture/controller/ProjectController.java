package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("project")
public class ProjectController {
    @Autowired
    private Logger log;
    @Autowired
    private ProjectService projectService;

    @GetMapping
    ResponseEntity<?> getAllProjects(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestParam Optional<Boolean> isExtension) throws Exception {
        List<BasicProject> projects = projectService.getProjects();
        Map<String, List<BasicProject>> resultMap = new HashMap();
        resultMap.put("projects", projects);
        log.debug("Got all projects isExtension:{} user:{} projects count:{}", isExtension, hostUser.getUserKey(), projects.size());

        return ResponseEntity.ok(resultMap);
    }
    @GetMapping("/{projectIdOrKey}")
    ResponseEntity<?> getProjectByProjectIdOrKey(@PathVariable String projectIdOrKey) {
        log.debug("getProjectByProjectIdOrKey start");
        Project projects = projectService.getProjectObjByKey(projectIdOrKey);
        log.debug("getProjectByProjectIdOrKey end");
        return ResponseEntity.ok(projects);
    }
}
