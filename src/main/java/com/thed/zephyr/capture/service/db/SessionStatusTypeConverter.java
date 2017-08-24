package com.thed.zephyr.capture.service.db;

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
	public String convert(Status object) {
		return object.name();
	}

	@Override
	public Status unconvert(String object) {
		return Status.valueOf(object);
	}

}
