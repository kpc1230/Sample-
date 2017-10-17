package com.thed.zephyr.capture.repositories.elasticsearch.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RegexpQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;

/**
 * @author manjunath
 *
 */
@Component
public class SessionESRepositoryImpl {
	
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;
	
	private static Map<Character, Character> escapeCharactersMap = new HashMap<>();
	
	static {
		escapeCharactersMap.put('~', '~');
		escapeCharactersMap.put('`', '`');
		escapeCharactersMap.put('#', '#');
		escapeCharactersMap.put('%', '%');
		escapeCharactersMap.put('^', '^');
		escapeCharactersMap.put('&', '&');
		escapeCharactersMap.put('*', '*');
		escapeCharactersMap.put('(', '(');
		escapeCharactersMap.put(')', ')');
		escapeCharactersMap.put('+', '+');
		escapeCharactersMap.put('{', '{');
		escapeCharactersMap.put('}', '}');
		escapeCharactersMap.put('[', '[');
		escapeCharactersMap.put('|', '|');
		escapeCharactersMap.put('"', '"');
		escapeCharactersMap.put('<', '<');
		escapeCharactersMap.put('?', '?');
		escapeCharactersMap.put('@', '@');
		escapeCharactersMap.put('.', '.');
		escapeCharactersMap.put('\\', '\\');
	}
	
