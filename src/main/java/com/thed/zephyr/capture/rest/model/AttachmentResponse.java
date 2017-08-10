package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AttachmentResponse {
    @XmlElement
    private String iconPath;

    public AttachmentResponse() {
    }

    public AttachmentResponse(String iconPath) {
        this.iconPath = iconPath;
    }
}
