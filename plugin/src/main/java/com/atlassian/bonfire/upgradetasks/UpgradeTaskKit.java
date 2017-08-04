package com.atlassian.bonfire.upgradetasks;

import com.atlassian.bonfire.exceptions.UnsupportedJIRAVersionException;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.BonfireBuildCheckService;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.web.util.VersionKit;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opensymphony.module.propertyset.PropertySet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static org.apache.commons.lang.Validate.notNull;

/**
 * <p>
 * Utility methods for upgrade tasks.
 * </p>
 * <p>
 * Don't mess with the implementations too much since they need to stay fairly constant going into the future.
 * </p>
 * <p>
 * That's said, we still need some code commonality.  So in general be careful how you change this code, mean to do it
 * and mostly add new code as a change strategy
 * </p>
 * <p>
 * If adding an upgrade task, ensure that the plugin version is added to {@link com.atlassian.bonfire.service.BonfireBuildCheckService}
 * </p>
 *
 * @since v1.3
 */
@Service(UpgradeTaskKit.SERVICE)
public class UpgradeTaskKit {
    @Resource(name = BonfireBuildCheckService.SERVICE)
    private BonfireBuildCheckService buildCheckService;

    @JIRAResource
    private BuildUtilsInfo jiraBuildUtilsInfo;

    @JIRAResource
    private JiraPropertySetFactory jiraPropertySetFactory;

    /**
     * Everytime you add an upgrade tasks to Bonfire you MUST add the version here And the upgrade task itself
     * MUST mark itself as done against that version.
     */
    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private static Set<String> VERSIONS_WITH_UPGRADE_TASKS = Sets.newHashSet(
            "2.1000.0"
    );


    /**
     * @return a Set of all the versions of Bonfire that had upgrade tasks
     */
    public static Set<String> getVersionsWithUpgradeTasks() {
        return VERSIONS_WITH_UPGRADE_TASKS;
    }

    private static final Logger log = Logger.getLogger(UpgradeTaskKit.class);
    private static final String TABLE_UPGRADE_TASK_HISTORY = "Bonfire.Upgrade.Task.History";
    public static final String SERVICE = "bonfire-UpgradeTaskKit";

    /**
     * Makes an upgrade task done in terms of a bonfire version
     *
     * @param bonfireVersion   the version of bonfire
     * @param upgradeTaskClass the class of the upgrade tasks
     */
    public void markTaskAsDoneViaPS(final String bonfireVersion, final Class upgradeTaskClass) {
        setStringInPS(TABLE_UPGRADE_TASK_HISTORY, 1L, bonfireVersion, upgradeTaskClass.getName() + "_" + new DateTime().toString(ISODateTimeFormat.dateTime()));
        // Update the value of "Greatest run  version"
        buildCheckService.persistRunVersion(bonfireVersion);
        log.warn("Marking upgrade task " + upgradeTaskClass.getName() + " as done for version " + bonfireVersion);
    }

    public static class UpgradeTaskInfo {
        private final String bonfireVersion;
        private final String upgradeClassName;
        private final DateTime when;

        public UpgradeTaskInfo(String bonfireVersion, String upgradeClassName) {
            this.bonfireVersion = bonfireVersion;
            this.upgradeClassName = getClassName(StringUtils.defaultString(upgradeClassName));
            this.when = getWhen(StringUtils.defaultString(upgradeClassName));
        }

        public String getBonfireVersion() {
            return bonfireVersion;
        }

        public String getUpgradeClassName() {
            return upgradeClassName;
        }

        public DateTime getWhen() {
            return when;
        }

        private DateTime getWhen(String upgradeClassName) {
            DateTime unkown = new DateTime(0);
            int i = upgradeClassName.lastIndexOf("_");
            if (i == -1) {
                return unkown;
            } else {
                String dts = upgradeClassName.substring(i + 1);
                try {
                    return ISODateTimeFormat.dateTime().parseDateTime(dts);
                } catch (Exception e) {
                    return unkown;
                }
            }
        }

