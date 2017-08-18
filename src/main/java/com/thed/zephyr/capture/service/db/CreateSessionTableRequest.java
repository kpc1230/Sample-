package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.model.*;
import com.thed.zephyr.capture.util.ApplicationConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aliakseimatsarski on 8/17/17.
 */
public class CreateSessionTableRequest {

    public CreateSessionTableRequest(){

    }

    public CreateTableRequest build(){
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(ApplicationConstants.SESSION_TABLE_NAME)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(ApplicationConstants.SESSION_TABLE_READ_CAPACITY_UNITS)
                        .withWriteCapacityUnits(ApplicationConstants.SESSION_TABLE_WRITE_CAPACITY_UNITS))
                .withAttributeDefinitions(getAttributeDefinitions())
                .withKeySchema(getPrimaryKey())
                .withGlobalSecondaryIndexes(getGlobalSecondaryIndexes());

        return createTableRequest;
    }

    private List<AttributeDefinition> getAttributeDefinitions(){
        List<AttributeDefinition> attributes = Arrays.asList(
                new AttributeDefinition("id", ScalarAttributeType.S),
                new AttributeDefinition("clientKey", ScalarAttributeType.N),
                new AttributeDefinition("relatedProject", ScalarAttributeType.N)
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
                new KeySchemaElement("clientKey" ,KeyType.HASH),
                new KeySchemaElement("relatedProject" ,KeyType.RANGE)
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
