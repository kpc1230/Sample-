package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.AcHostModel;

/**
 * Created by aliakseimatsarski on 8/13/17.
 */
public class TenantStatusTypeConverter implements DynamoDBTypeConverter<String, AcHostModel.TenantStatus> {
    @Override
    public String convert(AcHostModel.TenantStatus object) {
        return object.toString();
    }

    @Override
    public AcHostModel.TenantStatus unconvert(String object) {
        return AcHostModel.TenantStatus.valueOf(object);
    }
}
