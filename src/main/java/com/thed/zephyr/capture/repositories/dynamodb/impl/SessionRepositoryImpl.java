package com.thed.zephyr.capture.repositories.dynamodb.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    public List<Session> searchSessions(String ctId, Optional<Long> projectId, Optional<String> assignee, Optional<String> status, Optional<String> searchTerm) {
    	List<QueryFilter> queryFilters = new LinkedList<>();
    	QuerySpec querySpec = new QuerySpec();
    	querySpec.withHashKey(new KeyAttribute(ApplicationConstants.TENANT_ID_FIELD, ctId));
    	if(projectId.isPresent()) {
    		RangeKeyCondition range = new RangeKeyCondition(ApplicationConstants.PROJECT_ID);
    		querySpec.withRangeKeyCondition(range.eq(projectId.get()));
    	}
    	
    	if(assignee.isPresent() && !StringUtils.isBlank(assignee.get())) {
    		QueryFilter assigneeQueryFilter = new QueryFilter(ApplicationConstants.ASSIGNEE_FIELD);
    		assigneeQueryFilter.eq(assignee.get());
    		queryFilters.add(assigneeQueryFilter);
    	}
    	if(status.isPresent() && !StringUtils.isBlank(status.get())) {
    		QueryFilter statusQueryFilter = new QueryFilter(ApplicationConstants.STATUS_FIELD);
    		statusQueryFilter.eq(status.get());
    		queryFilters.add(statusQueryFilter);
    	}
    	if(searchTerm.isPresent() && !StringUtils.isBlank(searchTerm.get())) {
    		QueryFilter searchTermQueryFilter = new QueryFilter(ApplicationConstants.SESSION_NAME_FIELD);
    		searchTermQueryFilter.contains(searchTerm.get());
    		queryFilters.add(searchTermQueryFilter);
    	}
    	querySpec.withQueryFilters(queryFilters.toArray(new QueryFilter[queryFilters.size()]));
    	Table table = dynamodb.getTable(ApplicationConstants.SESSION_TABLE_NAME);
    	Index index = table.getIndex(ApplicationConstants.GSI_CT_ID_PROJECT_ID);
    	ItemCollection<QueryOutcome> activityItemList = index.query(querySpec);
    	ArrayList<Session> sessionsList = new ArrayList<>(activityItemList.getAccumulatedItemCount());
    	activityItemList.forEach(item -> {
    		Session session = convertItemToSession(item, Session.class);
    		sessionsList.add(session);
    	});
    	return sessionsList;
    }
    
    private Session convertItemToSession(Item item, Class<?> clazz){
        Map<String, AttributeValue> objectMap = new LinkedHashMap<>();
        Map<String, Object> stringObjectMap = item.asMap();
        for (Map.Entry<String, Object> entry:stringObjectMap.entrySet()){
            AttributeValue attributeValue = new AttributeValue();
            if (entry.getValue() instanceof String){
                attributeValue.setS(entry.getValue().toString());
            } else if(entry.getValue() instanceof BigDecimal){
                attributeValue.setN(entry.getValue().toString());
            }
            objectMap.put(entry.getKey(), attributeValue);
        }
        return  (Session) dynamoDBMapper.marshallIntoObject(clazz, objectMap);
    }
}
