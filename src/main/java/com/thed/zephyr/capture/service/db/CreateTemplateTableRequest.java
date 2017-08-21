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
                new AttributeDefinition("clientKey", ScalarAttributeType.S),
                new AttributeDefinition("sortKey", ScalarAttributeType.S),
                new AttributeDefinition("createdBy", ScalarAttributeType.S),
                new AttributeDefinition("shared", ScalarAttributeType.N)
        );

        return attributes;
    }

    private Collection<KeySchemaElement> getPrimaryKey(){
        List<KeySchemaElement> primaryKey = Arrays.asList(
                new KeySchemaElement("clientKey", KeyType.HASH),
                new KeySchemaElement("sortKey", KeyType.RANGE),
                new KeySchemaElement("createdBy", KeyType.RANGE),
                new KeySchemaElement("shared", KeyType.RANGE)
        );

        return primaryKey;
    }

    private List<GlobalSecondaryIndex> getGlobalSecondaryIndexes(){
        List<GlobalSecondaryIndex> globalSecondaryIndices = new ArrayList<>();
        List<KeySchemaElement> indexFavouriteSchema = Arrays.asList(
                new KeySchemaElement("clientKey" ,KeyType.HASH),
                new KeySchemaElement("createdBy" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex favouriteIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_CREATED_BY)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 3)
                        .withWriteCapacityUnits((long) 3))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(indexFavouriteSchema);
        List<KeySchemaElement> indexSharedSchema = Arrays.asList(
                new KeySchemaElement("clientKey" ,KeyType.HASH),
                new KeySchemaElement("shared" ,KeyType.RANGE)
        );
        GlobalSecondaryIndex sharedKeyIndex = new GlobalSecondaryIndex()
                .withIndexName(ApplicationConstants.GSI_SHARED)
                .withProvisionedThroughput(new ProvisionedThroughput()
                        .withReadCapacityUnits((long) 3)
                        .withWriteCapacityUnits((long) 3))
                .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                .withKeySchema(indexSharedSchema);


        globalSecondaryIndices.add(favouriteIndex);
        globalSecondaryIndices.add(sharedKeyIndex);

        return globalSecondaryIndices;
    }
}
