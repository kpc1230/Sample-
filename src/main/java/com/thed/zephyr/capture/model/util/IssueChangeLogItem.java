package com.thed.zephyr.capture.model.util;

public class IssueChangeLogItem {

    private String field;

    private String fieldtype;

    private String fieldId;

    private String from;

    private String fromString;

    private String to;

    private String toString;

    public IssueChangeLogItem() {
    }

    public IssueChangeLogItem(String field, String fieldtype, String fieldId, String from, String fromString, String to, String toString) {
        this.field = field;
        this.fieldtype = fieldtype;
        this.fieldId = fieldId;
        this.from = from;
        this.fromString = fromString;
        this.to = to;
        this.toString = toString;
    }

    public String getField() {
        return field;
    }

    public String getFieldtype() {
        return fieldtype;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFrom() {
        return from;
    }

    public String getFromString() {
        return fromString;
    }

    public String getTo() {
        return to;
    }

    public String getToString() {
        return toString;
    }
}
