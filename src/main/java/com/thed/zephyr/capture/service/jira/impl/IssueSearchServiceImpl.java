package com.thed.zephyr.capture.service.jira.impl;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.util.IssueSearchList;
import com.thed.zephyr.capture.model.view.IssueSearchDto;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.jira.IssueSearchService;
import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * @author manjunath
 *
 */
@Service
public class IssueSearchServiceImpl  implements IssueSearchService {

    @Autowired
    private JiraRestClient jiraRestClient;
    
    @Autowired
	private ITenantAwareCache iTenantAwareCache;
    
    @Override
    public IssueSearchList getIssuesForQuery(String projectKey, String query) {
    	try {
    		SearchResult searchResult = null;
        	if(query.matches("^[a-zA-Z0-9]+\\-[0-9]+$")) {
        		ArrayList<IssueSearchDto> searchedIssues = new ArrayList<>(10);
        		int index = 0; //Placeholder for incrementing the value to append in the issue
        		int maxResult = 10; //Fetching only matching 10 records from jira.
        		while(index < maxResult) { //looping till we find 10 results matching to the issue query.
        			if(index != 0) {
        				searchResult = getJQLResult("issue=" + query + index);
        			} else {
        				searchResult = getJQLResult("issue=" + query);
        			}
        			searchResult.getIssues().spliterator().forEachRemaining(issue -> {
        				searchedIssues.add(new IssueSearchDto(issue.getId(), issue.getKey(), issue.getIssueType().getIconUri().toString(), issue.getSummary()));
            		});
        			index++;
        		}
        		return new IssueSearchList(searchedIssues, 0, 10, searchedIssues.size());
        	} else {
        		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        		String cacheKey = "issue_search_project_" + projectKey + "_user_" + host.getUserKey();
        		ArrayList<IssueSearchDto> searchedIssues = new ArrayList<>(50);;
        		try {
        			searchedIssues = iTenantAwareCache.getOrElse((AcHostModel)host.getHost(), cacheKey, new Callable<ArrayList<IssueSearchDto>>() {
        				public ArrayList<IssueSearchDto> call() throws Exception {
        					ArrayList<IssueSearchDto> issues = new ArrayList<>(50);
        					SearchResult searchResult = getJQLResult("project=" + projectKey);
        		    		searchResult.getIssues().spliterator().forEachRemaining(issue -> {
        		    			issues.add(new IssueSearchDto(issue.getId(), issue.getKey(), issue.getIssueType().getIconUri().toString(), issue.getSummary()));
        		    		});
        		    		return issues;
        				}				
        			}, ApplicationConstants.FOUR_HOUR_CACHE_EXPIRATION);
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        		return new IssueSearchList(searchedIssues, 0, 50, searchedIssues.size());
        	}
    	} catch(Exception ex) {
    		throw new CaptureRuntimeException(ex);
    	}
    }
    
    private SearchResult getJQLResult(String jql) {
    	return jiraRestClient.getSearchClient().searchJql(jql).claim();
    }
    
    @Override
    public IssueSearchList getEpicIssuesForQuery(String query) {
    	return null;
    }
}
