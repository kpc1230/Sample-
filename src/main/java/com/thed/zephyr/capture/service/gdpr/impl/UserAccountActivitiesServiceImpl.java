package com.thed.zephyr.capture.service.gdpr.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thed.zephyr.capture.model.gdpr.UserActivities;
import com.thed.zephyr.capture.repositories.dynamodb.UserAccountActivitiesRepository;
import com.thed.zephyr.capture.service.gdpr.UserAccountActivitesService;

/**
 * @author manjunath
 *
 */
@Service
public class UserAccountActivitiesServiceImpl implements UserAccountActivitesService {
	
	@Autowired
	private UserAccountActivitiesRepository userAccountActivitiesRepository;

	@Override
	public void saveUserActivities(String userAccountId, String tenantKey, String ctId, String url) {
		UserActivities userActivities = userAccountActivitiesRepository.findOne(userAccountId);
		if(userActivities != null) {
			userActivities.setUrl(url);
			userActivities.setLastAccessedTime(new Date());
		} else {
			userActivities = new UserActivities();
			userActivities.setAccountId(userAccountId);
			userActivities.setTenantKey(tenantKey);
			userActivities.setCtId(ctId);
			userActivities.setUrl(url);
			userActivities.setLastAccessedTime(new Date());
		}
		userAccountActivitiesRepository.save(userActivities);		
	}

}
