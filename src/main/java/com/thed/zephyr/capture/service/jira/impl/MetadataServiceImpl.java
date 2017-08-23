package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.model.jira.IssueType;
import com.thed.zephyr.capture.model.jira.Project;
import com.thed.zephyr.capture.service.jira.MetadataService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Masud on 8/22/17.
 */
@Service
public class MetadataServiceImpl implements MetadataService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient restClient;

    @Override
    public List<Field> createFieldScreenRenderer(Project project, IssueType issueType) {
          return Lists.newArrayList(restClient.getMetadataClient().getFields().claim());
    }
}
