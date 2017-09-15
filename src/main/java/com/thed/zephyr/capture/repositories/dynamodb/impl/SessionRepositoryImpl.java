package com.thed.zephyr.capture.repositories.dynamodb.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.QueryFilter;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * Class implementation for session repository methods.
 * 
 * @author manjunath
 *
 */
@Component
public class SessionRepositoryImpl {

    @Autowired
    private DynamoDB dynamodb;
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    
    /**
     * Fetch the list of sessions based on input filters from the database.
     * 
     * @param ctId -- Tenant id for the logged in user.
     * @param projectId -- Selected project id.
     * @param assignee -- Selected assigned.
     * @param status -- Selected status.
     * @param searchTerm -- Typed input value for the session name.
     * @return -- Returns the list of sessions.
     */
    public List<Session> searchSessions(String ctId, Optional<Long> projectId, Optional<String> assignee, Optional<List<String>> status, Optional<String> searchTerm) {
    	List<QueryFilter> queryFilters = new LinkedList<>();
    	QuerySpec querySpec = new QuerySpec();
    	querySpec.withHashKey(new KeyAttribute(ApplicationConstants.TENANT_ID_FIELD, ctId));
    	if(projectId.isPresent()) { //Check if project is selected then add to query.
    		RangeKeyCondition range = new RangeKeyCondition(ApplicationConstants.PROJECT_ID);
    		querySpec.withRangeKeyCondition(range.eq(projectId.get()));
    	}
    	
    	if(assignee.isPresent() && !StringUtils.isBlank(assignee.get())) { //Check if assignee is selected then add to query.
    		QueryFilter assigneeQueryFilter = new QueryFilter(ApplicationConstants.ASSIGNEE_FIELD);
    		assigneeQueryFilter.eq(assignee.get());
    		queryFilters.add(assigneeQueryFilter);
    	}
    	if(status.isPresent() && status.get().size() > 0) { //Check if status is selected then add to query.
    		QueryFilter statusQueryFilter = new QueryFilter(ApplicationConstants.STATUS_FIELD);
    		statusQueryFilter.in(status.get().toArray());
    		queryFilters.add(statusQueryFilter);
    	}
    	if(searchTerm.isPresent() && !StringUtils.isBlank(searchTerm.get())) { //Check if user typed any search term against session name then add to query.
    		QueryFilter searchTermQueryFilter = new QueryFilter(ApplicationConstants.SESSION_NAME_FIELD);
    		searchTermQueryFilter.contains(searchTerm.get());
    		queryFilters.add(searchTermQueryFilter);
    	}
    	querySpec.withQueryFilters(queryFilters.toArray(new QueryFilter[queryFilters.size()]));
    	ItemCollection<QueryOutcome> activityItemList = fetchData(querySpec);
    	ArrayList<Session> sessionsList = new ArrayList<>(activityItemList.getAccumulatedItemCount());
    	activityItemList.forEach(item -> {
    		Session session = convertItemToSession(item, Session.class);
    		sessionsList.add(session);
    	});
    	return sessionsList;
    }
    
    /**
     * Fetch private sessions for the user.
     * <ul>Conditions:
     * <li>Session should be not shared.</li>
     * <li>Session should be assigned to the same user.</li>
     * <li>Session status should not be equal to Completed.</li>
     * </ul>
     * 
     * @param ctId -- Tenant id for the logged in user.
     * @param user -- Logged in User key
     * @return -- Returns the list of sessions.
     */
    public List<Session> fetchPrivateSessionsForUser(String ctId, String user) {
    	List<QueryFilter> queryFilters = new LinkedList<>();
    	QuerySpec querySpec = new QuerySpec();
    	querySpec.withHashKey(new KeyAttribute(ApplicationConstants.TENANT_ID_FIELD, ctId));
    	querySpec.withMaxResultSize(50);//fetch only 50 sessions for the user.
		
		//check if the session is assigned to same user
    	QueryFilter assigneeQueryFilter = new QueryFilter(ApplicationConstants.ASSIGNEE_FIELD);
    	assigneeQueryFilter.eq(user);
		queryFilters.add(assigneeQueryFilter);
		
		//check if the status is not equal to completed
    	QueryFilter statusQueryFilter = new QueryFilter(ApplicationConstants.STATUS_FIELD);
    	statusQueryFilter.ne("COMPLETED");
		queryFilters.add(statusQueryFilter);
		
		querySpec.withQueryFilters(queryFilters.toArray(new QueryFilter[queryFilters.size()]));
		ItemCollection<QueryOutcome> activityItemList = fetchData(querySpec);
    	ArrayList<Session> sessionsList = new ArrayList<>(activityItemList.getAccumulatedItemCount());
    	activityItemList.forEach(item -> {
    		Session session = convertItemToSession(item, Session.class);
    		sessionsList.add(session);
    	});
		return sessionsList;
    }
    
