package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.model.*;
import com.thed.zephyr.capture.util.ApplicationConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aliakseimatsarski on 8/13/17.
 */
public class CreateTenantTableRequest {

    public CreateTenantTableRequest(){

    }

    public CreateTableRequest build(String tabelName){
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(tabelName)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(ApplicationConstants.TENANT_TABLE_READ_CAPACITY_UNITS)
                        .withWriteCapacityUnits(ApplicationConstants.TENANT_TABLE_WRITE_CAPACITY_UNITS))
                .withAttributeDefinitions(getAttributeDefinitions())
                .withKeySchema(getPrimaryKey())
                .withGlobalSecondaryIndexes(getGlobalSecondaryIndexes());

        return createTableRequest;
    }

    private List<AttributeDefinition> getAttributeDefinitions(){
        List<AttributeDefinition> attributes = Arrays.asList(
                new AttributeDefinition("ctId", ScalarAttributeType.S),
                new AttributeDefinition("clientKey", ScalarAttributeType.S),
                new AttributeDefinition("baseUrl", ScalarAttributeType.S)
        );

        return attributes;
    }

    private KeySchemaElement getPrimaryKey(){
        KeySchemaElement primaryKey = new KeySchemaElement("ctId", KeyType.HASH);

        return primaryKey;
    }

    private List<GlobalSecondaryIndex> getGlobalSecondaryIndexes(){
        List<GlobalSecondaryIndex> globalSecondaryIndices = new ArrayList<>();
        GlobalSecondaryIndex clientKeyIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_CLIENT_KEY)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(new KeySchemaElement("clientKey" ,KeyType.HASH));

        GlobalSecondaryIndex baseUrlIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_BASE_URL)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(new KeySchemaElement("baseUrl", KeyType.HASH));

        globalSecondaryIndices.add(clientKeyIndex);
        globalSecondaryIndices.add(baseUrlIndex);

        return globalSecondaryIndices;
    }
}
