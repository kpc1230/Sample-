package com.thed.zephyr.capture.repositories;

import com.thed.zephyr.capture.model.SessionActivity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by aliakseimatsarski on 8/23/17.
 */
@Repository
public interface SessionActivityRepository extends CrudRepository<SessionActivity, String> {
}
