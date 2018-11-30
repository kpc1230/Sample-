package com.thed.zephyr.capture.service.gdpr;

/**
 * @author manjunath
 *
 */
public interface UserAccountActivitesService {
	
	void saveUserActivities(String userAccountId, String tenantKey, String ctId, String url);

}
