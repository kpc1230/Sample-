package com.thed.zephyr.capture.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.atlassian.connect.spring.AtlassianHost;
import com.thed.zephyr.capture.service.db.converter.GDPRMigrationStatusTypeConverter;
import com.thed.zephyr.capture.service.db.converter.TenantStatusTypeConverter;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by aliakseimatsarski on 9/27/16.
 */
@DynamoDBTable(tableName = ApplicationConstants.TENANT_TABLE_NAME)
public class AcHostModel extends AtlassianHost implements Serializable{
    @Id
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    private String ctId;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = ApplicationConstants.GSI_CLIENT_KEY)
    private String clientKey;
    private String publicKey;
    private String oauthClientId;
    private String sharedSecret;
    @DynamoDBIndexHashKey(globalSecondaryIndexName = ApplicationConstants.GSI_BASE_URL)
    private String baseUrl;
    private String productType;
    private String description;
    private String serviceEntitlementNumber;
    private boolean addonInstalled;
    private Calendar createdDate;
    private Calendar lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
    private String createdByAccountId;
    private String lastModifiedByAccountId;
  //  @DynamoDBIndexHashKey(globalSecondaryIndexName = ApplicationConstants.GSI_STATUS)
    @DynamoDBTypeConverted(converter = TenantStatusTypeConverter.class)
    private TenantStatus status;
    
    @DynamoDBTypeConverted(converter = GDPRMigrationStatusTypeConverter.class)
    private GDPRMigrationStatus migrated;

    public AcHostModel(){
}

    public AcHostModel(AtlassianHost atlassianHost){
        this.setClientKey(atlassianHost.getClientKey());
        this.setPublicKey(atlassianHost.getPublicKey());
        this.setSharedSecret(atlassianHost.getSharedSecret());
        this.setOauthClientId(atlassianHost.getOauthClientId());
        this.setBaseUrl(atlassianHost.getBaseUrl());
        this.setProductType(atlassianHost.getProductType());
        this.setDescription(atlassianHost.getDescription());
        this.setServiceEntitlementNumber(atlassianHost.getServiceEntitlementNumber());
        this.setAddonInstalled(atlassianHost.isAddonInstalled());
        this.setCreatedBy(atlassianHost.getCreatedBy());
        this.setLastModifiedBy(atlassianHost.getLastModifiedBy());
        this.setLastModifiedDate(atlassianHost.getLastModifiedDate() != null?atlassianHost.getLastModifiedDate():Calendar.getInstance());
        this.setCreatedDate(atlassianHost.getCreatedDate() != null?atlassianHost.getCreatedDate():Calendar.getInstance());
    }

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public void setOauthClientId(String oauthClientId) {
        this.oauthClientId = oauthClientId;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServiceEntitlementNumber() {
        return serviceEntitlementNumber;
    }

    public void setServiceEntitlementNumber(String serviceEntitlementNumber) {
        this.serviceEntitlementNumber = serviceEntitlementNumber;
    }

    public boolean isAddonInstalled() {
        return addonInstalled;
    }

    public void setAddonInstalled(boolean addonInstalled) {
        this.addonInstalled = addonInstalled;
    }

    public Calendar getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Calendar createdDate) {
        this.createdDate = createdDate;
    }

    public Calendar getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Calendar lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public String getCreatedByAccountId() {
		return createdByAccountId;
	}

	public void setCreatedByAccountId(String createdByAccountId) {
		this.createdByAccountId = createdByAccountId;
	}

	public String getLastModifiedByAccountId() {
		return lastModifiedByAccountId;
	}

	public void setLastModifiedByAccountId(String lastModifiedByAccountId) {
		this.lastModifiedByAccountId = lastModifiedByAccountId;
	}
	
	public GDPRMigrationStatus getMigrated() {
		return migrated;
	}

	public void setMigrated(GDPRMigrationStatus migrated) {
		this.migrated = migrated;
	}

	public enum TenantStatus {
        ACTIVE,
        LIC_EXPIRED,
        HOST_UNREACHABLE,
        PLUGIN_DISABLED,
        DELETED,
        UNINSTALLED,
        TEMPORARY
    }
	
	public enum GDPRMigrationStatus {
		MIGRATED("migrated"),
		FAILED("failed"),
		GDPR("gdpr"),
		BLANK("");
		
		GDPRMigrationStatus(String status) {
			this.status = status;
		}
		
		private String status;
		
		public String toString() {
			return status;
		}
	}

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("ctId:" + this.ctId + ", ");
        sb.append("status:" + this.status.toString() + ", ");
        sb.append("clientKey:" + this.getClientKey() + ", ");
        sb.append("publicKey:" + this.getPublicKey() + ", ");
        sb.append("sharedSecret:" + "****, ");
        sb.append("oauthClientId:" + this.getOauthClientId() + ", ");
        sb.append("baseUrl:" + this.getBaseUrl() + ", ");
        sb.append("productType:" + this.getProductType() + ", ");
        sb.append("description:" + this.getDescription() + ", ");
        sb.append("serviceEntitlementNumber:" + this.getServiceEntitlementNumber() + ", ");
        sb.append("isAddonInstalled:" + this.isAddonInstalled() + ", ");
        sb.append("GDPRMigratedStatus:" + (this.migrated != null ? this.migrated.toString() : ""));
        sb.append("}");

        return sb.toString();
    }
}
