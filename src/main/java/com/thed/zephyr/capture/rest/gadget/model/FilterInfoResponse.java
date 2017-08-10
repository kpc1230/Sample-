package com.thed.zephyr.capture.rest.gadget.model;

import com.thed.zephyr.capture.rest.model.ProjectBean;
import com.thed.zephyr.capture.rest.model.StatusBean;
import com.thed.zephyr.capture.rest.model.UserOptionBean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class FilterInfoResponse {
    @XmlElement
    private List<ProjectBean> projects;

    @XmlElement
    private List<UserOptionBean> users;

    @XmlElement
    private List<StatusBean> statuses;

    public FilterInfoResponse() {

    }

    public FilterInfoResponse(List<ProjectBean> projects, List<UserOptionBean> users, List<StatusBean> statuses) {
        this.projects = projects;
        this.users = users;
        this.statuses = statuses;
    }
}
