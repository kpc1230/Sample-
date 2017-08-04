package com.atlassian.bonfire.features;

import com.atlassian.featureflag.client.api.FeatureFlag.BooleanFlag;

/**
 * Dark features which can be controlled by the LaunchDarkly UI.
 * See https://extranet.atlassian.com/display/JCC/JIRA+Feature+Flag+Service
 */
public enum CaptureFeatureFlags {
    TESTING_STATUS_DB_PRIMARY("capture.testing.status.db.primary", false),
    TESTING_STATUS_UPDATE_IN_DB("capture.testing.status.update.db", false),
    TESTING_STATUS_INDEXER_READ_FROM_DB("capture.testing.status.indexer.read.from.db", false);


    private final BooleanFlag flag;

    CaptureFeatureFlags(String key, boolean defaultValue) {
        flag = new BooleanFlag(key, defaultValue);
    }

    public BooleanFlag asFlag() {
        return flag;
    }
}
