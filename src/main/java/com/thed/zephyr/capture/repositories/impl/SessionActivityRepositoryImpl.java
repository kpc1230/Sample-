package com.thed.zephyr.capture.repositories.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.thed.zephyr.capture.model.SessionActivity;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
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
            SessionActivity result = (SessionActivity)dynamoDBMapper.marshallIntoObject(cls, objectMap);

            return  result;
        } catch (ClassNotFoundException e) {
            log.error("Error during instantiating class:{} from table:{} dynamoDB", clazz, ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME,  e);
        }

        return null;
    }
}
