package com.thed.zephyr.capture.service.cache;

import com.thed.zephyr.capture.model.AcHostModel;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Masud on 8/16/17.
 */
public interface ITenantAwareCache {

    public Object get(String key);

    public Object get(AcHostModel acHostModel, String key);

    <T> T getOrElse(String key, Callable<T> block, int expiration) throws Exception;

    <T> T getOrElse(AcHostModel acHostModel, String key, Callable<T> block, int expiration) throws Exception;

    void set(String key, Object value);

    void set(AcHostModel acHostModel, String key, Object value);

    Boolean delete(AcHostModel acHostModel, String key);

    void set(AcHostModel acHostModel, String key, Object value, Integer expireTime, TimeUnit timeUnit);

    void set(String key, Object value, Integer timeout);

    void add(String key, int expiration, Object value) throws ExecutionException, InterruptedException;

    Boolean delete(String key) throws ExecutionException, InterruptedException;

    void remove(AcHostModel acHostModel, String key);

    public Future getAsync(String key);


}
