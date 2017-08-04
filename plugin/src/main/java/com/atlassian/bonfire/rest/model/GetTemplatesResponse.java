package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;

public class GetTemplatesResponse {
    @XmlElement
    private boolean hasMore;

    @XmlElement
    private Collection<TemplateBean> templates;

    public GetTemplatesResponse(boolean hasMore, Collection<TemplateBean> templates) {
        this.hasMore = hasMore;
        this.templates = templates;
    }
}
