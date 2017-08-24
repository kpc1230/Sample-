package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient jiraRestClient;

    @Override
    public Project getProjectObj(Long projectId) {
        return getProjectObjByKey(String.valueOf(projectId));
    }

    @Override
    public Project getProjectObjByKey(String projectKey) {
         return jiraRestClient.getProjectClient().getProject(projectKey).claim();
    }

    @Override
    public ArrayList<BasicProject> getProjects() {
         return Lists.newArrayList(jiraRestClient.getProjectClient().getAllProjects().claim());
    }

}
