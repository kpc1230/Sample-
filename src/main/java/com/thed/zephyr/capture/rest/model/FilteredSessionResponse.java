package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class FilteredSessionResponse {
    @XmlElement
    private List<SessionBean> sessions;

    @XmlElement
    private boolean hasAny; // false if there are no sessions visible for this user

    @XmlElement
    private boolean hasMore;

    @XmlElement
    private int nextStart;

    @XmlElement
    private int totalFilteredCount;

    public FilteredSessionResponse(List<SessionBean> projectSessions, boolean hasAny, boolean hasMore, int nextStart, int totalFilteredCount) {
        this.sessions = projectSessions;
        this.hasMore = hasMore;
        this.nextStart = nextStart;
        this.totalFilteredCount = totalFilteredCount;
        this.hasAny = hasAny;
    }
}
