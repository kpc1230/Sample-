package com.thed.zephyr.capture.service.db;

import org.joda.time.DateTime;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

/**
 * Converts DateTime object into milliseconds while saving into database.
 * Converts milliseconds into DateTime object while reading from database.
 * 
 * @author manjunath
 * @see com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
 *
 */
public class DateTimeTypeConverter implements DynamoDBTypeConverter<Long, DateTime>{

	@Override
	public Long convert(DateTime dateTime) {
		return dateTime.getMillis();
	}

	@Override
	public DateTime unconvert(Long dateTimeInMillis) {
		return new DateTime(dateTimeInMillis);
	}


}
