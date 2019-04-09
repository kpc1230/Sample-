package com.thed.zephyr.capture.service.gdpr;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.service.gdpr.model.UserDTO;

import java.util.List;

/**
 * Created by Masud on 4/1/19.
 */
public interface GDPRUserService {
    List<UserDTO> getAndPushUserToMigration(AtlassianHostUser hostUser);
    void processToPushMigration(List<UserDTO> userList, String tenantId, String ctId);
}
