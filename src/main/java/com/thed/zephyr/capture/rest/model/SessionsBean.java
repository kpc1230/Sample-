package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * A list of sessions for the user
 */
@XmlRootElement
public class SessionsBean {
    @XmlElement
    private List<SessionBean> privateSessions;

    @XmlElement
    private List<SessionBean> sharedSessions;

    public SessionsBean(List<SessionBean> privateSessions, List<SessionBean> sharedSessions) {
        this.privateSessions = privateSessions;
        this.sharedSessions = sharedSessions;
    }
}
