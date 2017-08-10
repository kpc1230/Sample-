package com.thed.zephyr.capture.service.parser;

/**
 * Type information of a parsed field
 *
 * @since v2.9.1
 */
public enum ParsedFieldType {
    SINGLE_SELECT("com.atlassian.jira.plugin.system.customfieldtypes:select");

    private final String typeKey;

    ParsedFieldType(String typeKey) {
        this.typeKey = typeKey;
    }

    public String getTypeKey() {
        return typeKey;
    }
}