package com.thed.zephyr.capture.repositories.elasticsearch.impl;

import java.util.HashSet;
import java.util.List;
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
	
	public AggregatedPage<Session> searchSessions(String ctId, Optional<Long> projectId, Optional<String> assignee, Optional<List<String>> status, Optional<String> searchTerm,
			Optional<String> sortField, boolean sortAscending, int startAt, int size) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);
		
		if(projectId.isPresent()) { //Check if project is selected then add to query.
			MatchQueryBuilder projectQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.PROJECT_ID, projectId.get());
			boolQueryBuilder.must(projectQueryBuilder);
    	}
    	
    	if(assignee.isPresent() && !StringUtils.isBlank(assignee.get())) { //Check if assignee is selected then add to query.
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
    	if(searchTerm.isPresent() && !StringUtils.isBlank(searchTerm.get())) { //Check if user typed any search term against session name then add to query.
    		RegexpQueryBuilder statusQueryBuilder = QueryBuilders.regexpQuery(ApplicationConstants.SESSION_NAME_FIELD,  ".*" + searchTerm.get().toLowerCase() + ".*");
			boolQueryBuilder.must(statusQueryBuilder);
    	}
    	FieldSortBuilder sortFieldBuilder =  SortBuilders.fieldSort("id").order(sortAscending ? SortOrder.ASC : SortOrder.DESC);
    	if(sortField.isPresent() && !StringUtils.isBlank(sortField.get())) {
    		String fieldToSort = sortField.get();
    		if("sessionname".equalsIgnoreCase(fieldToSort)) fieldToSort = "name.lower_case_sort";
    		if("created".equalsIgnoreCase(fieldToSort)) fieldToSort = "timeCreated";
    		if("project".equalsIgnoreCase(fieldToSort)) fieldToSort = "projectName.lower_case_sort";
    		if("assignee".equalsIgnoreCase(fieldToSort)) fieldToSort = "assignee.lower_case_sort";
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
	
	public void deleteSessionsByCtId(String ctId) {
		DeleteQuery deleteQuery = new DeleteQuery();
		deleteQuery.setQuery(QueryBuilders.matchQuery(ApplicationConstants.TENANT_ID_FIELD, ctId));		
		elasticsearchTemplate.delete(deleteQuery, Session.class);
	}
}
