package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatusBean {
    @XmlElement
    private String value;

    @XmlElement
    private String text;

    public StatusBean(String value, String text) {
        this.value = value;
        this.text = text;
    }
}
