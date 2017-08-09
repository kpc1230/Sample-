package com.thed.zephyr.capture.rest.model;

import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.issue.Issue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 * @since v1.1
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@XmlRootElement(name = "issue")
public class IssueBean {
    @XmlElement
    private URI self;

    @XmlElement
    private String id;

    @XmlElement
    private String key;

    @XmlElement
    private String parentKey;

    @XmlElement
    private String parentId;

    @XmlElement
    private String summary;

    @XmlElement
    private String issueIconUrl;

    public IssueBean() {
    }

    public IssueBean(final Issue sourceIssue, ExcaliburWebUtil webUtil) {
        this.self = null;
        this.key = sourceIssue.getKey();
        this.id = toString(sourceIssue.getId());
        this.summary = sourceIssue.getSummary();
        if (sourceIssue.isSubTask()) {
            this.parentId = toString(sourceIssue.getParentId());
            this.parentKey = toString(sourceIssue.getParentObject().getKey());
        }
        this.issueIconUrl = webUtil.getFullIconUrl(sourceIssue);
    }

    private String toString(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public String getKey() {
        return key;
    }

    public String getSummary() {
        return summary;
    }

    public URI getSelf() {
        return self;
    }

    public String getId() {
        return id;
    }

    public String getParentKey() {
        return parentKey;
    }

    public String getParentId() {
        return parentId;
    }
}
