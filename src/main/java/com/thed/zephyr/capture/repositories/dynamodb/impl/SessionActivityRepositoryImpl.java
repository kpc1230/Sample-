package com.thed.zephyr.capture.repositories.dynamodb.impl;


import java.math.BigDecimal;
import java.util.*;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.thed.zephyr.capture.service.db.DynamoDBTableNameResolver;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.thed.zephyr.capture.model.FeedbackRequest;
import com.thed.zephyr.capture.model.SessionActivity;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.db.DynamoDBTableNameResolver;
import com.thed.zephyr.capture.service.email.CaptureEmailService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
@Component
public class SessionActivityRepositoryImpl {

    @Autowired
    private Logger log;
    @Autowired
    private DynamoDB dynamodb;
    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private DynamoDBTableNameResolver dynamoDBTableNameResolver;
    @Autowired
    private CaptureEmailService captureEmailService;
    @Autowired
    private DynamicProperty dynamicProperty;
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;


    public SessionActivity findOne(String id){
        String tableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        Table table = dynamodb.getTable(tableName);
        Item item = table.getItem("id", id);
        if(item == null){
        	return null;
        }
        SessionActivity sessionActivity = convertItemToSessionActivity(item);
        if(isTenantCorrect(sessionActivity)){
            return sessionActivity;
        }else {
            String ctId = CaptureUtil.getCurrentCtId();
            log.warn("WARNING some one else's sessionActivity in scope findOne() sessionActivityId:{} current ctId:{}", sessionActivity.getId(), ctId);
            sendWarningEmail(sessionActivity.getId());
            return null;
        }
    }

    public List<SessionActivity> findBySessionId(String sessionId){
       return findBySessionId(sessionId, Optional.empty());
    }

    public List<SessionActivity> findBySessionId(String sessionId, Optional<String> propertyName){
        String ctId = CaptureUtil.getCurrentCtId();
        List<QueryFilter> queryFilters = new LinkedList<>();
    	QuerySpec querySpec = new QuerySpec();
    	querySpec.withHashKey(new KeyAttribute(ApplicationConstants.SESSION_ID_FIELD, sessionId));
    	if(propertyName.isPresent() && !StringUtils.isBlank(propertyName.get())) {
    		QueryFilter propertynameQueryFilter = new QueryFilter(propertyName.get());
    		propertynameQueryFilter.exists();
    		queryFilters.add(propertynameQueryFilter);
    	}
    	querySpec.withQueryFilters(queryFilters.toArray(new QueryFilter[queryFilters.size()]));
        List<SessionActivity> result = new ArrayList<>();
        String tableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        Table table = dynamodb.getTable(tableName);
        Index index = table.getIndex(ApplicationConstants.GSI_SESSIONID_TIMESTAMP);
        ItemCollection<QueryOutcome> activityItemList = index.query(querySpec);
        IteratorSupport<Item, QueryOutcome> iterator = activityItemList.iterator();
        List<String> errSessActIds = new ArrayList<>();
        while (iterator.hasNext()){
            Item item = iterator.next();
            SessionActivity sessionActivity = convertItemToSessionActivity(item);
            if (isTenantCorrect(sessionActivity)){
                result.add(sessionActivity);
            } else{
                errSessActIds.add(sessionActivity.getId());
                log.warn("WARNING some one else's sessionActivity in scope findBySessionId()  sessionActivityId:{} sessionId:{} ctId:{} \n" + getStuckTrace(), sessionActivity.getId(), sessionId, ctId);
            }
        }

        if(errSessActIds != null && errSessActIds.size()>0){
            String sessAcIds = String.join(" , ", errSessActIds);
            sendWarningEmail(sessAcIds);
        }

        return result;
    }

    Map<String, Object> findByCtId(String ctid, Map<String, AttributeValue> lastKeyEvaluated) {
        Map<String, Object> result = new HashedMap();
        List<SessionActivity> sessionActivities = new ArrayList<>();

        String tableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        Table table = dynamodb.getTable(tableName);
        Map<String, AttributeValue> expressionAttributeValues =
                new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":val", new AttributeValue().withS(ctid));
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tableName)
                .withFilterExpression("ctId = :val")
                .withLimit(ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT)
                .withExclusiveStartKey(lastKeyEvaluated)
                .withExpressionAttributeValues(expressionAttributeValues);
        ;

        ScanResult scanResult = amazonDynamoDB.scan(scanRequest);
        if (scanResult != null) {
            result.put("lastKeyEvaluated", scanResult.getLastEvaluatedKey());
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                sessionActivities.add(convertItemToSessionActivity(item));
            }
            result.put("items", sessionActivities);
        }


        return result;
    }

    @SuppressWarnings("unchecked")
    private SessionActivity convertItemToSessionActivity(Map<String, AttributeValue> stringObjectMap) {
        AttributeValue clazz = stringObjectMap.get("clazz");
        String clazzStr = clazz.getS();
        try {
            Class<?> cls = Class.forName(clazzStr);
            return (SessionActivity) dynamoDBMapper.marshallIntoObject(cls, stringObjectMap);
        } catch (ClassNotFoundException e) {
            log.error("Error during instantiating class:{} from table:{} dynamoDB", clazzStr, ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME, e);
        }

        return null;
    }

    private SessionActivity convertItemToSessionActivity(Item item) {
        Map<String, AttributeValue> objectMap = new LinkedHashMap<>();
        Map<String, Object> stringObjectMap = item.asMap();
        String clazz = (String) stringObjectMap.get("clazz");
        try {
            Class<?> cls = Class.forName(clazz);
            for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
                AttributeValue attributeValue = new AttributeValue();
                if (entry.getValue() instanceof String) {
                    attributeValue.setS(entry.getValue().toString());
                } else if (entry.getValue() instanceof BigDecimal) {
                    attributeValue.setN(entry.getValue().toString());
                } else if (entry.getValue() instanceof Set) {
                    attributeValue.setSS((Set<String>) entry.getValue());
                }
                objectMap.put(entry.getKey(), attributeValue);
            }

            return (SessionActivity) dynamoDBMapper.marshallIntoObject(cls, objectMap);
        } catch (ClassNotFoundException e) {
            log.error("Error during instantiating class:{} from table:{} dynamoDB", clazz, ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME, e);
        }

        return null;
    }

    private boolean isTenantCorrect(SessionActivity sessionActivity){
        String ctId = CaptureUtil.getCurrentCtId();
        return StringUtils.equals(ctId, sessionActivity.getCtId());
    }

    private void sendWarningEmail(String sessActIds){
        String ctId = CaptureUtil.getCurrentCtId();
        FeedbackRequest feedbackRequest = new FeedbackRequest();
        String toEmail = dynamicProperty.getStringProp(ApplicationConstants.FEEDBACK_SEND_EMAIL, "atlassian.dev@getzephyr.com").get();
        feedbackRequest.setEmail(toEmail);
        feedbackRequest.setSummary("WARNING data mix up");
        String desc = "WARNING some one else's sessionActivity in scope sessionActivityId(s):" + sessActIds + " current ctId:" + ctId;
        feedbackRequest.setDescription(desc);
        feedbackRequest.setName("Admin");
        try {
            captureEmailService.sendFeedBackEmail(feedbackRequest);
        } catch (Exception e) {
            log.error("Can't send warning email.", e);
        }
    }

    private String getStuckTrace(){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<stackTrace.length;i++){
            if(StringUtils.contains(stackTrace[i].getClassName(), "com.thed.zephyr.capture")){
                sb.append(stackTrace[i] + "\n");
            }
        }

        return sb.toString();
    }
}
