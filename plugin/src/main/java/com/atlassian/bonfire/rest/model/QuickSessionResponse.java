package com.atlassian.bonfire.rest.model;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class QuickSessionResponse {
    @XmlElement
    private Long id;

    @XmlElement
    private String name;

    @XmlElement
    private List<NoteBean> notes;

    @XmlElement
    private boolean hasNotes;

    @XmlElement
    private String additionalInfo;

    @XmlElement
    private String rawAdditionalInfo;

    @XmlElement
    private boolean hasAdditionalInfo;

    @XmlElement
    private boolean canEditSession;

    @XmlElement
    private boolean canAddNote;

    public QuickSessionResponse(LightSession session, List<NoteBean> notes, ExcaliburWebUtil excaliburWebUtil, boolean canEditSession,
                                boolean canAddNote) {
        this.id = session.getId();
        this.name = session.getName();
        this.notes = notes;
        this.hasNotes = notes.size() > 0;
        this.additionalInfo = excaliburWebUtil.renderWikiContent(session.getAdditionalInfo());
        this.rawAdditionalInfo = session.getAdditionalInfo();
        this.hasAdditionalInfo = StringUtils.isNotBlank(session.getAdditionalInfo());
        this.canEditSession = canEditSession;
        this.canAddNote = canAddNote;
    }
}
