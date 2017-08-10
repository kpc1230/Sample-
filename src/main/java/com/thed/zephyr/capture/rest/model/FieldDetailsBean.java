package com.thed.zephyr.capture.rest.model;

import com.atlassian.jira.issue.fields.CustomField;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class FieldDetailsBean {
    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    @XmlElement
    private String typeKey;

    @XmlElement
    private List<FieldOptionBean> options;

    @XmlElement
    private String defaultValueString;

    @XmlElement
    private List<String> defaultOptions;

    @XmlElement
    private boolean systemField;

    public FieldDetailsBean() {
    }

    public FieldDetailsBean(String id, String name, String typeKey, String description, List<FieldOptionBean> options, String defaultValue,
                            boolean systemField) {
        this.id = id;
        this.name = name;
        this.typeKey = typeKey;
        this.description = description;
        this.options = options;
        this.defaultValueString = defaultValue;
        this.systemField = systemField;
    }

    public FieldDetailsBean(CustomField customField, List<FieldOptionBean> options, boolean systemField) {
        this.id = customField.getId();
        this.name = customField.getName();
        this.typeKey = customField.getCustomFieldType().getKey();
        this.description = customField.getDescription();
        this.options = options;
        this.systemField = systemField;
    }

    public void setDefaultValueString(String defaultValueString) {
        this.defaultValueString = defaultValueString;
    }

    public void setDefaultValue(List<String> defaultOptions) {
        this.defaultOptions = defaultOptions;
    }

    public String getDefaultValueString() {
        return defaultValueString;
    }

    public List<String> getDefaultOptions() {
        return defaultOptions;
    }
}
