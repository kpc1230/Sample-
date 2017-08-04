package com.atlassian.bonfire.rest.model.request;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueCommentRequest {
    public enum VisibilityType {
        group, role
    }

    @JsonProperty
    private String comment;

    @JsonProperty
    private VisibilityType visibilityType;

    @JsonProperty
    private Long roleId;

    @JsonProperty
    private String group;

    public IssueCommentRequest() {
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setVisibilityType(VisibilityType visibilityType) {
        this.visibilityType = visibilityType;
    }

    public VisibilityType getVisibilityType() {
        return visibilityType;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
