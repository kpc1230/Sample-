package com.thed.zephyr.capture.service.jira;

import com.thed.zephyr.capture.model.util.IssueSearchList;

/**
 * @author manjunath
 *
 */
public interface IssueSearchService {	
	
	IssueSearchList getIssuesForQuery(String projectKey, String issueTerm);
		
	IssueSearchList getEpicIssuesForQuery(String projectKey, String issueTerm);

    String getSprintByProject(String projectKeys, String term);
}
