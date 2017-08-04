package com.atlassian.bonfire.service.dao;

import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service(AdvancedCFDao.SERVICE)
public class AdvancedCFDao {
    public static final String SERVICE = "bonfire-advancedCFTDao";

    @VisibleForTesting
    static final String LOCK_NAME = AdvancedCFDao.class.getName();

    private final static String ADVANCED_CFT_ENTITY_NAME = "bonfire.temp.advancedcft";
    private final static Long ADVANCED_CFT_ENTITY_ID = -1l; // Each user should only ever have 1 set of advancedCFT data saved

    @Resource
    private LockOperations lockOperations;

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    public String load(final ApplicationUser user) {
        final String userKey = user.getKey();
        return persistenceService.getText(ADVANCED_CFT_ENTITY_NAME, ADVANCED_CFT_ENTITY_ID, userKey);
    }

    public void save(final ApplicationUser user, final String data) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                String userKey = user.getKey();
                persistenceService.setText(ADVANCED_CFT_ENTITY_NAME, ADVANCED_CFT_ENTITY_ID, userKey, data);
            }
        });
    }

    public void delete(final ApplicationUser user) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                final String userKey = user.getKey();
                persistenceService.delete(ADVANCED_CFT_ENTITY_NAME, ADVANCED_CFT_ENTITY_ID, userKey);
            }
        });
    }
}
