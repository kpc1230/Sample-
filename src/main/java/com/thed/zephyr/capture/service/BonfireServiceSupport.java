package com.thed.zephyr.capture.service;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

/**
 * A base class that other services can derive from and hence get better plugin life cycle methods
 * and atlassian-event support.
 *
 * @since v1.4
 */
public abstract class BonfireServiceSupport implements InitializingBean, DisposableBean {
    @JIRAResource
    private EventPublisher eventPublisher;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService bonfireI18nService;


    /**
     * Made final to allow better template naming
     *
     * @throws Exception because it says it should
     */
    public final void afterPropertiesSet() throws Exception {
        eventPublisher.register(this);
        onPluginStart();
    }

    /**
     * Made final to allow better template naming
     *
     * @throws Exception because it says it should
     */
    public final void destroy() throws Exception {
        try {
            onPluginStop();
        } finally {
            eventPublisher.unregister(this);
        }
    }

    /**
     * <p>
     * Made final to allow better template naming.  Called when JIRA restores data but does NOT re-start the plugin system
     * </p>
     * <p>
     * This is done as an optimisation for functional testing but it can trip you up if you are retaining state.
     * </p>
     *
     * @param clearCacheEvent empty event which is not of much use to propagate.
     */
    @EventListener
    public final void onClearCache(final ClearCacheEvent clearCacheEvent) {
        onClearCache();
    }

    /**
     * <p>
     * Allows you want to do start up stuff like initialise caches.  At this point the service has registered itself with the
     * plugin system and the plugin is operational.
     * </p>
     * <p>
     * Doing nothing is an appropriate answer here but we want you to make that conscious decision.
     * </p>
     */
    protected abstract void onPluginStart();

    /**
     * <p>
     * Called when the plugin is being taken down.  You should clean up quickly.  Don't do anything long running here.
     * </p>
     * <p>
     * Doing nothing is an appropriate answer here but we want you to make that conscious decision.
     * </p>
     */
    protected abstract void onPluginStop();

    /**
     * Everyone MUST handle the {@link ClearCacheEvent}.  Doing nothing is an appropriate answer here but we want you to make that conscious decision.
     */
    protected abstract void onClearCache();

    protected EventPublisher getEventPublisher() {
        return eventPublisher;
    }

    protected BonfireI18nService getBonfireI18nService() {
        return bonfireI18nService;
    }

    /**
     * Gets I18n text
     *
     * @param key    the message key
     * @param params the parameters to use
     * @return the I18n text
     * @see BonfireI18nService#getText(String, Object...)
     */
    protected String getText(String key, Object... params) {
        return bonfireI18nService.getText(key, params);
    }
}
