package com.thed.zephyr.capture.repositories.dynamodb;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.thed.zephyr.capture.model.gdpr.UserActivities;

/**
 * @author manjunath
 *
 */
@Repository
public interface UserAccountActivitiesRepository extends CrudRepository<UserActivities, String>  {

}
