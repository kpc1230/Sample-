package com.thed.zephyr.capture.service.jira;


import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.jira.CaptureProject;

import java.util.ArrayList;

/**
 * Created by Masud on 8/13/17.
 */
public interface ProjectService {
    Project getProjectObj(Long projectId);
    Project getProjectObjByKey(String projectKey);
    ArrayList<BasicProject> getProjects();
    CaptureProject getCaptureProject(Long projectId);

}
