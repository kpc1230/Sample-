package com.thed.zephyr.capture.service.cache;


import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.AcHostModel;

/**
 * Created by aliakseimatsarski on 1/14/16.
 */
public interface LockService {

    Boolean tryLock(String clientKey, String lockKey, Integer lockTimeOut)  throws HazelcastInstanceNotDefinedException;

    void deleteLock(String clientKey, String lockKey) throws HazelcastInstanceNotDefinedException;
}
