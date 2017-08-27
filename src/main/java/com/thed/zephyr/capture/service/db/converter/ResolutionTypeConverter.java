package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.Note;

/**
 * Created by aliakseimatsarski on 8/27/17.
 */
public class ResolutionTypeConverter implements DynamoDBTypeConverter<String, Note.Resolution> {
    @Override
    public String convert(Note.Resolution resolution) {
        return resolution != null?resolution.toString():null;
    }

    @Override
    public Note.Resolution unconvert(String resolutionStr) {
        return resolutionStr != null?Note.Resolution.valueOf(resolutionStr):null;
    }
}
