package com.thed.zephyr.capture.service;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.vcache.RequestCache;
import com.atlassian.vcache.VCacheFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @since v2.9.5
 */
@Service(CaptureAdminSettingsService.SERVICE)
public class CaptureAdminSettingsServiceImpl implements CaptureAdminSettingsService {
    private static final long FEEDBACK_ENTITY_ID = 2L;
    private static final long CAPTURE_SERVICEDESK_ENTITY_ID = 3L;
    private static final long CAPTURE_BUSINESS_ENTITY_ID = 4L;
    private static final String BONFIRE_FEEDBACK_SETTING_KEY = "bonfire-feedback";
    private static final String CAPTURE_JIRA_SERVICEDESK_SETTING_KEY = "capture-jira-servicedesk";
    private static final String CAPTURE_JIRA_BUSINESS_SETTING_KEY = "capture-jira-business";

    private static final String VCACHE_KEY = CaptureAdminSettingsServiceImpl.class.getName() + ".settingsCache";

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @JIRAResource
    private VCacheFactory vCacheFactory;

    @Override
    public Boolean isFeedbackEnabled() {
        return getBooleanValueWithDefault(BONFIRE_FEEDBACK_SETTING_KEY, FEEDBACK_ENTITY_ID, true);
    }

    @Override
    public void setFeedbackEnabled(final Boolean status) {
        persistBooleanValue(BONFIRE_FEEDBACK_SETTING_KEY, FEEDBACK_ENTITY_ID, status);
    }

    @Override
    public Boolean isServiceDeskProjectsEnabled() {
        return getBooleanValueWithDefault(CAPTURE_JIRA_SERVICEDESK_SETTING_KEY, CAPTURE_SERVICEDESK_ENTITY_ID, false);
    }

    @Override
    public void setServiceDeskProjectsEnabled(final Boolean status) {
        persistBooleanValue(CAPTURE_JIRA_SERVICEDESK_SETTING_KEY, CAPTURE_SERVICEDESK_ENTITY_ID, status);
    }

    @Override
    public Boolean isBusinessProjectsEnabled() {
        return getBooleanValueWithDefault(CAPTURE_JIRA_BUSINESS_SETTING_KEY, CAPTURE_BUSINESS_ENTITY_ID, false);
    }

    @Override
    public void setBusinessProjectsEnabled(final Boolean status) {
        persistBooleanValue(CAPTURE_JIRA_BUSINESS_SETTING_KEY, CAPTURE_BUSINESS_ENTITY_ID, status);
    }

    /**
     * Persist value and updates the request cache.
     *
     * @param settingsKey     the entity key
     * @param entityId        the entity id
     * @param value           the entity value
     */
    private void persistBooleanValue(final String settingsKey, final Long entityId, final Boolean value) {
        RequestCache<String, Boolean> settingsCache = vCacheFactory.getRequestCache(VCACHE_KEY);

        persistenceService.setBoolean(settingsKey, entityId, settingsKey, value);
        settingsCache.remove(settingsKey);
    }

    /**
     * Reads and returns a cached value. Caches the given default value if none exists yet.
     *
     * @param settingsKey     the entity key
     * @param entityId        the entity id
     * @param defaultValue    the value to be set if not exists
     */
    private Boolean getBooleanValueWithDefault(final String settingsKey, final Long entityId, final Boolean defaultValue) {
        RequestCache<String, Boolean> settingsCache = vCacheFactory.getRequestCache(VCACHE_KEY);

        return settingsCache.get(settingsKey, () -> {
            Boolean value = persistenceService.getBoolean(settingsKey, entityId, settingsKey);
            if (value == null) {
                value = defaultValue;
            }
            return value;
        });
    }
}
