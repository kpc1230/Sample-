package com.atlassian.bonfire.upgradetasks;

import java.util.Collection;
import javax.annotation.Nonnull;
import com.google.common.collect.ImmutableList;
import cloud.atlassian.upgrade.api.ServerDowngradeTask;
import cloud.atlassian.upgrade.api.UpgradeTask;
import cloud.atlassian.upgrade.api.UpgradeTaskFactory;

/**
 * The upgrade task factory that provides the upgrade tasks to run in the new framework.
 */
public class BonfireUpgradeTaskFactory implements UpgradeTaskFactory {

    private Collection<UpgradeTask> upgradeTasks;

    public BonfireUpgradeTaskFactory(Collection<UpgradeTask> upgradeTasks) {
        this.upgradeTasks = upgradeTasks;
    }

    @Override
    public String getProductDisplayName() {
        return "Capture for JIRA";
    }

    @Override
    public String getProductMinimumVersion() {
        return "2.1000.0";
    }

    @Override
    public int getMinimumBuildNumber() {
        return 0;
    }

    @Nonnull
    @Override
    public Collection<UpgradeTask> getAllUpgradeTasks() {
        return upgradeTasks;
    }

    @Override
    public Collection<ServerDowngradeTask> getAllServerDowngradeTasks() {
        return ImmutableList.of();
    }
}
