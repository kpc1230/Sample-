package com.atlassian.bonfire.rest.model;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.util.model.SessionDisplayHelper;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class ExtensionSpecificSessionBean extends SessionBean {
    @XmlElement
    private Integer noteCount;

    @XmlElement
    private Integer issuesRaisedCount;

    @XmlElement
    private Integer participantCount;

    @XmlElement
    private String additionalInfo;

    @XmlElement
    private String rawAdditionalInfo;

    @XmlElement
    private List<IssueBean> relatedIssues;

    @XmlElement
    private String defaultTemplateId;

    public ExtensionSpecificSessionBean() {
        super();
    }

    public ExtensionSpecificSessionBean(LightSession session, ExcaliburWebUtil excaliburWebUtil, boolean isActive, SessionDisplayHelper permissions,
                                        Integer noteCount, Integer issuesRaisedCount, Integer activeParticipantCount, List<IssueBean> relatedIssues) {
        super(session, excaliburWebUtil, isActive, permissions);
        this.noteCount = noteCount;
        this.issuesRaisedCount = issuesRaisedCount;
        this.participantCount = activeParticipantCount;
        this.additionalInfo = excaliburWebUtil.renderWikiContent(session.getAdditionalInfo());
        this.rawAdditionalInfo = session.getAdditionalInfo();
        this.relatedIssues = relatedIssues;
        this.defaultTemplateId = session.getDefaultTemplateId();
    }
}
