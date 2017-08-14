package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.Project;

import java.util.List;

/**
 * Created by Masud on 8/13/17.
 */
public interface ProjectService {
    Project getProjectObj(Long projectId);
    Project getProjectObjByKey(String projectKey);
    List<Project> getProjects();

}
