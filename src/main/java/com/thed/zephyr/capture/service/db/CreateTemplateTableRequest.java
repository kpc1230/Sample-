package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.model.*;
import com.thed.zephyr.capture.util.ApplicationConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by aliakseimatsarski on 8/20/17.
 */
public class CreateTemplateTableRequest {

    public CreateTemplateTableRequest(){

    }

    public CreateTableRequest build(){
        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(ApplicationConstants.TEMPLATE_TABLE_NAME)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits(ApplicationConstants.TEMPLATE_TABLE_READ_CAPACITY_UNITS)
                        .withWriteCapacityUnits(ApplicationConstants.TEMPLATE_TABLE_WRITE_CAPACITY_UNITS))
                .withAttributeDefinitions(getAttributeDefinitions())
                .withKeySchema(getPrimaryKey())
                .withGlobalSecondaryIndexes(getGlobalSecondaryIndexes());

        return createTableRequest;
    }

    private List<AttributeDefinition> getAttributeDefinitions(){
        List<AttributeDefinition> attributes = Arrays.asList(
                new AttributeDefinition("id", ScalarAttributeType.S),
                new AttributeDefinition("ctId", ScalarAttributeType.S),
                new AttributeDefinition("createdBy", ScalarAttributeType.S),
                new AttributeDefinition("shared", ScalarAttributeType.N),
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

        List<KeySchemaElement> indexProjectIdSchema = Arrays.asList(
                new KeySchemaElement("ctId" ,KeyType.HASH),
                new KeySchemaElement("projectId" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex projectIdIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_PROJECTID)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(indexProjectIdSchema);

        List<KeySchemaElement> indexFavouriteSchema = Arrays.asList(
                new KeySchemaElement("ctId" ,KeyType.HASH),
                new KeySchemaElement("createdBy" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex favouriteIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_CREATED_BY)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(indexFavouriteSchema);

        List<KeySchemaElement> indexSharedSchema = Arrays.asList(
                new KeySchemaElement("ctId" ,KeyType.HASH),
                new KeySchemaElement("shared" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex sharedKeyIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_SHARED)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 1)
                        .withWriteCapacityUnits((long) 1))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(indexSharedSchema);

        globalSecondaryIndices.add(favouriteIndex);
        globalSecondaryIndices.add(sharedKeyIndex);
        globalSecondaryIndices.add(projectIdIndex);

        return globalSecondaryIndices;
    }
}
