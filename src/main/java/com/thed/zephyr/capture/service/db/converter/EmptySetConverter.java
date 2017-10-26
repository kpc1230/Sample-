package com.thed.zephyr.capture.service.db.converter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.util.Set;

public class EmptySetConverter implements DynamoDBTypeConverter<Set<Long>, Set<Long>> {
    @Override
    public Set<Long> convert(Set<Long> set) {
        if (set != null && set.size() == 0){
            return null;
        }

        return set;
    }

    @Override
    public Set<Long> unconvert(Set<Long> set) {
        return set;
    }
}
