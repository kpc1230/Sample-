package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.properties.BonfireConstants;
import com.thed.zephyr.capture.upgradetasks.UpgradeTaskKit;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.service.dao.UpgradeTaskDao;
import com.atlassian.excalibur.web.util.VersionKit;
import com.atlassian.jira.util.BuildUtilsInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

@Service(BonfireBuildCheckService.SERVICE)
public class BonfireBuildCheckService extends BonfireServiceSupport {
    public static final String SERVICE = "bonfire-BonfireBuildCheckService";

    private static final String KEY_EX_PROPS = "Excalibur.properties";
    private static final long GLOBAL_ENTITY_ID = 1l;
    private static final String SESSION_STORAGE_KEY = "Excalibur.Session.Storage";
    public static final String BONFIRE_HIGHEST_RUN_VERSION = "Bonfire.Highest.Run.Version";

    @Resource(name = UpgradeTaskDao.SERVICE)
    private UpgradeTaskDao upgradeTaskDao;

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource(name = BuildPropertiesService.SERVICE)
    private BuildPropertiesService buildPropertiesService;

    @JIRAResource
    private BuildUtilsInfo jiraBuildUtilsInfo;

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private AtomicBoolean buildIsinGoodShape = new AtomicBoolean(false);

    /**
     * This will check that no downgrading has occurred and that upgrade tasks required have been run.  If this is not the case then an error
     * collection will be filled out
     *
     * @return an error collection that will be empty if all is well or have messages if this are not well
     */
    public ErrorCollection checkTheStateOfTheNation() {
        ErrorCollection errorCollection = new ErrorCollection();

        if (buildIsinGoodShape.get()) {
            return errorCollection;
        }

        if (!isJIRAVersionSupported(errorCollection)) {
            buildIsinGoodShape.set(false);
            return errorCollection;
        }

        if (checkDowngradeHasOccurred(errorCollection)) {
            buildIsinGoodShape.set(false);
            return errorCollection;
        }

        if (requiresUpgradeTask()) {
            StringBuilder sb = new StringBuilder();
            for (String targetVersion : UpgradeTaskKit.getVersionsWithUpgradeTasks()) {
                if (!upgradeTaskDao.wasUpgradeTasksRunFor(targetVersion)) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(targetVersion);
                }
            }
            if (sb.length() > 0) {
                errorCollection.addError(getText("upgrade.tasks.not.run", sb.toString()));
            } else {
                buildIsinGoodShape.set(true);
            }
        } else {
            buildIsinGoodShape.set(true);
        }

        return errorCollection;
    }

    private boolean requiresUpgradeTask() {
        // If there are sessions in this location then we want to do an upgrade.
        if (persistenceService.getDataLongKey(KEY_EX_PROPS, GLOBAL_ENTITY_ID, SESSION_STORAGE_KEY) != null) {
            return true;
        }
        return false;
    }

    private boolean isGreaterThanOrEqualTo(String currentVersion, String highestRunVersion) {
        VersionKit.SoftwareVersion highestRun = VersionKit.parse(highestRunVersion);
        VersionKit.SoftwareVersion current = VersionKit.parse(currentVersion);
        return current.isGreaterThanOrEqualTo(highestRun);
    }

    private boolean isJIRAVersionSupported(ErrorCollection errorCollection) {
        VersionKit.SoftwareVersion jira = VersionKit.parse(jiraBuildUtilsInfo.getVersion());
        if (!jira.isGreaterThanOrEqualTo(BonfireConstants.LOWEST_SUPPORTED_JIRA)) {
            errorCollection.addError(getText("jira.version.violation", buildPropertiesService.getVersion()));
            return false;
        }
        return true;
    }

    private boolean checkDowngradeHasOccurred(ErrorCollection errorCollection) {
        String currentVersion = buildPropertiesService.getVersion();
        String highestRunVersion = getHighestRunVersion();
        if (StringUtils.isNotBlank(highestRunVersion) && !isGreaterThanOrEqualTo(currentVersion, highestRunVersion)) {
            errorCollection.addError(getText("plugin.downgrade.has.occurred", currentVersion, highestRunVersion));
            return true;
        }
        return false;
    }

    public void persistRunVersion(String version) {
        String highestRunVersion = getHighestRunVersion();
        if (StringUtils.isBlank(highestRunVersion)) {
            // its never been set to set it
            persistenceService.setString(KEY_EX_PROPS, GLOBAL_ENTITY_ID, BONFIRE_HIGHEST_RUN_VERSION, version);
        } else if (isGreaterThanOrEqualTo(version, highestRunVersion)) {
            persistenceService.setString(KEY_EX_PROPS, GLOBAL_ENTITY_ID, BONFIRE_HIGHEST_RUN_VERSION, version);
        }
    }

    public String getHighestRunVersion() {
        return persistenceService.getString(KEY_EX_PROPS, GLOBAL_ENTITY_ID, BONFIRE_HIGHEST_RUN_VERSION);
    }

    @Override
    protected void onPluginStart() {
    }

    @Override
    protected void onPluginStop() {
    }

    @Override
    protected void onClearCache() {
        buildIsinGoodShape.set(false);
    }
}
