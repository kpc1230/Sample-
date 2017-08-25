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
    public Long convert(Project object) {
        return object.getId();
    }

    @Override
    public Project unconvert(Long ptojectId) {
        return projectService.getProjectObj(ptojectId);
    }
}
