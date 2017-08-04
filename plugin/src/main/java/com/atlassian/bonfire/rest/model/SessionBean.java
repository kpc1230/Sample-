package com.atlassian.bonfire.rest.model;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.util.model.SessionDisplayHelper;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This bean contains the things that both Active and Inactive sessions will need.
 *
 * @author ezhang
 */
@XmlRootElement
public class SessionBean {
    @XmlElement
    private Long id;

    @XmlElement
    private String name;

    @XmlElement
    private Session.Status status;

    @XmlElement
    private String user;

    @XmlElement
    private String userDisplayName;

    @XmlElement
    private String url;

    @XmlElement
    private boolean shared;

    @XmlElement
    private boolean isActive; // shortcut for the extension

    @XmlElement
    private String userAvatarSrc;

    @XmlElement
    private String userLargeAvatarSrc;

    @XmlElement
    private String prettyStatus;

    @XmlElement
    private String projectId;

    @XmlElement
    private String projectKey;

    @XmlElement
    private String projectName;

    @XmlElement
    private SessionDisplayHelper permissions;

    public SessionBean() {
    }

    public SessionBean(Session session, ExcaliburWebUtil excaliburWebUtil, boolean isActive, SessionDisplayHelper permissions) {
        this.id = session.getId();
        this.name = session.getName();
        this.status = session.getStatus();
        this.user = session.getAssignee().getName();
        this.userDisplayName = session.getAssignee().getDisplayName();
        this.projectId = session.getRelatedProject().getId().toString();
        this.projectKey = session.getRelatedProject().getKey();
        this.projectName = session.getRelatedProject().getName();
        this.userAvatarSrc = excaliburWebUtil.getSmallAvatarUrl(session.getAssignee());
        this.userLargeAvatarSrc = excaliburWebUtil.getLargeAvatarUrl(session.getAssignee());
        this.prettyStatus = excaliburWebUtil.getText("session.status.pretty." + session.getStatus());
        this.shared = session.isShared();
        this.isActive = isActive;
        this.url = BonfireConstants.SESSION_PAGE + session.getId().toString();
        this.permissions = permissions;
    }

    public SessionBean(LightSession session, ExcaliburWebUtil excaliburWebUtil, boolean isActive, SessionDisplayHelper permissions) {
        this.id = session.getId();
        this.name = session.getName();
        this.status = session.getStatus();
        this.user = session.getAssignee().getName();
        this.userDisplayName = session.getAssignee().getDisplayName();
        this.projectId = session.getRelatedProject().getId().toString();
        this.projectKey = session.getRelatedProject().getKey();
        this.projectName = session.getRelatedProject().getName();
        this.userAvatarSrc = excaliburWebUtil.getSmallAvatarUrl(session.getAssignee());
        this.userLargeAvatarSrc = excaliburWebUtil.getLargeAvatarUrl(session.getAssignee());
        this.prettyStatus = excaliburWebUtil.getText("session.status.pretty." + session.getStatus());
        this.shared = session.isShared();
        this.isActive = isActive;
        this.url = BonfireConstants.SESSION_PAGE + session.getId().toString();
        this.permissions = permissions;
    }
}
