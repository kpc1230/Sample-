package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

public class FieldOptionBean {
    @XmlElement
    private String text;

    @XmlElement
    private String value;

    @XmlElement
    private List<FieldOptionBean> children; // For cascading selects

    @XmlElement
    private boolean hasChildren; // for simplicity

    public FieldOptionBean(String text, String value) {
        this.text = text;
        this.value = value;
        this.children = new ArrayList<FieldOptionBean>();// so it isn't null
        this.hasChildren = false;
    }

    public FieldOptionBean(String text, String value, List<FieldOptionBean> children) {
        this.text = text;
        this.value = value;
        this.children = children;
        this.hasChildren = !children.isEmpty(); // True if children is not empty
    }

    public FieldOptionBean() {
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
