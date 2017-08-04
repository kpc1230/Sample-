package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class FieldsResponse {
    @XmlElement
    private List<FieldListBean> fieldListBeans;

    @XmlElement
    private Map<String, FieldDetailsBean> fieldDetails;

    @XmlElement
    private List<UserOptionBean> userBeans;

    public FieldsResponse(List<FieldListBean> fieldListBeans, Map<String, FieldDetailsBean> fieldDetails, List<UserOptionBean> userBeans) {
        this.fieldListBeans = fieldListBeans;
        this.fieldDetails = fieldDetails;
        this.userBeans = userBeans;
    }

    public FieldsResponse() {
    }

    public List<FieldListBean> getFieldListBeans() {
        return fieldListBeans;
    }

    public List<UserOptionBean> getUserBeans() {
        return userBeans;
    }
}
