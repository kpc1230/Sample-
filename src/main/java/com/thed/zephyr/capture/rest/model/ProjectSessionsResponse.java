package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ProjectSessionsResponse {
    @XmlElement
    private List<SessionBean> projectSessions;

    public ProjectSessionsResponse(List<SessionBean> projectSessions) {
        this.projectSessions = projectSessions;
    }
}
