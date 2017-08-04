package com.atlassian.excalibur.service.lock;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

/**
 * A LockOperations implementation that uses cluster-safe locks.
 */
@Component
public class ClusterLockOperations implements LockOperations {
    private final ClusterLockService clusterLockService;

    @Autowired
    public ClusterLockOperations(final ClusterLockService clusterLockService) {
        this.clusterLockService = clusterLockService;
    }

    @Override
    public <T> T callUnderLock(final String lockName, final Callable<T> function) {
        final ClusterLock lock = getClusterLockService().getLockForName(lockName);

        if(lock.isHeldByCurrentThread()){
            return callWithoutLock(function);
        }
        lock.lock();
        try {
            return callWithoutLock(function);
        } finally {
            lock.unlock();
        }
    }

    @Nonnull
    private ClusterLockService getClusterLockService() {
        if (clusterLockService == null) {
            throw new IllegalStateException("Could not get ClusterLockService");
        }
        return clusterLockService;
    }

    private <T> T callWithoutLock(final Callable<T> function) {
        try {
            return function.call();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runUnderLock(final String lockName, final Runnable sideEffect) {
        callUnderLock(lockName, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                sideEffect.run();
                return null;
            }
        });
    }
}
