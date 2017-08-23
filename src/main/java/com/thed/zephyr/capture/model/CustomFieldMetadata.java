package com.thed.zephyr.capture.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Created by snurulla on 8/22/2017.
 */
public class CustomFieldMetadata {

    private final String name;
    private final String description;
    private final String type;
    private final String searcherKey;

    public CustomFieldMetadata(String name, String description, String type, String searcherKey) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.searcherKey = searcherKey;

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getSearcherKey() {
        return searcherKey;
    }

    @Override
    public String toString() {
        return "CustomFieldMetadata{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", searcherKey='" + searcherKey + '\'' +
                '}';
    }
}
