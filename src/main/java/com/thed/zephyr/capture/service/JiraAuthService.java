package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.AcHostModel;

/**
 * Created by snurulla on 8/17/2017.
 */
public interface JiraAuthService {
    Boolean authenticateWithJira(String username, String Password, String baseURL);

    AcHostModel getAcHostModelbyBaseUrl(String baseUrl);

}
