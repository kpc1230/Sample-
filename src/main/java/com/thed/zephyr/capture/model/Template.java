package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/20/17.
 */
@DynamoDBTable(tableName = ApplicationConstants.TEMPLATE_TABLE_NAME)
public class Template {
    @Id
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;
    private String name;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_PROJECTID)
    private Long projectId;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = ApplicationConstants.GSI_CLIENT_KEY)
    private String clientKey;
    private Boolean favourite;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_SHARED)
    private Boolean shared;
    private JsonNode content;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_CREATED_BY)
    private String createdBy;
    private Date createdOn;

    public Template() {
    }

    public Template(String id, String name, Long projectId, String clientKey, Boolean favourite, Boolean shared, JsonNode content, String createdBy, Date createdOn) {
        this.id = id;
        this.name = name;
        this.projectId = projectId;
        this.clientKey = clientKey;
        this.favourite = favourite;
        this.shared = shared;
        this.content = content;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
