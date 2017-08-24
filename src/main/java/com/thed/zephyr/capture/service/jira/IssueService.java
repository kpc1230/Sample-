package com.thed.zephyr.capture.service.jira;


import com.atlassian.jira.rest.client.api.domain.Issue;

/**
 * Created by Masud on 8/13/17.
 */
public interface IssueService {
	
    Issue getIssueObject(Long issueId);
    
    Issue getIssueObject(String issueKey);
    
}
