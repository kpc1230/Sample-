package com.thed.zephyr.capture.service;

import org.joda.time.DateTimeZone;


/**
 * TimeZoneService for Bonfire, which wraps the JIRA TimeZoneManager, who only exists in JIRA 4.4+
 */
public interface TimeZoneService {

    public static final String SERVICE = "bonfire-TimeZoneService";

    /**
     * Return the time zone of the user who is currently logged in.
     * If user specific timezones don't exist, returns the server timezone.
     *
     * @return the time zone.
     */
    DateTimeZone getLoggedInUserTimeZone();
}
