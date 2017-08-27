package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.Session.Status;

/**
 * Converts the status object to string value while saving to dynamodb.
 * Converts the string value to status object while reading from dynamodb.
 * 
 * @author manjunath
 *
 */
public class SessionStatusTypeConverter implements DynamoDBTypeConverter<String, Status> {

	@Override
	public String convert(Status status) {
		return status != null?status.name():null;
	}

	@Override
	public Status unconvert(String status) {
		return status != null?Status.valueOf(status):null;
	}

}
