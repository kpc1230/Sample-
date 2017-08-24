package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.model.*;
import com.thed.zephyr.capture.util.ApplicationConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Venkatareddy on 8/24/17.
 */
public class CreateVariableTableRequest {

    public CreateVariableTableRequest(){
    }

    public CreateTableRequest build(){
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(ApplicationConstants.VARIABLE_TABLE_NAME)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(ApplicationConstants.VARIABLE_TABLE_READ_CAPACITY_UNITS)
                        .withWriteCapacityUnits(ApplicationConstants.VARIABLE_TABLE_WRITE_CAPACITY_UNITS))
                .withAttributeDefinitions(getAttributeDefinitions())
                .withKeySchema(getPrimaryKey())
                .withGlobalSecondaryIndexes(getGlobalSecondaryIndexes());

        return createTableRequest;
    }

    private List<AttributeDefinition> getAttributeDefinitions(){
        List<AttributeDefinition> attributes = Arrays.asList(
                new AttributeDefinition("id", ScalarAttributeType.S),
//                new AttributeDefinition("name", ScalarAttributeType.S),
//                new AttributeDefinition("value", ScalarAttributeType.S),
//                new AttributeDefinition("ownerName", ScalarAttributeType.S),
                new AttributeDefinition("clientKey", ScalarAttributeType.S)
        );

        return attributes;
    }

    private KeySchemaElement getPrimaryKey(){
        KeySchemaElement primaryKey = new KeySchemaElement("id", KeyType.HASH);

        return primaryKey;
    }

    private List<GlobalSecondaryIndex> getGlobalSecondaryIndexes(){
        List<GlobalSecondaryIndex> globalSecondaryIndices = new ArrayList<>();
        List<KeySchemaElement> indexSchema = Arrays.asList(
                new KeySchemaElement("clientKey" ,KeyType.HASH)
        );
        GlobalSecondaryIndex clientKeyIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_CLIENT_KEY)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 10)
                        .withWriteCapacityUnits((long) 10))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(indexSchema);
        globalSecondaryIndices.add(clientKeyIndex);

        return globalSecondaryIndices;
    }
}
