package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AcHostModel.GDPRMigrationStatus;

/**
 * @author manjunath
 * @version 1.0
 *
 */
public class GDPRMigrationStatusTypeConverter implements DynamoDBTypeConverter<String, AcHostModel.GDPRMigrationStatus> {

	@Override
	public String convert(GDPRMigrationStatus status) {
		 return status != null ? status.toString() : GDPRMigrationStatus.BLANK.toString();
	}

	@Override
	public GDPRMigrationStatus unconvert(String statusStr) {
		return (statusStr != null && statusStr.length() > 0) ? AcHostModel.GDPRMigrationStatus.valueOf(statusStr.toUpperCase()) : GDPRMigrationStatus.BLANK;
	}

}
