package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by aliakseimatsarski on 8/26/17.
 */
public class ProjectTypeMarshaller implements DynamoDBMarshaller<Project> {

    @Autowired
    private ProjectService projectService;

    @Override
    public String marshall(Project project) {
        return project != null?project.getId().toString():null;
    }

    @Override
    public Project unmarshall(Class<Project> clazz, String projectId) {
        return projectId != null?projectService.getProjectObj(Long.parseLong(projectId)):null;
    }
}
