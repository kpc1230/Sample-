package com.thed.zephyr.capture.model;

import com.atlassian.connect.spring.AtlassianHost;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by aliakseimatsarski on 9/27/16.
 */
public class AcHostModel extends AtlassianHost {

    @JsonIgnore
    public String ztId;

    public TenantStatus status;

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
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public String getZtId() {
        return ztId;
    }
    @JsonIgnore
    public void setZtId(String ztId) {
        this.ztId = ztId;
    }

    public enum TenantStatus {
        ACTIVE,
        LIC_EXPIRED,
        HOST_UNREACHABLE,
        PLUGIN_DISABLED,
        DELETED,
        UNINSTALLED
    }

    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("ztId:" + this.ztId + ", ");
        sb.append("status:" + this.status.toString() + ", ");
        sb.append("clientKey:" + this.getClientKey() + ", ");
        sb.append("publicKey:" + this.getPublicKey() + ", ");
        sb.append("sharedSecret:" + "****, ");
        sb.append("oauthClientId:" + this.getOauthClientId() + ", ");
        sb.append("baseUrl:" + this.getBaseUrl() + ", ");
        sb.append("productType:" + this.getProductType() + ", ");
        sb.append("description:" + this.getDescription() + ", ");
        sb.append("serviceEntitlementNumber:" + this.getServiceEntitlementNumber() + ", ");
        sb.append("isAddonInstalled:" + this.isAddonInstalled());
        sb.append("}");

        return sb.toString();
    }
}
