package com.thed.zephyr.capture.events;

import com.atlassian.excalibur.model.Session;

public class CompleteSessionEvent extends SessionStatusChangedEvent {

    public CompleteSessionEvent(Session session) {
        super(session);
    }
}