        private String getClassName(String upgradeClassName) {
            int i = upgradeClassName.lastIndexOf("_");
            return i == -1 ? "???" + upgradeClassName : upgradeClassName.substring(0, i);
        }
    }

    public List<UpgradeTaskInfo> getRunUpgradeTasks() {
        PropertySet propertySet = getPropertySet(TABLE_UPGRADE_TASK_HISTORY, 1L);
        @SuppressWarnings("unchecked") Collection<String> bonfireVersions = propertySet.getKeys();
        ArrayList<UpgradeTaskInfo> upgradeTaskInfos = Lists.newArrayList();
        for (String bonfireVersion : bonfireVersions) {
            String upgradeTaskClassName = propertySet.getString(bonfireVersion);
            upgradeTaskInfos.add(new UpgradeTaskInfo(bonfireVersion, upgradeTaskClassName));
        }
        Collections.sort(upgradeTaskInfos, new Comparator<UpgradeTaskInfo>() {
            @Override
            public int compare(UpgradeTaskInfo o1, UpgradeTaskInfo o2) {
                return o1.getWhen().compareTo(o2.getWhen());
            }
        });
        return upgradeTaskInfos;
    }


    /**
     * Loads a PropertySet from the storage given a sequenceName/sequenceId mapping.
     *
     * @param entityName the name of the property set entity
     * @param entityId   the id of the related entity
     * @return a PropertySet for the given entityName and entityId.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PropertySet getPropertySet(String entityName, Long entityId) {
        return jiraPropertySetFactory.buildCachingPropertySet(entityName, entityId, false);
    }

    /**
     * Set a Long property into a named PropertySet
     *
     * @param entityName the name of the property set entity
     * @param entityId   the id to use
     * @param key        the key value to store
     * @param value      the value to store for that key
     */
    public void setLongInPS(String entityName, Long entityId, String key, Long value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setLong(key, value);
    }

    /**
     * Get a Long property from a named PropertySet
     *
     * @param entityName the name of the property set entity
     * @param entityId   the id to use
     * @param key        the key value to retrive
     * @return a Long
     */
    public Long getLongFromPS(String entityName, Long entityId, String key) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);

        return exists(entityName, entityId, key) ? getPropertySet(entityName, entityId).getLong(key) : null;
    }

    /**
     * Set a Text property into a named PropertySet
     *
     * @param entityName the name of the property set entity
     * @param entityId   the id to use
     * @param key        the key value to store
     * @param value      the value to store for that key
     */
    public void setTextInPS(String entityName, Long entityId, String key, String value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setText(key, value);
    }

    /**
     * Set a String property into a named PropertySet
     *
     * @param entityName the name of the property set entity
     * @param entityId   the id to use
     * @param key        the key value to store
     * @param value      the value to store for that key
     */
    public void setStringInPS(String entityName, Long entityId, String key, String value) {
        notNull(entityName);
        notNull(entityId);
        notNull(key);
        notNull(value);

        getPropertySet(entityName, entityId).setString(key, value);
    }

    /**
     * Does a given key exist?
     *
     * @param entityName the name of the property set entity
     * @param entityId   the id to use
     * @param key        the key value to check
     * @return true if the property key exists
     */
    public boolean exists(String entityName, Long entityId, String key) {
        return getPropertySet(entityName, entityId).exists(key);
    }

    /**
     * This method should only be called by upgrade tasks and should be called by all upgrade tasks
     *
     * @throws UnsupportedJIRAVersionException - to stop upgrade tasks from finishing
     */
    public void checkJIRAVersion() throws UnsupportedJIRAVersionException {
        VersionKit.SoftwareVersion jira = VersionKit.parse(jiraBuildUtilsInfo.getVersion());
        if (!jira.isGreaterThanOrEqualTo(BonfireConstants.LOWEST_SUPPORTED_JIRA)) {
            throw new UnsupportedJIRAVersionException();
        }
    }
}
