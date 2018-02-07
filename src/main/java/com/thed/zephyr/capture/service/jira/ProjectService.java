package com.thed.zephyr.capture.service.jira;


import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.jira.CaptureProject;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Created by Masud on 8/13/17.
 */
public interface ProjectService {
    Project getProjectObj(Long projectId);
    Project getProjectObjByKey(String projectKey);
    ArrayList<BasicProject> getProjects(Optional<Boolean> extension) throws Exception;
    CaptureProject getCaptureProject(Long projectId);
    CaptureProject getCaptureProject(String projectKey);
    CaptureProject getCaptureProjectViaAddon(AcHostModel acHostModel, String projectIdOrKey);

}
