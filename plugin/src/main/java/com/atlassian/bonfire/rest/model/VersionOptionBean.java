package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;

public class VersionOptionBean extends FieldOptionBean {
    @XmlElement
    private boolean released;

    public VersionOptionBean(String text, String value, boolean released) {
        super(text, value);
        this.released = released;
    }
}
