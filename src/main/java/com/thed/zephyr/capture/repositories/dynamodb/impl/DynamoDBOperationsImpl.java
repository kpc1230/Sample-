package com.thed.zephyr.capture.repositories.dynamodb.impl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.thed.zephyr.capture.repositories.dynamodb.DynamoDBOperations;
import com.thed.zephyr.capture.service.db.DynamoDBTableNameResolver;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DynamoDBOperationsImpl implements DynamoDBOperations {
    @Autowired
    private Logger log;
    @Autowired
    private DynamoDB dynamodb;
    @Autowired
    private DynamoDBTableNameResolver dynamoDBTableNameResolver;
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Override
    public void saveItem(Item item, String tableName) {
        Table table = dynamodb.getTable(dynamoDBTableNameResolver.getTableNameWithPrefix(tableName));
        PutItemOutcome putItemOutcome = table.putItem(item);
    }

    @Override
    public void deleteAllItems(String ctId, String tableName) {
        Map<String, AttributeValue> lastKeyEvaluated = null;
        Table table = dynamodb.getTable(dynamoDBTableNameResolver.getTableNameWithPrefix(tableName));
        Map<String, AttributeValue> expressionAttributeValues =
                new HashMap<String, AttributeValue>();
        expressionAttributeValues.put(":val", new AttributeValue().withS(ctId));
        do {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(tableName)
                    .withFilterExpression("ctId = :val")
                    .withLimit(ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT)
                    .withExclusiveStartKey(lastKeyEvaluated)
                    .withExpressionAttributeValues(expressionAttributeValues);
            ;
            ScanResult scanResult = amazonDynamoDB.scan(scanRequest);
            if (scanResult != null) {
                for (Map<String, AttributeValue> item : scanResult.getItems()) {
                    AttributeValue idAtr = item.get("id");
                    String id = idAtr.getS();
                    DeleteItemOutcome outcome = table.deleteItem("id", id);
                    log.debug("Item deleted : {}, from Table : {} ", id, table.getTableName());
                }
                lastKeyEvaluated = scanResult.getLastEvaluatedKey();
            }

        } while (lastKeyEvaluated != null);
    }


}
