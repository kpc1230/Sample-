package com.thed.zephyr.capture.service.jira.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtSigningRestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.util.IssueSearchList;
import com.thed.zephyr.capture.service.jira.IssueSearchService;
import com.thed.zephyr.capture.util.JiraConstants;

/**
 * @author manjunath
 *
 */
@Service
public class IssueSearchServiceImpl implements IssueSearchService {
	
	@Autowired
    private Logger log;

    @Autowired
    private JwtSigningRestTemplate restTemplate;
    
    @Override
    public IssueSearchList getIssuesForQuery(String query) {
    	return getIssuesForJQLAndQuery(query, "ORDER BY updated DESC");
    }
    
    @Override
    public IssueSearchList getEpicIssuesForQuery(String query) {
    	return getIssuesForJQLAndQuery(query, "issuetype = Epic ORDER BY updated DESC");
    }
    
    private IssueSearchList getIssuesForJQLAndQuery(String query, String currentJQL) {
        ArrayList<IssueSearchDto> issues = new ArrayList<>(20);        
        try {
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        	String uri = host.getHost().getBaseUrl();
            URI targetUrl= UriComponentsBuilder.fromUriString(uri)
                    .path(JiraConstants.REST_API_ISSUE_PICKER)
                    .queryParam("term", query)
                    .queryParam("currentJQL", currentJQL)
                    .build()
                    .encode()
                    .toUri();
            JsonNode response = restTemplate.getForObject(targetUrl, JsonNode.class);
            JsonNode issuesNode = response.path("sections").get(0).get("issues");
            Iterator<JsonNode> issuesIterator = issuesNode.iterator();
            IssueSearchDto issueSearchDto = null;
            while(issuesIterator.hasNext()) {
            	JsonNode objNode = issuesIterator.next();
            	String key = objNode.path("key").toString();
            	String keyHtml = objNode.path("keyHtml").toString();
            	String img = objNode.path("img").toString();
            	String summary = objNode.path("summary").toString();
            	String summaryText = objNode.path("summaryText").toString();
            	issueSearchDto = new IssueSearchDto(key, keyHtml, img, summary, summaryText);
            	issues.add(issueSearchDto);
            }
        } catch (RestClientException exception) {
            log.error("Error during getting issues from jira for picker.", exception);
            throw new CaptureRuntimeException(exception);
        }
        return new IssueSearchList(issues, 0, issues.size(), issues.size());
    }
    
    public class IssueSearchDto {
    	
    	private String key;
    	private String keyHtml;
    	private String img;
    	private String summary;
    	private String summaryText;
    	
    	public IssueSearchDto(String key, String keyHtml, String img, String summary, String summaryText) {
    		this.key = key;
    		this.keyHtml =  keyHtml;
    		this.img =  img;
    		this.summary = summary;
    		this.summaryText = summaryText;
    	}

		public String getKey() {
			return key;
		}

		public String getKeyHtml() {
			return keyHtml;
		}

		public String getImg() {
			return img;
		}

		public String getSummary() {
			return summary;
		}

		public String getSummaryText() {
			return summaryText;
		}
    	
    }
}
