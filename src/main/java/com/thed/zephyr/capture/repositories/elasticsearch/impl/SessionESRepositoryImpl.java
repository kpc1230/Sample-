package com.thed.zephyr.capture.repositories.elasticsearch.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.Mail;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.Session.Status;
import com.thed.zephyr.capture.service.email.AmazonSEService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.DeleteQuery;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.util.*;

/**
 * @author manjunath
 *
 */
@Component
public class SessionESRepositoryImpl {

	private static final Logger log = LoggerFactory.getLogger(SessionESRepositoryImpl.class);


	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private DynamicProperty dynamicProperty;

	@Autowired
	private AmazonSEService amazonSEService;

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
	
	public Map<String, Object> searchSessions(String ctId,List<Long> projectIds, Optional<String> assignee, Optional<String> assigneeAccountId, Optional<List<String>> status, Optional<String> searchTerm,
			Optional<String> sortField, boolean sortAscending, int startAt, int size) {
		Map<String,Object> results = new HashMap<>();
		List<Session> sessionList = new ArrayList<>();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);
		
		if(projectIds != null && projectIds.size()>0) { //Check if project is selected then add to query.
			BoolQueryBuilder projectQueryBuilder =QueryBuilders.boolQuery();
			projectIds.stream().forEach(projectId -> {
				projectQueryBuilder.should((QueryBuilders.matchQuery(ApplicationConstants.PROJECT_ID, projectId)));
			});
			boolQueryBuilder.must(projectQueryBuilder);
    	}
    	
