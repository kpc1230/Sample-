package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class ProjectsBean {
    @XmlElement
    public List<ProjectBean> projects;

    public ProjectsBean(List<ProjectBean> projects) {
        this.projects = projects;
    }
}
