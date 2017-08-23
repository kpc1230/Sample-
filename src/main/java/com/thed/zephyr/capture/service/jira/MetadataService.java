package com.thed.zephyr.capture.service.jira;

import com.atlassian.jira.rest.client.api.domain.Field;
import com.thed.zephyr.capture.model.jira.IssueType;
import com.thed.zephyr.capture.model.jira.Project;

import java.util.List;

/**
 * Created by Masud on 8/22/17.
 */
public interface MetadataService {

    List<Field> createFieldScreenRenderer(Project project, IssueType issueType);
}