    	if(assignee.isPresent() && !StringUtils.isEmpty(assignee.get())) { //Check if assignee is selected then add to query.
    		if(!CaptureUtil.isTenantGDPRComplaint()) {
    			MatchQueryBuilder assigneeQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.ASSIGNEE_FIELD, assignee.get());
    			boolQueryBuilder.must(assigneeQueryBuilder);
    		} else {
    			MatchQueryBuilder assigneeAccountIdQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.ASSIGNEE_ACCOUNT_ID_FIELD, assignee.get());
    			boolQueryBuilder.must(assigneeAccountIdQueryBuilder);
    		}
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
		AggregatedPage<Session> sessions = elasticsearchTemplate.queryForPage(query, Session.class);
		List<Session> crossList = new ArrayList<>();
		sessions.forEach(session -> {
			if(session.getCtId().equals(ctId)){
				sessionList.add(session);
			}else{
				crossList.add(session);
			}
		});

		if(crossList != null && crossList.size()>0) {
			//error not matched ctId
			log.error("Error during getting sessions from elasticsearch , ctId missmatch");
			Mail mail = new Mail();
			String toEmail = dynamicProperty.getStringProp(ApplicationConstants.FEEDBACK_SEND_EMAIL, "atlassian.dev@getzephyr.com").get();

			String body = "<p>Looking sessions for ctId:" + ctId + " </p>";

			for(Session session: crossList) {
				JsonNode jsonNode = new ObjectMapper().convertValue(session, JsonNode.class);
				body += "<p>Found session Object: " + jsonNode.toString() + " </p>";
			}

			mail.setTo(toEmail);
			mail.setSubject("Mismatch during retrieving sessions for ctId:" + ctId);
			mail.setText(body);

			try {
				if (amazonSEService.sendMail(mail)) {
					log.info("Successfully sent email to : {}", toEmail);
				}
			} catch (MessagingException e) {
				log.error("Error during sending Mismatch Session Email. for ctId:" + ctId);
			}
		}
		results.put(ApplicationConstants.SESSION_LIST,sessionList);
		results.put(ApplicationConstants.TOTAL_COUNT,sessions.getTotalElements());
		return results;
	}
	
	public Set<String> fetchAllAssigneesForCtId(String ctId) {
    	BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);
		SearchQuery query = new NativeSearchQueryBuilder().withTypes("session").withFilter(boolQueryBuilder).withPageable(CaptureUtil.getPageRequest(0, 1000))
				.withSourceFilter(new FetchSourceFilter(new String[]{"assignee", "assigneeAccountId"}, null)).build();
    	return elasticsearchTemplate.query(query, response -> {
    		Set<String> assigneesList = new HashSet<>();
    		final SearchHits hits = response.getHits();
            for (final SearchHit hit : hits) {
            	if(CaptureUtil.isTenantGDPRComplaint()) {
            		assigneesList.add((String)hit.getSource().get("assigneeAccountId"));
            	} else {
            		assigneesList.add((String)hit.getSource().get("assignee"));
            	}
            }
            return assigneesList;
    	});
    }
	
	public AggregatedPage<Session> fetchPrivateSessionsForUser(String ctId, String user, String userAccountId,List<Long> projectIds) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);

		addProjectFilter(projectIds,boolQueryBuilder);
		
		MatchQueryBuilder statusQueryBuilder = (QueryBuilders.matchQuery(ApplicationConstants.STATUS_FIELD, Status.COMPLETED.name()));
		boolQueryBuilder.mustNot(statusQueryBuilder);
		
    	if(!CaptureUtil.isTenantGDPRComplaint() && !StringUtils.isEmpty(user)) {
    		MatchQueryBuilder assigneeQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.ASSIGNEE_FIELD, user);
			boolQueryBuilder.must(assigneeQueryBuilder);
    	}
    	
    	if(CaptureUtil.isTenantGDPRComplaint() && !StringUtils.isEmpty(userAccountId)) {
    		MatchQueryBuilder assigneeAccountIdQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.ASSIGNEE_ACCOUNT_ID_FIELD, userAccountId);
			boolQueryBuilder.must(assigneeAccountIdQueryBuilder);
    	}

    	FieldSortBuilder sortFieldBuilder =  SortBuilders.fieldSort(ApplicationConstants.SORTFIELD_ES_CREATED).order(SortOrder.DESC);
    	Pageable pageable = CaptureUtil.getPageRequest(0, 50);
		SearchQuery query = new NativeSearchQueryBuilder().withFilter(boolQueryBuilder).withSort(sortFieldBuilder).withPageable(pageable).build();
		return elasticsearchTemplate.queryForPage(query, Session.class);
	}
	
	public AggregatedPage<Session> fetchSharedSessionsForUser(String ctId, String user, String userAccountId,List<Long> projectIds) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder ctidQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.TENANT_ID_FIELD, ctId);
		boolQueryBuilder.must(ctidQueryBuilder);

		addProjectFilter(projectIds,boolQueryBuilder);
		
		MatchQueryBuilder sharedQueryBuilder = QueryBuilders.matchQuery(ApplicationConstants.SHARED_FIELD, true);
		boolQueryBuilder.must(sharedQueryBuilder);
		
		MatchQueryBuilder statusQueryBuilder = (QueryBuilders.matchQuery(ApplicationConstants.STATUS_FIELD, Status.STARTED.name()));
		boolQueryBuilder.must(statusQueryBuilder);
		
    	if(!CaptureUtil.isTenantGDPRComplaint() && !StringUtils.isEmpty(user)) {
    		MatchQueryBuilder assigneeQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.ASSIGNEE_FIELD, user);
			boolQueryBuilder.mustNot(assigneeQueryBuilder);
    	}
    	if(CaptureUtil.isTenantGDPRComplaint() && !StringUtils.isEmpty(userAccountId)) {
    		MatchQueryBuilder assigneeAccountIdQueryBuilder = QueryBuilders.matchPhraseQuery(ApplicationConstants.ASSIGNEE_ACCOUNT_ID_FIELD, userAccountId);
			boolQueryBuilder.mustNot(assigneeAccountIdQueryBuilder);
    	}
    	FieldSortBuilder sortFieldBuilder =  SortBuilders.fieldSort(ApplicationConstants.SORTFIELD_ES_CREATED).order(SortOrder.DESC);
    	Pageable pageable = CaptureUtil.getPageRequest(0, 50);
		SearchQuery query = new NativeSearchQueryBuilder().withFilter(boolQueryBuilder).withSort(sortFieldBuilder).withPageable(pageable).build();
		return elasticsearchTemplate.queryForPage(query, Session.class);
	}

	private void addProjectFilter(List<Long> projectIds,BoolQueryBuilder boolQueryBuilder){
		if(projectIds !=null && projectIds.size()>0) {
			BoolQueryBuilder projectQueryBuilder =QueryBuilders.boolQuery();
			projectIds.stream().forEach(projectId -> {
				projectQueryBuilder.should((QueryBuilders.matchQuery(ApplicationConstants.PROJECT_ID, projectId)));
			});
			boolQueryBuilder.must(projectQueryBuilder);
		}
	}
	
	public void deleteSessionsByCtId(String ctId) {
		DeleteQuery deleteQuery = new DeleteQuery();
		deleteQuery.setQuery(QueryBuilders.matchPhraseQuery(ApplicationConstants.TENANT_ID_FIELD, ctId));
		elasticsearchTemplate.delete(deleteQuery, Session.class);
	}
}
