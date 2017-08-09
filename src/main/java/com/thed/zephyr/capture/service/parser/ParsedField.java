package com.thed.zephyr.capture.service.parser;

import java.util.List;

/**
 * Represents parsed field that is currently supported
 *
 * @since v2.9.1
 */
public class ParsedField {
    private String id;

    private String name;

    private String description;

    private String typeKey;

    private ParsedFieldType fieldType;

    private List<ParsedFieldOption> options;

    public ParsedField(String id, String name, String typeKey, String description, ParsedFieldType fieldType, List<ParsedFieldOption> options) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.typeKey = typeKey;
        this.fieldType = fieldType;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTypeKey() {
        return typeKey;
    }

    public List<ParsedFieldOption> getOptions() {
        return options;
    }

    public ParsedFieldType getFieldType() {
        return fieldType;
    }
}
