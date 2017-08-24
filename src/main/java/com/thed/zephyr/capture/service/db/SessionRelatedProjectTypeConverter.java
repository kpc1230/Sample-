package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.jira.Project;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by aliakseimatsarski on 8/17/17.
 */
public class SessionRelatedProjectTypeConverter implements DynamoDBTypeConverter<Long, Project> {

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
