package com.thed.zephyr.capture.service.extension;

import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.jira.CaptureUser;

/**
 * Created by snurulla on 8/17/2017.
 */
public interface JiraAuthService {
    Boolean authenticateWithJira(String username, String password, String baseURL);

    CaptureUser getUserDetails(String username, String password, String baseURL);

}
