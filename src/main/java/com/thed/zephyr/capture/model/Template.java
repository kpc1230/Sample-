package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.service.db.converter.DateTimeTypeConverter;
import com.thed.zephyr.capture.service.db.converter.JsonNodeTypeConverter;
import com.thed.zephyr.capture.util.ApplicationConstants;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Set;

/**
 * Created by aliakseimatsarski on 8/20/17.
 */
@DynamoDBTable(tableName = ApplicationConstants.TEMPLATE_TABLE_NAME)
public class Template {

    @Id
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;
    @DynamoDBIndexHashKey(globalSecondaryIndexNames = {
            ApplicationConstants.GSI_CT_ID_PROJECT_ID,
            ApplicationConstants.GSI_CT_ID_SHARED,
            ApplicationConstants.GSI_CT_ID_CREATED_BY})
    private String ctId;
    private String name;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_CT_ID_PROJECT_ID)
    private Long projectId;
    private Boolean favourite;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_CT_ID_SHARED)
    private Boolean shared;
    @DynamoDBTypeConverted(converter = JsonNodeTypeConverter.class)
    private JsonNode content;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_CT_ID_CREATED_BY)
    private String createdBy;
    @DynamoDBTypeConverted(converter = DateTimeTypeConverter.class)
    private DateTime timeCreated;
    @DynamoDBTypeConverted(converter = DateTimeTypeConverter.class)
    private DateTime timeUpdated;
    @DynamoDBTypeConverted(converter = DateTimeTypeConverter.class)
    private DateTime timeFavourited;

    public Template() {
    }

    public Template(String ctId, String name, Long projectId, Boolean favourite, Boolean shared, JsonNode content, String createdBy, Date createdOn
    		, DateTime timeCreated, DateTime timeUpdated,DateTime timeFavourited, Set<String> variables) {
        this.ctId = ctId;
        this.name = name;
        this.projectId = projectId;
        this.favourite = favourite;
        this.shared = shared;
        this.content = content;
        this.createdBy = createdBy;
        this.timeCreated = timeCreated;
        this.timeUpdated = timeUpdated;
        this.timeFavourited = timeFavourited;
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

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String clientKey) {
        this.ctId = clientKey;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public JsonNode getContent() {
        return content;
    }

    public void setContent(JsonNode content) {
        this.content = content;
    }

    public DateTime getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(DateTime timeCreated) {
		this.timeCreated = timeCreated;
	}

	public DateTime getTimeUpdated() {
		return timeUpdated;
	}

	public void setTimeUpdated(DateTime timeUpdated) {
		this.timeUpdated = timeUpdated;
	}

	public DateTime getTimeFavourited() {
		return timeFavourited;
	}

	public void setTimeFavourited(DateTime timeFavourited) {
		this.timeFavourited = timeFavourited;
	}


	public JsonNode toJSON() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }
}
