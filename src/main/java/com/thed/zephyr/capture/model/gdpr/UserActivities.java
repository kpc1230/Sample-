package com.thed.zephyr.capture.model.gdpr;

import java.util.Date;
import java.util.Objects;

import org.springframework.data.annotation.Id;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.thed.zephyr.capture.util.ApplicationConstants;

/**
 * @author manjunath
 * @version 1.0
 *
 */
@DynamoDBTable(tableName = ApplicationConstants.USER_ACTIVITIES_TABLE_NAME)
public class UserActivities {
	
	@Id
	@DynamoDBHashKey
	private String accountId;
    private String tenantKey;
    private String ctId;
    private String url;
    private Date lastAccessedTime;
    
    public UserActivities() {
    }
    
    public UserActivities(String accountId, String tenantKey, String ctId, String url, Date lastAccessedTime) {
        this.accountId = accountId;
        this.tenantKey = tenantKey;
        this.ctId = ctId;
        this.url = url;
        this.lastAccessedTime = lastAccessedTime;
    }
    
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTenantKey() {
        return tenantKey;
    }

    public void setTenantKey(String tenantKey) {
        this.tenantKey = tenantKey;
    }

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(Date lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserActivities)) return false;
        UserActivities that = (UserActivities) o;
        return Objects.equals(getAccountId(), that.getAccountId()) &&
                Objects.equals(getTenantKey(), that.getTenantKey()) &&
                Objects.equals(getCtId(), that.getCtId()) &&
                Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getLastAccessedTime(), that.getLastAccessedTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountId(), getTenantKey(), getCtId(), getUrl(), getLastAccessedTime());
    }

    @Override
    public String toString() {
        return "UserActivities{" +
                ", accountId='" + accountId + '\'' +
                ", tenantKey='" + tenantKey + '\'' +
                ", ctId='" + ctId + '\'' +
                ", url='" + url + '\'' +
                ", lastAccessedTime=" + lastAccessedTime +
                '}';
    }

}
