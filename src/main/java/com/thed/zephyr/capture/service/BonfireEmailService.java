package com.thed.zephyr.capture.service;

import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.user.ApplicationUser;

public interface BonfireEmailService {
    public static String SERVICE = "bonfire-emailService";

    public void sendInviteToSession(ApplicationUser sender, Session session, String emailAddress, String message);
}
