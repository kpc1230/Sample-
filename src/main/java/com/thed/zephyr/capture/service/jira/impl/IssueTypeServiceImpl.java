package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.google.common.collect.Lists;
import com.thed.zephyr.capture.service.jira.IssueTypeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Masud on 8/18/17.
 */
@Service
public class IssueTypeServiceImpl implements IssueTypeService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient jiraRestClient;

    @Override
    public List<IssueType> getIssueTypes() {
        return Lists.newArrayList(jiraRestClient.getMetadataClient().getIssueTypes().claim());
    }

    @Override
    public List<IssuelinksType> getIssuelinksType() {
        return Lists.newArrayList(jiraRestClient.getMetadataClient().getIssueLinkTypes().claim());
    }

    @Override
    public IssueType getIssueType(Long id) {
         return jiraRestClient.getMetadataClient().getIssueType(URI.create(String.valueOf(id))).claim();
    }

    @Override
    public List<IssueType> getIssueTypesByProject(Long projectId) {
        List<String> expList = new ArrayList<>();
        List<Long> prList = new ArrayList<>();
        expList.add("issueTypes"); Iterable<String> expandos = expList;
        prList.add(projectId); Iterable<Long> prIds = prList;
        GetCreateIssueMetadataOptions options =
                new GetCreateIssueMetadataOptions(expandos,null,null,null,prIds);
        List<CimProject> projects = Lists.newArrayList(jiraRestClient.getIssueClient()
                .getCreateIssueMetadata(options).claim());
        List<IssueType> issueTypes = new ArrayList<>();
        projects.forEach(cimProject -> {
            issueTypes.addAll((Collection<? extends IssueType>) cimProject.getIssueTypes());
        });
         return issueTypes;
    }
}
