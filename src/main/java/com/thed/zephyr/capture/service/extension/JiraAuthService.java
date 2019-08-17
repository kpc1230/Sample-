package com.thed.zephyr.capture.service.extension;

import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.be.BEAuthToken;
import com.thed.zephyr.capture.model.jira.CaptureUser;

/**
 * Created by snurulla on 8/17/2017.
 */
public interface JiraAuthService {

    BEAuthToken authenticateWithJira(String username, String password, String baseURL, String userAgent);

    CaptureUser getUserDetails(String username, String password, String baseURL);

    BEAuthToken createBEAuthTokenWithCookies(String clientKey, String userKey, String userAccountId, long timestamp, String jiraToken, String userAgent,String beLoggedInParam);

    BEAuthToken createBEAuthTokenWithApiToken(String clientKey, String userKey, String userAccountId, long timestamp, String apiToken, String userAgent,String beLoggedInParam);

    BEAuthToken createBEAuthTokenFromString(String token);

    String createStringTokenFromBEAuthToken(BEAuthToken beAuthToken);

}