	public AggregatedPage<Session> searchSessions(String ctId, Optional<Long> projectId, Optional<String> assignee, Optional<List<String>> status, Optional<String> searchTerm,
			Optional<String> sortField, boolean sortAscending, int startAt, int size) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);
		
		if(projectId.isPresent()) { //Check if project is selected then add to query.
			MatchQueryBuilder projectQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.PROJECT_ID, projectId.get());
			boolQueryBuilder.must(projectQueryBuilder);
    	}
    	
    	if(assignee.isPresent() && !StringUtils.isEmpty(assignee.get())) { //Check if assignee is selected then add to query.
    		MatchQueryBuilder assigneeQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.ASSIGNEE_FIELD, assignee.get());
			boolQueryBuilder.must(assigneeQueryBuilder);

    	}
    	if(status.isPresent() && status.get().size() > 0) { //Check if status is selected then add to query.
    		BoolQueryBuilder statusQueryBuilder = QueryBuilders.boolQuery();
    		status.get().stream().forEach(status1 -> {
    			statusQueryBuilder.should((QueryBuilders.matchQuery(ApplicationConstants.STATUS_FIELD, status1)));
    		});
			boolQueryBuilder.must(statusQueryBuilder);
    	}
    	if(searchTerm.isPresent() && !StringUtils.isEmpty(searchTerm.get())) { //Check if user typed any search term against session name then add to query.
    		String term = searchTerm.get().toLowerCase();
    		StringBuilder escapedSearchTerm = new StringBuilder();
    		for(int i =0;i < term.length(); i++) {
    			char ch = term.charAt(i);
    			if(!Character.isLetterOrDigit(ch)
    					&& escapeCharactersMap.containsKey(ch)) {
    				escapedSearchTerm.append("\\" + ch);
    			} else {
    				escapedSearchTerm.append(ch);
    			}
    		}
    		RegexpQueryBuilder statusQueryBuilder = QueryBuilders.regexpQuery(ApplicationConstants.SORTFIELD_ES_SESSION_NAME,  ".*" + escapedSearchTerm.toString() + ".*");
			boolQueryBuilder.must(statusQueryBuilder);
    	}
    	FieldSortBuilder sortFieldBuilder =  SortBuilders.fieldSort(ApplicationConstants.SORTFIELD_ES_CREATED).order(SortOrder.DESC);
    	if(sortField.isPresent() && !StringUtils.isEmpty(sortField.get())) {
    		String fieldToSort = sortField.get();
    		switch(fieldToSort.toLowerCase()) {
	    		case ApplicationConstants.SORTFIELD_SESSION_NAME:
	    			fieldToSort = ApplicationConstants.SORTFIELD_ES_SESSION_NAME;
	    			break;
	    		case ApplicationConstants.SORTFIELD_CREATED:
	    			fieldToSort = ApplicationConstants.SORTFIELD_ES_CREATED;
	    			break;
	    		case ApplicationConstants.SORTFIELD_PROJECT:
	    			fieldToSort = ApplicationConstants.SORTFIELD_ES_PROJECT;
	    			break;
		    	case ApplicationConstants.SORTFIELD_ASSIGNEE:
					fieldToSort = ApplicationConstants.SORTFIELD_ES_ASSIGNEE;
					break;
				case ApplicationConstants.SORTFIELD_STATUS:
	    			fieldToSort = ApplicationConstants.SORTFIELD_ES_STATUS;
	    			break;
			}
    		sortFieldBuilder = SortBuilders.fieldSort(fieldToSort).order(sortAscending ? SortOrder.ASC : SortOrder.DESC);
    	}
    	Pageable pageable = CaptureUtil.getPageRequest((startAt / size), size);
		SearchQuery query = new NativeSearchQueryBuilder().withFilter(boolQueryBuilder).withSort(sortFieldBuilder).withPageable(pageable).build();
		return elasticsearchTemplate.queryForPage(query, Session.class);
	}
	
	public Set<String> fetchAllAssigneesForCtId(String ctId) {
    	BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);
		SearchQuery query = new NativeSearchQueryBuilder().withTypes("session").withFilter(boolQueryBuilder).withPageable(CaptureUtil.getPageRequest(0, 1000))
				.withSourceFilter(new FetchSourceFilter(new String[]{"assignee"}, null)).build();
    	return elasticsearchTemplate.query(query, response -> {
    		Set<String> assigneesList = new HashSet<>();
    		final SearchHits hits = response.getHits();
            for (final SearchHit hit : hits) {
            	assigneesList.add((String)hit.getSource().get("assignee"));
            }
            return assigneesList;
    	});
    }
	
	public AggregatedPage<Session> fetchPrivateSessionsForUser(String ctId, String user) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);
		
		MatchQueryBuilder statusQueryBuilder = (QueryBuilders.matchQuery(ApplicationConstants.STATUS_FIELD, Status.COMPLETED.name()));
		boolQueryBuilder.mustNot(statusQueryBuilder);
		
    	if(!StringUtils.isEmpty(user)) {
    		MatchQueryBuilder assigneeQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.ASSIGNEE_FIELD, user);
			boolQueryBuilder.must(assigneeQueryBuilder);

    	}
		
    	FieldSortBuilder sortFieldBuilder =  SortBuilders.fieldSort(ApplicationConstants.SORTFIELD_ES_CREATED).order(SortOrder.DESC);
    	Pageable pageable = CaptureUtil.getPageRequest(0, 50);
		SearchQuery query = new NativeSearchQueryBuilder().withFilter(boolQueryBuilder).withSort(sortFieldBuilder).withPageable(pageable).build();
		return elasticsearchTemplate.queryForPage(query, Session.class);
	}
	
	public AggregatedPage<Session> fetchSharedSessionsForUser(String ctId, String user) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);
		
		MatchQueryBuilder sharedQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.SHARED_FIELD, true);
		boolQueryBuilder.must(sharedQueryBuilder);
		
		MatchQueryBuilder statusQueryBuilder = (QueryBuilders.matchQuery(ApplicationConstants.STATUS_FIELD, Status.STARTED.name()));
		boolQueryBuilder.must(statusQueryBuilder);
		
    	if(!StringUtils.isEmpty(user)) {
    		MatchQueryBuilder assigneeQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.ASSIGNEE_FIELD, user);
			boolQueryBuilder.mustNot(assigneeQueryBuilder);

    	}		
    	FieldSortBuilder sortFieldBuilder =  SortBuilders.fieldSort(ApplicationConstants.SORTFIELD_ES_CREATED).order(SortOrder.DESC);
    	Pageable pageable = CaptureUtil.getPageRequest(0, 50);
		SearchQuery query = new NativeSearchQueryBuilder().withFilter(boolQueryBuilder).withSort(sortFieldBuilder).withPageable(pageable).build();
		return elasticsearchTemplate.queryForPage(query, Session.class);
	}
	
	public void deleteSessionsByCtId(String ctId) {
		DeleteQuery deleteQuery = new DeleteQuery();
		deleteQuery.setQuery(QueryBuilders.matchQuery(ApplicationConstants.TENANT_ID_FIELD, ctId));		
		elasticsearchTemplate.delete(deleteQuery, Session.class);
	}
}
