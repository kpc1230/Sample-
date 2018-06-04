package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.util.ErrorCollection;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.util.IssueSearchList;
import com.thed.zephyr.capture.model.view.IssueSearchDto;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.jira.IssueSearchService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author manjunath
 *
 */
@Service
public class IssueSearchServiceImpl  implements IssueSearchService {

	@Autowired
	private Logger log;
    @Autowired
    private JiraRestClient jiraRestClient;
    @Autowired
	private ITenantAwareCache iTenantAwareCache;
    
    private Pattern patttern = Pattern.compile("^[a-zA-Z0-9]+\\-[0-9]+$");
    
    private static Set<String> fields = new HashSet<>();
    
    static {
    	fields.add("summary");
    	fields.add("issuetype");
    	fields.add("created");
    	fields.add("updated");
    	fields.add("project");
    	fields.add("status");
    }
    
    private IssueSearchList getIssuesForIssueTerm(String projectKeys, String issueTerm, boolean appendEpicIssueType) {
    	try {
    		SearchResult searchResult = null;
    		if(StringUtils.isEmpty(issueTerm) || StringUtils.isEmpty(projectKeys)) {
    			return new IssueSearchList(new ArrayList<>(1), 0, 11, 0);
    		}
    		Matcher match = patttern.matcher(issueTerm);
        	if(match.matches()) {
        		ArrayList<IssueSearchDto> searchedIssues = new ArrayList<>(11);
        		String projectKey = projectKeys;
        		for(String projectkey : projectKeys.split(",")) {
        			if(issueTerm.toUpperCase().startsWith(projectkey)) { //pick matched issue and project key from the list of project keys.
        				projectKey = projectkey;
        				break;
        			}
        		}
        		//Generate 10 issue keys based on search term and post the request to jira.
        		String issueQuery = generateIssueInClause(issueTerm, 10, null);
        		if(issueQuery.length() > 0) {
        			try {
        				searchResult = getJQLResult("project IN ('" + projectKey + "')" + (appendEpicIssueType ? " AND issuetype = Epic" : "") + " AND issue IN (" + issueQuery.toString() + ")", 0, 11);
        			} catch(RestClientException ex) {
        				//Removing the issues not found in jira.
        				for(ErrorCollection errorCollection: ex.getErrorCollections()) {
        					Collection<String> errorMessages = errorCollection.getErrorMessages();
        					issueQuery = generateIssueInClause(issueTerm, 10, errorMessages);
        					if(issueQuery.length() > 0) 
        						searchResult = getJQLResult("project IN ('" + projectKey + "')" + (appendEpicIssueType ? " AND issuetype = Epic" : "") + " AND issue IN (" + issueQuery.toString() + ")", 0, 11);
        					else 
        						return new IssueSearchList(searchedIssues, 0, 11, searchedIssues.size());
        				}
        			}
            		searchResult.getIssues().spliterator().forEachRemaining(issue -> {
        				searchedIssues.add(new IssueSearchDto(issue.getId(), issue.getKey(), issue.getIssueType().getIconUri().toString(), issue.getSummary()));
            		});
        		}
        		return new IssueSearchList(searchedIssues, 0, 11, searchedIssues.size());
        	} else {
        		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        		String cacheKey = "issue " + (appendEpicIssueType ? "epic" : "") + "_search_project_" + projectKeys;
        		ArrayList<IssueSearchDto> searchedIssues = new ArrayList<>(20);
        		try {
        			searchedIssues = iTenantAwareCache.getOrElse((AcHostModel)host.getHost(), cacheKey, new Callable<ArrayList<IssueSearchDto>>() {
        				public ArrayList<IssueSearchDto> call() throws Exception {
        					ArrayList<IssueSearchDto> issues = new ArrayList<>(0);
							SearchResult searchResult = null;
							searchResult = getJQLResult("project in ('" + projectKeys + "')" + (appendEpicIssueType ? " AND issuetype = Epic" : ""), 0, 30); //fetching 20 is better in terms of performance.
        		    		searchResult.getIssues().spliterator().forEachRemaining(issue -> {
        		    			issues.add(new IssueSearchDto(issue.getId(), issue.getKey(), issue.getIssueType().getIconUri().toString(), issue.getSummary()));
        		    		});
        		    		return issues;
        				}				
        			}, 1800);
        		} catch (Exception exception) {
					log.error("Error during getting IssueSearchDto from cache of Jira", exception);
        		}
        		return new IssueSearchList(searchedIssues, 0, 20, searchedIssues.size());
        	}
    	} catch(Exception ex) {
    		throw new CaptureRuntimeException(ex);
    	}
    }
    
    private SearchResult getJQLResult(String jql, int offset, int limit) {
    	return jiraRestClient.getSearchClient().searchJql(jql, limit, offset, fields).claim();
    }
    
    private String generateIssueInClause(String issueKey, int total, Collection<String> errorMessages) {
    	StringBuilder sb = new StringBuilder();
    	if(!isIssuePresentInErrorMessages(issueKey, errorMessages))
			sb.append("'" + issueKey + "'"); 
		int i = 0;
		while(i < total) {
			if(!isIssuePresentInErrorMessages(issueKey + i, errorMessages))
				sb.append(",'" + issueKey + i + "'"); 
			i++;
		}
		return sb.toString();
    }
    
    private boolean isIssuePresentInErrorMessages(String issueKey, Collection<String> errorMessages) {
    	if(Objects.nonNull(errorMessages)) {
    		long issuePresentCount  = errorMessages.parallelStream().filter(str -> str.indexOf("'" + issueKey + "'") != -1).count();
    		if(issuePresentCount > 0) {
    			return true;
    		}
    	}    	
		return false;
    }
    
    @Override
    public IssueSearchList getEpicIssuesForQuery(String projectKeys, String issueTerm) {
    	return getIssuesForIssueTerm(projectKeys, issueTerm, true);
    }

	@Override
	public IssueSearchList getIssuesForQuery(String projectKeys, String issueTerm) {
		return getIssuesForIssueTerm(projectKeys, issueTerm, false);
	}
}
