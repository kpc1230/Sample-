package com.thed.zephyr.capture.service.db;

import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.thed.zephyr.capture.util.ApplicationConstants;

public class CreateUserActivitiesTableRequest {
	
	public CreateUserActivitiesTableRequest() {
	}
	
	public CreateTableRequest build(String userActivitiesTableName){
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(userActivitiesTableName)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(ApplicationConstants.USER_ACTIVITIES_TABLE_READ_CAPACITY_UNITS)
                        .withWriteCapacityUnits(ApplicationConstants.USER_ACTIVITIES__TABLE_WRITE_CAPACITY_UNITS))
                .withAttributeDefinitions(getAttributeDefinitions())
                .withKeySchema(getPrimaryKey());

        return createTableRequest;
    }
	
	private KeySchemaElement getPrimaryKey(){
        KeySchemaElement primaryKey = new KeySchemaElement("accountId", KeyType.HASH);
        return primaryKey;
    }
	
	private List<AttributeDefinition> getAttributeDefinitions(){
        List<AttributeDefinition> attributes = Arrays.asList(
                new AttributeDefinition("accountId", ScalarAttributeType.S)
        );
        return attributes;
    }

}
