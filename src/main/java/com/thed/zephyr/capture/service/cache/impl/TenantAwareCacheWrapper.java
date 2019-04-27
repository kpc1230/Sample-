package com.thed.zephyr.capture.service.cache.impl;

import com.hazelcast.core.HazelcastInstance;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
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

    @Override
    public Object get(AcHostModel acHostModel, String key) {
        Object result = null;
        String clientKey = acHostModel.getClientKey();
        try{
            validateArgs(key, clientKey);
            result = hazelcastInstance.getMap(clientKey).get(key);
        } catch (IllegalArgumentException exception){
            log.warn("The key or clientKey are null during getting the object from cache key:{} clientKey:{}", key, clientKey, exception);
            result = null;
        } catch (Exception exception){
            log.error("Error during getting object from cache key:{} clientKey:{}", key, clientKey, exception);
            result = null;
        }

        return result;
    }

    @Override
    public <T> T getOrElse(AcHostModel acHostModel, String key, Callable<T> block, int expiration) throws Exception {
        @SuppressWarnings("unchecked")
        T val = (T) get(acHostModel, key);
        if (val == null) {
            val = block.call();

            return val != null?(T)set(acHostModel, key, val, expiration, TimeUnit.SECONDS):null;
        }

        return val;
    }

    @Override
    public Object set(AcHostModel acHostModel, String key, Object value) {
        Object result = value;
        String clientKey = acHostModel.getClientKey();
        try{
            validateArgs(key, clientKey);
            hazelcastInstance.getMap(clientKey).set(key, value);
        } catch (IllegalArgumentException exception){
            log.warn("The key or clientKey are null during setting the object into cache key:{} clientKey:{}", key, clientKey, exception);
            result = null;
        } catch (Exception exception){
            log.error("Error during setting object into cache key:{} clientKey:{}", key, clientKey, exception);
            result = null;
        }

        return result;
    }

    @Override
    public Boolean delete(AcHostModel acHostModel, String key) {
        String clientKey = acHostModel.getClientKey();
        try{
            validateArgs(key, clientKey);
            hazelcastInstance.getMap(clientKey).delete(key);
            return true;
        } catch (IllegalArgumentException exception){
            log.warn("The key or clientKey are null during delete the object from cache key:{} clientKey:{}", key, clientKey, exception);
        } catch (Exception exception){
            log.error("Error during delete object from cache key:{} clientKey:{}", key, clientKey, exception);
        }

        return false;
    }

    @Override
    public Object set(AcHostModel acHostModel, String key, Object value, Integer expireTime, TimeUnit timeUnit) {
        Object result = value;
        String clientKey = acHostModel.getClientKey();
        try{
            validateArgs(key, clientKey);
            hazelcastInstance.getMap(clientKey).set(key, value, expireTime, timeUnit);
        } catch (IllegalArgumentException exception){
            log.warn("The key or clientKey are null during getting the object from cache key:{} clientKey:{}", key, clientKey, exception);
            result = null;
        } catch (Exception exception){
            log.error("Error during set value to cache key:{} clientKey:{}", key, clientKey, exception);
            result = null;
        }

        return result;
    }

    @Override
    public void remove(AcHostModel acHostModel, String key) {
        String clientKey = acHostModel.getClientKey();
        try{
            validateArgs(key, clientKey);
            hazelcastInstance.getMap(clientKey).remove(key);
        } catch (IllegalArgumentException exception){
            log.warn("The key or clientKey are null during remove the object from cache key:{} clientKey:{}", key, clientKey, exception);
        } catch (Exception exception){
            log.error("Error during remove object from cache key:{} clientKey:{}", key, clientKey, exception);
        }
    }

    @Override
    public void clearTenantCache(AcHostModel acHostModel) {
        String clientKey = acHostModel.getClientKey();
        try{
            validateArgs("key", clientKey);
            hazelcastInstance.getMap(clientKey).clear();
            CaptureUtil.updateTenantCache(acHostModel, hazelcastInstance);
        } catch (IllegalArgumentException exception){
            log.warn("The clientKey is null during clear iMap in cache clientKey:{}", clientKey, exception);
        } catch (Exception exception){
            log.error("Error during clear iMap in cache clientKey:{}", clientKey, exception);
        }
    }

    private void validateArgs(String key, String clientKey){
        if (StringUtils.isBlank(key) || StringUtils.isBlank(clientKey)){
            throw  new IllegalArgumentException();
        }
    }
}
