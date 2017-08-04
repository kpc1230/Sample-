package com.atlassian.excalibur.service.lock;

import java.util.concurrent.Callable;

/**
 * Provides lock-related operations.
 */
public interface LockOperations {
    /**
     * Calls the given function under the named lock, as follows:
     * <ul>
     * <li>If the current thread already holds that lock, i.e. the lock is
     * being re-entered, this method calls the function within it and does not
     * release it.</li>
     * <li>Otherwise, this method blocks until it acquires the lock, calls the
     * function, then releases the lock.</li>
     * </ul>
     *
     * @param lockName the globally unique name of the lock to acquire, if not already held (required)
     * @param function the function to perform (required)
     * @param <T>      the return type of the function
     * @return the return value of the function
     * @throws RuntimeException if the Callable throws an exception
     */
    <T> T callUnderLock(String lockName, Callable<T> function);

    /**
     * Runs the given side-effect under the named lock, as follows:
     * <ul>
     * <li>If the current thread already holds that lock, i.e. the lock is
     * being re-entered, this method runs the side-effect within it and does not
     * release it.</li>
     * <li>Otherwise, this method blocks until it acquires the lock, runs the
     * side-effect, then releases the lock.</li>
     * </ul>
     * <p>
     * This method is cleaner than (but equivalent to) calling {@link #callUnderLock} with a Callable&lt;Void&gt;.
     * </p>
     *
     * @param lockName   the globally unique name of the lock to acquire, if not already held (required)
     * @param sideEffect the side effect to perform (required)
     * @throws RuntimeException if the Callable throws an exception
     */
    void runUnderLock(String lockName, Runnable sideEffect);
}
