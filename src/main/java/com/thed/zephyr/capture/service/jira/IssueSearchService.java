package com.thed.zephyr.capture.service.jira;

import com.thed.zephyr.capture.model.util.IssueSearchList;

/**
 * @author manjunath
 *
 */
public interface IssueSearchService {	
	
	IssueSearchList getIssuesForQuery(String query);
		
	IssueSearchList getEpicIssuesForQuery(String query);
	
}
