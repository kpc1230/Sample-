package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.Session;

/**
 * Created by Masud on 8/17/17.
 */
public interface InviteService {
    void sendInviteToSession(Session session, String emailAddress, String message) throws Exception;
}
