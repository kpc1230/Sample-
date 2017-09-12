package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.model.InviteSessionRequest;
import com.thed.zephyr.capture.model.Session;

/**
 * Created by Masud on 8/17/17.
 */
public interface InviteService {
    void sendInviteToSession(Session session, InviteSessionRequest inviteSessionRequest) throws Exception;
}
