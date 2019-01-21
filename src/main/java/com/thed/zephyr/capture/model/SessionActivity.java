package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.thed.zephyr.capture.service.db.converter.DateTypeConverter;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;

import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
@DynamoDBTable(tableName = ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME)
abstract public class SessionActivity {

    @Id
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = ApplicationConstants.GSI_SESSIONID_TIMESTAMP)
    private String sessionId;

    private String ctId;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = ApplicationConstants.GSI_SESSIONID_TIMESTAMP)
    @DynamoDBTypeConverted(converter = DateTypeConverter.class)
    private Date timestamp;

    private String user;
    
    private String userAccountId;

    private String clazz;

    private Long projectId;

    @DynamoDBIgnore
    private String displayName;

    public SessionActivity() {
        this.clazz = this.getClass().getCanonicalName();
    }

    public SessionActivity(String sessionId, String ctId, Date timestamp, String user, Long projectId) {
        this.sessionId = sessionId;
        this.ctId = ctId;
        this.timestamp = timestamp;
        this.user = user;
        this.projectId = projectId;
        this.clazz = this.getClass().getCanonicalName();
    }
    
    public SessionActivity(String sessionId, String ctId, Date timestamp, String user, String userAccountId, Long projectId) {
        this(sessionId, ctId, timestamp, user, projectId);
        this.userAccountId = userAccountId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date time) {
        this.timestamp = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getClazz() {
        return this.getClass().getCanonicalName();
    }

    public void setClazz(String clazz) {
        this.clazz = this.getClass().getCanonicalName();
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserAccountId() {
		return userAccountId;
	}

	public void setUserAccountId(String userAccountId) {
		this.userAccountId = userAccountId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SessionActivity that = (SessionActivity) o;

        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) {
            return false;
        }
        if (!CaptureUtil.isTenantGDPRComplaint() && user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }
        
        if (CaptureUtil.isTenantGDPRComplaint() && userAccountId != null ? !userAccountId.equals(that.userAccountId) : that.userAccountId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
        result = 31 * result + (ctId != null ? ctId.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (userAccountId != null ? userAccountId.hashCode() : 0);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        return result;
    }
}
