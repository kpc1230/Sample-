package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.thed.zephyr.capture.model.Note;
import com.thed.zephyr.capture.model.NoteSessionActivity;

/**
 * Created by aliakseimatsarski on 8/27/17.
 */
public class ResolutionTypeConverter implements DynamoDBTypeConverter<String, NoteSessionActivity.Resolution> {
    @Override
    public String convert(NoteSessionActivity.Resolution resolution) {
        return resolution != null?resolution.toString():null;
    }

    @Override
    public NoteSessionActivity.Resolution unconvert(String resolutionStr) {
        return resolutionStr != null?NoteSessionActivity.Resolution.valueOf(resolutionStr):null;
    }
}
