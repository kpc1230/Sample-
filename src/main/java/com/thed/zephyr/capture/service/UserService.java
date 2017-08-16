package com.thed.zephyr.capture.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by Masud on 8/15/17.
 */
public interface UserService {
    JsonNode getUserProperty(String userName, String propName);

    JsonNode getAllUserProperties(String userName);

    boolean deleteUserProperty(String userName, String propName);

    boolean createOrUpdateUserProperty(String userName, String propName, JsonNode jsonNode);
}