    /**
     * Fetch shared sessions for the user.
     * <ul>Conditions:
     * <li>Session should be shared.</li>
     * <li>Session should be not assigned to the same user.</li>
     * <li>Session status should be STARTED.</li>
     * </ul>
     * 
     * @param ctId -- Tenant id for the logged in user.
     * @param user -- Logged in User key
     * @return -- Returns the list of sessions.
     */
    public List<Session> fetchSharedSessionsForUser(String ctId, String user) {
    	List<QueryFilter> queryFilters = new LinkedList<>();
    	QuerySpec querySpec = new QuerySpec();
    	querySpec.withHashKey(new KeyAttribute(ApplicationConstants.TENANT_ID_FIELD, ctId));
    	querySpec.withMaxResultSize(50);//fetch only 50 sessions for the user.
    	
    	//check if the session is shared == false
    	QueryFilter sharedQueryFilter = new QueryFilter(ApplicationConstants.SHARED_FIELD);
    	sharedQueryFilter.eq(1);
		queryFilters.add(sharedQueryFilter);
		
		//check if the session is assigned to same user
    	QueryFilter assigneeQueryFilter = new QueryFilter(ApplicationConstants.ASSIGNEE_FIELD);
    	assigneeQueryFilter.ne(user);
		queryFilters.add(assigneeQueryFilter);
		
		//check if the status is not equal to completed
    	QueryFilter statusQueryFilter = new QueryFilter(ApplicationConstants.STATUS_FIELD);
    	statusQueryFilter.eq("STARTED");
		queryFilters.add(statusQueryFilter);
		
		querySpec.withQueryFilters(queryFilters.toArray(new QueryFilter[queryFilters.size()]));
		ItemCollection<QueryOutcome> activityItemList = fetchData(querySpec);
    	ArrayList<Session> sessionsList = new ArrayList<>(activityItemList.getAccumulatedItemCount());
    	activityItemList.forEach(item -> {
    		Session session = convertItemToSession(item, Session.class);
    		sessionsList.add(session);
    	});
		return sessionsList;
    }
    
    /**
     * Fetch all the assignees for the logged in user tenant id.
     * 
     * @param ctId -- Tenant id
     * @return -- Returns the list of assignees.
     */
    public Set<String> fetchAllAssigneesForCtId(String ctId) {
    	QuerySpec querySpec = new QuerySpec();
    	querySpec.withHashKey(new KeyAttribute(ApplicationConstants.TENANT_ID_FIELD, ctId));
    	ItemCollection<QueryOutcome> activityItemList = fetchData(querySpec);
    	Set<String> assigneesList = new HashSet<>(activityItemList.getAccumulatedItemCount());
    	activityItemList.forEach(item -> {
    		String assignee = item.getString("assignee");
    		assigneesList.add(assignee);
    	});
    	return assigneesList;
    }
    
    /**
     * Fetch Data using query specification which is passed as parameter against dynamodb.
     * 
     * @param querySpec -- Query Specification holds the query filters and key conditions.
     * @return -- Returns the fetched data against the query.
     */
    private ItemCollection<QueryOutcome> fetchData(QuerySpec querySpec) {
    	Table table = dynamodb.getTable(ApplicationConstants.SESSION_TABLE_NAME);
    	Index index = table.getIndex(ApplicationConstants.GSI_CT_ID_PROJECT_ID);
    	return index.query(querySpec);
    }
    
    /**
     * Converts the item object into class specific object.
     * 
     * @param item -- Holds the record data
     * @param clazz -- Class to be converted
     * @return -- Returns the converted class object using the item data.
     */
    private Session convertItemToSession(Item item, Class<?> clazz){
	  Map<String, AttributeValue> objectMap = new LinkedHashMap<>();
      Map<String, Object> stringObjectMap = item.asMap();
      for (Map.Entry<String, Object> entry:stringObjectMap.entrySet()){
          AttributeValue attributeValue = new AttributeValue();
          if (entry.getValue() instanceof String){
              attributeValue.setS(entry.getValue().toString());
          } else if(entry.getValue() instanceof BigDecimal){
              attributeValue.setN(entry.getValue().toString());
          } else if(entry.getValue() instanceof Set) {
        	  if(!entry.getKey().equals("participants")) {
        		  attributeValue.setNS(item.getStringSet(entry.getKey()));
        	  } else {
        		  attributeValue.setSS(item.getStringSet("participants"));
        	  }
          }
          objectMap.put(entry.getKey(), attributeValue);
      }
      return  (Session) dynamoDBMapper.marshallIntoObject(clazz, objectMap);
    }
}
