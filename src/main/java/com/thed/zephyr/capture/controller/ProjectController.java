package com.thed.zephyr.capture.controller;

import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("project")
public class ProjectController {
    @Autowired
    private Logger log;
    @Autowired
    private ProjectService projectService;

    @GetMapping
    ResponseEntity<?> getAllProjects() throws Exception {
        log.debug("getAllProjects start");
        List<BasicProject> projects = projectService.getProjects();
        Map<String,List<BasicProject>> resultMap = new HashMap();
        resultMap.put("projects",projects);
        log.debug("getAllProjects end");
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
