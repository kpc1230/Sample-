package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AutocompleteBean {
    @XmlElement
    private String id;
    @XmlElement
    private String label;
    @XmlElement
    private String value;
    @XmlElement
    private boolean html;// use $.html() or use $.text()
    @XmlElement
    private boolean isError;
    @XmlElement
    private String iconUrl;

    public AutocompleteBean(String id, String label, String value) {
        this.id = id;
        this.label = label;
        this.value = value;

        this.html = false;
        this.isError = false;
        this.iconUrl = "";
    }

    public AutocompleteBean(String id, String label, String value, String iconUrl) {
        this.id = id;
        this.label = label;
        this.value = value;
        this.iconUrl = iconUrl;

        this.html = true;
        this.isError = false;
    }

    public AutocompleteBean(String id, String label, String value, boolean isError) {
        this.id = id;
        this.label = label;
        this.value = value;
        this.isError = isError;

        this.html = false;
        this.iconUrl = "";
    }

    public AutocompleteBean() {
    }
}
