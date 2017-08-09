package com.thed.zephyr.capture.rest.model;

import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AdditionalInfoResponse {
    @XmlElement
    private String additionalInfoRaw;

    @XmlElement
    private String additionalInfoDisplay;

    @XmlElement
    private boolean isEmpty;

    public AdditionalInfoResponse() {
    }

    public AdditionalInfoResponse(String additionalInfo, ExcaliburWebUtil webUtil) {
        this.additionalInfoRaw = additionalInfo;
        this.isEmpty = StringUtils.isEmpty(additionalInfo.trim());
        if (this.isEmpty) {
            this.additionalInfoDisplay = webUtil.getText("session.section.additionalinfo.empty");
        } else {
            this.additionalInfoDisplay = webUtil.renderWikiContent(additionalInfo);
        }
    }
}
