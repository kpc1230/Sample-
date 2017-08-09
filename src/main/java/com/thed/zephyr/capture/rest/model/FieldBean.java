package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;

public class FieldBean {
    @XmlElement
    private String id;

    @XmlElement
    private boolean required;

    @XmlElement
    private int screenIndex;

    public FieldBean(String id, boolean required, int screenIndex) {
        this.id = id;
        this.required = required;
        this.screenIndex = screenIndex;
    }

    public FieldBean() {
    }

    public boolean isRequired() {
        return required;
    }
}
