package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.model.*;
import com.thed.zephyr.capture.util.ApplicationConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aliakseimatsarski on 9/2/17.
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
                new AttributeDefinition("ctId", ScalarAttributeType.S),
                new AttributeDefinition("ownerName", ScalarAttributeType.S)
        );

        return attributes;
    }

    private KeySchemaElement getPrimaryKey(){
        KeySchemaElement primaryKey = new KeySchemaElement("id", KeyType.HASH);

        return primaryKey;
    }

    private List<GlobalSecondaryIndex> getGlobalSecondaryIndexes(){
        List<GlobalSecondaryIndex> globalSecondaryIndices = new ArrayList<>();
        List<KeySchemaElement> ctIdOwnerNameIndexSchema = Arrays.asList(
                new KeySchemaElement("ctId" ,KeyType.HASH),
                new KeySchemaElement("ownerName" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex ctIdSessionIdIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_CT_ID_OWNER_NAME)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(ctIdOwnerNameIndexSchema);
        globalSecondaryIndices.add(ctIdSessionIdIndex);

        return globalSecondaryIndices;
    }
}
