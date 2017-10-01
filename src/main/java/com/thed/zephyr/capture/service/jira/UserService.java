package com.thed.zephyr.capture.service.jira;

import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.model.jira.CaptureUser;

/**
 * Created by Masud on 8/15/17.
 */
public interface UserService {
    JsonNode getUserProperty(String userName, String propName);

    JsonNode getAllUserProperties(String userName);

    boolean deleteUserProperty(String userName, String propName);

    boolean createOrUpdateUserProperty(String userName, String propName, JsonNode jsonNode);

    JsonNode getAssignableUserByProjectKey(String projectKey,String username);

    JsonNode getAssignableUserByProjectKey(String projectKey);

    CaptureUser findUserByName(String username);

    CaptureUser findUserByKey(String key);

}
