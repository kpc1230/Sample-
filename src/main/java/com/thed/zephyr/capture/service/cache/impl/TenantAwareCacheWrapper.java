package com.thed.zephyr.capture.service.cache.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.cache.LockService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Masud on 8/16/17.
 */
@Service
public class TenantAwareCacheWrapper implements ITenantAwareCache {

    @Autowired
    private Logger log;
    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public Object get(AcHostModel acHostModel, String key) {
        if (!StringUtils.isNotBlank(key)) {
            return null;
        }
        String tenantId = acHostModel.getClientKey();
        if (tenantId != null && key != null) {
            return hazelcastInstance.getMap(tenantId).get(key);
        } else {
            return cacheManager.getCache(key);
        }
    }

    @Override
    public <T> T getOrElse(AcHostModel acHostModel, String key, Callable<T> block, int expiration) throws Exception {
        String tenantId = acHostModel.getClientKey();
        if (tenantId != null) {
            @SuppressWarnings("unchecked")
            T val1 = (T) get(acHostModel, key);
            if (val1 == null) {
                T val2 = block.call();
                if (val2 != null)    //Null values not allowed
                    set(acHostModel, key, val2, expiration, TimeUnit.SECONDS);
                return val2;
            }

            return val1;
        } else {
            return (T) hazelcastInstance.getCacheManager().getCache(key);
        }
    }

    @Override
    public void set(AcHostModel acHostModel, String key, Object value) {
        String tenantId = acHostModel.getClientKey();
        if (tenantId != null && key != null)
            hazelcastInstance.getMap(tenantId).set(key, value);

    }

    @Override
    public Boolean delete(AcHostModel acHostModel, String key) {
        String tenantId = acHostModel.getClientKey();
        if (key != null) {
            hazelcastInstance.getMap(tenantId).delete(key);
            return true;
        }
        return false;
    }

    @Override
    public void set(AcHostModel acHostModel, String key, Object value, Integer expireTime, TimeUnit timeUnit) {
        String tenantId = acHostModel.getClientKey();
        if (tenantId != null && key != null)
            hazelcastInstance.getMap(tenantId).set(key, value, expireTime, timeUnit);
    }

    @Override
    public void remove(AcHostModel acHostModel, String key) {
        String tenantId = acHostModel.getClientKey();
        if (tenantId != null && key != null) {
            hazelcastInstance.getMap(tenantId).remove(key);
        } else {
            hazelcastInstance.getCacheManager().getCache(key);
        }
    }

    @Override
    public void clearTenantCache(AcHostModel acHostModel) {
        String tenantId = acHostModel.getClientKey();
        if (tenantId != null) {
            hazelcastInstance.getMap(tenantId).clear();
        }
    }

    @Override
    public void displayTenantCache(AcHostModel acHostModel) {
        String tenantId = acHostModel.getClientKey();
        if (tenantId != null) {
            hazelcastInstance.getMap(tenantId).forEach((k, v) -> {
                log.debug("Cache Key available : " + k);
            });
        }
    }
}
