package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by aliakseimatsarski on 8/17/17.
 */
public class ProjectTypeConverter implements DynamoDBTypeConverter<Long, Project> {

    @Autowired
    private ProjectService projectService;

    @Override
    public Long convert(Project project) {
        return project != null?project.getId():null;
    }

    @Override
    public Project unconvert(Long projectId) {

        Project project = new Project(null, null, "projectKey_1", 10000l, "Test Project", null, null, null,
                null, null,
                null, null);

    //    return projectId != null?projectService.getProjectObj(projectId):null;
        return project;
    }
}
