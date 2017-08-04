package com.atlassian.excalibur.service.dao;

import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service(UpgradeTaskDao.SERVICE)
public class UpgradeTaskDao {
    public static final String SERVICE = "bonfire-UpgradeTaskDao";

    private static final String TABLE_UPGRADE_TASK_HISTORY = "Bonfire.Upgrade.Task.History";

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    public boolean wasUpgradeTasksRunFor(final String bonfireVersion) {
        final String when = persistenceService.getString(TABLE_UPGRADE_TASK_HISTORY, 1L, bonfireVersion);
        return StringUtils.isNotBlank(when);
    }
}

