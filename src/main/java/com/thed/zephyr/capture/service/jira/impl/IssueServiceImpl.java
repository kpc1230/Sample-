package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * Get serialized issue
     * @param issueId
     * @return
     */
    @Override
    public CaptureIssue getCaptureIssue(Long issueId) {
        Issue issue = getIssueObject(issueId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.debug("ISSUE: --> {}",issue.getSummary());
        return new CaptureIssue(issue.getSelf(),
                issue.getKey(),issue.getId(),
                CaptureUtil.getFullIconUrl(issue,host));
    }

    @Override
    public List<CaptureIssue> getCaptureIssuesByIds(List<Long> issueIds) {
        List<CaptureIssue> captureIssues = new ArrayList<>();
        if(issueIds != null && issueIds.size()>0) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
            String jql = "issue in (" + StringUtils.join(issueIds, ',') + ")";
            SearchResult searchResultPromise =
                    jiraRestClient.getSearchClient().searchJql(jql).claim();
            searchResultPromise.getIssues()
                    .forEach(issue -> {
                        captureIssues.add(new CaptureIssue(issue.getSelf(),
                                issue.getKey(), issue.getId(),
                                CaptureUtil.getFullIconUrl(issue, host)));
                    });
            }
            return captureIssues;
        }

    }
