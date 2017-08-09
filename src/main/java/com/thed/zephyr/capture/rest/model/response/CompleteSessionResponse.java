package com.thed.zephyr.capture.rest.model.response;

import javax.xml.bind.annotation.XmlElement;

public class CompleteSessionResponse {
    @XmlElement
    private String name;

    @XmlElement
    private String url;

    public CompleteSessionResponse() {
    }

    public CompleteSessionResponse(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
