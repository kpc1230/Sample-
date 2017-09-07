package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.util.Date;

/**
 * Converts DateTime object into milliseconds while saving into database.
 * Converts milliseconds into DateTime object while reading from database.
 * 
 * @author manjunath
 * @see com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
 *
 */
public class DateTypeConverter implements DynamoDBTypeConverter<Long, Date>{

	@Override
	public Long convert(Date dateTime) {
		return dateTime != null?dateTime.getTime():null;
	}

	@Override
	public Date unconvert(Long dateTimeInMillis) {
		return dateTimeInMillis != null?new Date(dateTimeInMillis):null;
	}


}
