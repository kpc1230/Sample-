package com.atlassian.excalibur.service.lock;

import com.atlassian.beehive.ClusterLock;
import com.atlassian.beehive.ClusterLockService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class TestClusterLockOperations {
    private static final String LOCK_NAME = "lock_1";

    private LockOperations lockOperations;

    @Mock
    private Callable<Integer> mockCallable;

    @Mock
    private ClusterLock mockLock;

    @Mock
    private ClusterLockService mockClusterLockService;

    @Mock
    private Runnable mockRunnable;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        lockOperations = new ClusterLockOperations(mockClusterLockService);
        when(mockLock.isHeldByCurrentThread())
                .thenReturn(false)
                .thenReturn(true);
    }

    @Test
    public void shouldLockForCallableIfNotAlreadyHeld() throws Exception {
        // Set up
        final Integer result = 42;
        when(mockCallable.call()).thenReturn(result);
        when(mockClusterLockService.getLockForName(LOCK_NAME)).thenReturn(mockLock);

        // Invoke
        final Integer actualResult = lockOperations.callUnderLock(LOCK_NAME, mockCallable);

        // Check
        assertEquals(result, actualResult);
        verify(mockClusterLockService).getLockForName(LOCK_NAME);
        verify(mockCallable).call();
        verify(mockLock).isHeldByCurrentThread();
        verify(mockLock).lock();
        verify(mockLock).unlock();
        verifyNoMoreInteractions(mockCallable, mockClusterLockService, mockLock);
    }

    @Test
    public void shouldLockForRunnableIfNotAlreadyHeld() {
        // Set up
        when(mockClusterLockService.getLockForName(LOCK_NAME)).thenReturn(mockLock);

        // Invoke
        lockOperations.runUnderLock(LOCK_NAME, mockRunnable);

        // Check
        verify(mockClusterLockService).getLockForName(LOCK_NAME);
        verify(mockRunnable).run();
        verify(mockLock).isHeldByCurrentThread();
        verify(mockLock).lock();
        verify(mockLock).unlock();
        verifyNoMoreInteractions(mockClusterLockService, mockLock, mockRunnable);
    }

    @Test
    public void shouldReEnterLockForRunnableIfAlreadyHeld() {
        // Set up
        when(mockClusterLockService.getLockForName(LOCK_NAME)).thenReturn(mockLock);

        // Invoke re-entrantly
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                lockOperations.runUnderLock(LOCK_NAME, mockRunnable);
            }
        });

        // Check
        verify(mockClusterLockService, times(2)).getLockForName(LOCK_NAME);
        verify(mockRunnable).run();
        verify(mockLock, times(2)).isHeldByCurrentThread();
        verify(mockLock).lock();
        verify(mockLock).unlock();
        verifyNoMoreInteractions(mockClusterLockService, mockLock, mockRunnable);
    }
}
