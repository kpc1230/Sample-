package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.service.jira.MetadataService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Masud on 8/24/17.
 */
@RestController
@RequestMapping("fields")
public class FieldController {

    @Autowired
    private Logger log;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = "/{projectKey}", method = RequestMethod.GET)
    public Map<String, Object> getMetadataForProject(@PathVariable String projectKey,
                                                     @AuthenticationPrincipal AtlassianHostUser hostUser){
        CaptureProject project = projectService.getCaptureProject(projectKey);
        log.debug("Project: {}", project.getName());
        return metadataService.createFieldScreenRenderer(project);
    }
}
