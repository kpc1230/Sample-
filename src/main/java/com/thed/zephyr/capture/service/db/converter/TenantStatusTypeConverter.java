package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.AcHostModel;

/**
 * Created by aliakseimatsarski on 8/13/17.
 */
public class TenantStatusTypeConverter implements DynamoDBTypeConverter<String, AcHostModel.TenantStatus> {
    @Override
    public String convert(AcHostModel.TenantStatus status) {
        return status != null?status.toString():null;
    }

    @Override
    public AcHostModel.TenantStatus unconvert(String statusStr) {
        return statusStr != null?AcHostModel.TenantStatus.valueOf(statusStr):null;
    }
}
