package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.thed.zephyr.capture.service.jira.IssueService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Masud on 8/13/17.
 */
@Service
public class IssueServiceImpl implements IssueService {

    @Autowired
    private Logger log;

    @Autowired
    private JiraRestClient jiraRestClient;

    @Override
    public Issue getIssueObject(Long issueId) {
    	return jiraRestClient.getIssueClient().getIssue(String.valueOf(issueId)).claim();
    }
    
    @Override
    public Issue getIssueObject(String issueKey) {
        return jiraRestClient.getIssueClient().getIssue(issueKey).claim();
    }

}
