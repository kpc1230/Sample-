package com.thed.zephyr.capture.service.db.converter;

import java.time.Duration;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

/**
 * Converts the duration object to milliseconds while saving into dynamodb.
 * Converts the milliseconds to duration object while reading from dynamodb.
 * 
 * @author manjunath
 *
 */
public class DurationTypeConverter implements DynamoDBTypeConverter<Long, Duration> {

	@Override
	public Long convert(Duration duration) {
		return duration != null?duration.toMillis():null;
	}

	@Override
	public Duration unconvert(Long millis) {
		return millis != null?Duration.ofMillis(millis):null;
	}

}
