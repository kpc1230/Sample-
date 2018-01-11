package com.thed.zephyr.capture.repositories.dynamodb;

import com.amazonaws.services.dynamodbv2.document.Item;

public interface DynamoDBOperations {
    void saveItem(Item item,String tableName);
    void deleteAllItems(String ctId,String tableName);
}
