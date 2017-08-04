package com.atlassian.excalibur.service;

import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.excalibur.web.util.VersionKit;
import com.atlassian.jira.user.ApplicationUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service(BonfireUserSettingsService.SERVICE)
public class BonfireUserSettingsService {
    public static final String SERVICE = "bonfire-bonfireUserSettingsService";

    private static final String BONFIRE_ENTITY_NAME = "Bonfire.properties";
    private static final long GLOBAL_ENTITY_ID = 1l;

    private static final String EXTENSION_CALLOUT_KEY = "bonfire.extension.callout";
    private static final String ANALYTICS_CALLOUT_KEY = "bonfire.analytics.callout";
    private static final String JIRA_ANALYTICS_CALLOUT_KEY = "bonfire.jira.analytics.callout";

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    /**
     * Prior to Capture for JIRA 2.8 there were a separate analytics dialog
     *
     * @param user
     * @return
     */
    private String getOldAnalyticsCalloutKey(User user) {
        return user.getName() + ANALYTICS_CALLOUT_KEY;
    }

    /**
     * Get Bonfire Callout
     **/

    public boolean showExtensionCallout(ApplicationUser user) {
        if (user == null) {
            return true;
        }
        // Get the value that's saved
        String toReturn = persistenceService.getString(BONFIRE_ENTITY_NAME, GLOBAL_ENTITY_ID, getExtensionCalloutKey(user));
        boolean doShow = !"shown".equals(toReturn);
        if (doShow) {
            // If it's false, then mark as shown - we only show this once per user
            persistenceService.setString(BONFIRE_ENTITY_NAME, GLOBAL_ENTITY_ID, getExtensionCalloutKey(user), "shown");
        }
        return doShow;
    }

    private String getExtensionCalloutKey(ApplicationUser user) {
        String userKey = user.getKey();
        return userKey + EXTENSION_CALLOUT_KEY;
    }

    private String getAnalyticsCalloutKey(ApplicationUser user) {
        String userKey = user.getKey();
        return userKey + ANALYTICS_CALLOUT_KEY;
    }

    /**
     * Using JIRA analytics settings
     *
     * @param user user
     * @return property
     */
    private String getJiraAnalyticsCalloutKey(ApplicationUser user) {
        String userKey = user.getKey();
        return userKey + JIRA_ANALYTICS_CALLOUT_KEY;
    }
}
