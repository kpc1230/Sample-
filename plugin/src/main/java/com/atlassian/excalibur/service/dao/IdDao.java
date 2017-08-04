package com.atlassian.excalibur.service.dao;

import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Callable;

@Service(IdDao.SERVICE)
public class IdDao {
    public static final String SERVICE = "bonfire-idDao";

    public static final long INITIAL_AUTO_ID = 10000;

    static final String KEY_ID = "Bonfire.Ids";

    @VisibleForTesting
    static final String LOCK_NAME = IdDao.class.getName();

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource
    private LockOperations lockOperations;

    /*
     * We keep the IDs in a virtual sequence, emulated by a table row.
     */
    public Long genNextId() {
        // Both gets and sets, so we do this under lock
        return lockOperations.callUnderLock(LOCK_NAME, new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Long nextId = persistenceService.getLong(KEY_ID, 1L, "data");
                if (nextId == null) {
                    nextId = INITIAL_AUTO_ID;
                } else {
                    nextId++;
                }
                persistenceService.setLong(KEY_ID, 1L, "data", nextId);
                return nextId;
            }
        });
    }
}
