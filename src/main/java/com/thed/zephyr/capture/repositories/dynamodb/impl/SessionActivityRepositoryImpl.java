package com.thed.zephyr.capture.repositories.dynamodb.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.thed.zephyr.capture.model.SessionActivity;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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


    public SessionActivity findOne(String id){
        Table table = dynamodb.getTable(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        Item item = table.getItem("id", id);
        SessionActivity sessionActivity = convertItemToSessionActivity(item);

        return sessionActivity;
    }

    public List<SessionActivity> findBySessionId(String sessionId){
        List<SessionActivity> result = new ArrayList<>();
        Table table = dynamodb.getTable(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        Index index = table.getIndex(ApplicationConstants.GSI_SESSIONID_TIMESTAMP);
        ItemCollection<QueryOutcome> activityItemList = index.query("sessionId", sessionId);
        IteratorSupport<Item, QueryOutcome> iterator = activityItemList.iterator();

        while (iterator.hasNext()){
            Item item = iterator.next();
            SessionActivity sessionActivity = convertItemToSessionActivity(item);
            result.add(sessionActivity);

        }

        return result;
    }

    private SessionActivity convertItemToSessionActivity(Item item){
        Map<String, AttributeValue> objectMap = new LinkedHashMap<>();
        Map<String, Object> stringObjectMap = item.asMap();
        String clazz = (String)stringObjectMap.get("clazz");
        try {
            Class cls = Class.forName(clazz);
            for (Map.Entry<String, Object> entry:stringObjectMap.entrySet()){
                AttributeValue attributeValue = new AttributeValue();
                if (entry.getValue() instanceof String){
                    attributeValue.setS(entry.getValue().toString());
                } else if(entry.getValue() instanceof BigDecimal){
                    attributeValue.setN(entry.getValue().toString());
                }
                objectMap.put(entry.getKey(), attributeValue);
            }

            return  (SessionActivity)dynamoDBMapper.marshallIntoObject(cls, objectMap);
        } catch (ClassNotFoundException e) {
            log.error("Error during instantiating class:{} from table:{} dynamoDB", clazz, ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME,  e);
        }

        return null;
    }
}
