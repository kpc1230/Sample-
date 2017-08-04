package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class FieldListBean {

    @XmlElement
    private List<FieldBean> fields;

    @XmlElement
    private IssueTypeBean issueType;

    @XmlElement
    private int advancedFieldCount;

    @XmlElement
    private int requiredAdvancedFieldCount;

    public FieldListBean(List<FieldBean> fields, IssueTypeBean issueType, int advancedFieldCount, int requiredAdvancedFieldCount) {
        this.fields = fields;
        this.issueType = issueType;
        this.advancedFieldCount = advancedFieldCount;
        this.requiredAdvancedFieldCount = requiredAdvancedFieldCount;
    }

    public FieldListBean() {
    }

    public List<FieldBean> getFields() {
        return fields;
    }

    public IssueTypeBean getIssueType() {
        return issueType;
    }
}
