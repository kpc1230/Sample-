package com.thed.zephyr.capture.service.cache.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by aliakseimatsarski on 1/14/16.
 */
@Service
public class LockServiceImpl implements LockService {

    @Autowired
    private Logger log;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public Boolean tryLock(AcHostModel acHostModel, String lockKey, Integer lockTimeOut) throws HazelcastInstanceNotDefinedException {
        IMap<String, String> lockIMap = getLockIMap(acHostModel);
        lockTimeOut = lockTimeOut != null ? lockTimeOut : ApplicationConstants.DEFAULT_LOCK_TIMEOUT_SEC;
        try {
            Boolean lockResult = lockIMap.tryLock(lockKey, lockTimeOut, TimeUnit.SECONDS);
            log.debug("Try to get IMapLock tenantId: " + acHostModel.getClientKey() + " lockKey: " + lockKey + " result: " + lockResult);
            return lockResult;
        } catch (InterruptedException exception) {
            log.warn("Can't acquire lock with key: " + lockKey + " tenantId: " + acHostModel.getClientKey(), exception);
            return false;
        }
    }

    @Override
    public void deleteLock(AcHostModel acHostModel, String lockKey) throws HazelcastInstanceNotDefinedException {
        IMap<String, String> lockIMap = getLockIMap(acHostModel);
        lockIMap.forceUnlock(lockKey);
        log.debug("IMapLock was deleted lockKey: " + lockKey + " tenantId: " + acHostModel.getClientKey());
    }

    private IMap<String, String> getLockIMap(AcHostModel acHostModel) throws HazelcastInstanceNotDefinedException {
        HazelcastInstance hz = hazelcastInstance;
        String iMapName = String.valueOf(acHostModel.getClientKey() + "_" + ApplicationConstants.HZ_LOCK_IMAP_NAME);
        IMap<String, String> lockIMap = hz.getMap(iMapName);
        log.debug("Got IMap with name: " + iMapName + " size: " + lockIMap.size() + " tenantId: " + acHostModel.getClientKey());

        return lockIMap;
    }
}