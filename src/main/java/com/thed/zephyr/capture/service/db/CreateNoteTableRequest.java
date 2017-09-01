package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.model.*;
import com.thed.zephyr.capture.util.ApplicationConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aliakseimatsarski on 8/27/17.
 */
public class CreateNoteTableRequest {

    public CreateNoteTableRequest(){

    }

    public CreateTableRequest build(){
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(ApplicationConstants.NOTE_TABLE_NAME)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(ApplicationConstants.NOTE_TABLE_READ_CAPACITY_UNITS)
                        .withWriteCapacityUnits(ApplicationConstants.NOTE_TABLE_WRITE_CAPACITY_UNITS))
                .withAttributeDefinitions(getAttributeDefinitions())
                .withKeySchema(getPrimaryKey())
                .withGlobalSecondaryIndexes(getGlobalSecondaryIndexes());

        return createTableRequest;
    }

    private List<AttributeDefinition> getAttributeDefinitions(){
        List<AttributeDefinition> attributes = Arrays.asList(
                new AttributeDefinition("id", ScalarAttributeType.S),
                new AttributeDefinition("ctId", ScalarAttributeType.S),
                new AttributeDefinition("sessionId", ScalarAttributeType.S),
                new AttributeDefinition("projectId", ScalarAttributeType.N)
        );

        return attributes;
    }

    private KeySchemaElement getPrimaryKey(){
        KeySchemaElement primaryKey = new KeySchemaElement("id", KeyType.HASH);

        return primaryKey;
    }

    private List<GlobalSecondaryIndex> getGlobalSecondaryIndexes(){
        List<GlobalSecondaryIndex> globalSecondaryIndices = new ArrayList<>();
        List<KeySchemaElement> ctIdSessionIdindexSchema = Arrays.asList(
                new KeySchemaElement("ctId" ,KeyType.HASH),
                new KeySchemaElement("sessionId" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex ctIdSessionIdIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_CT_ID_SESSION_ID)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(ctIdSessionIdindexSchema);


        List<KeySchemaElement> ctIdProjectIdindexSchema = Arrays.asList(
                new KeySchemaElement("ctId" ,KeyType.HASH),
                new KeySchemaElement("projectId" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex ctIdProjectIdIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_CT_ID_PROJECT_ID)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(ctIdProjectIdindexSchema);


        globalSecondaryIndices.add(ctIdProjectIdIndex);
        globalSecondaryIndices.add(ctIdSessionIdIndex);

        return globalSecondaryIndices;
    }
}
