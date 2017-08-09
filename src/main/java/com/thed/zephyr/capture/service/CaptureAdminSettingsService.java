package com.thed.zephyr.capture.service;

/**
 * Persists and retrieves admin settings
 *
 * @since v2.9.5
 */
public interface CaptureAdminSettingsService {
    String SERVICE = "capture-CaptureAdminSettingsService";

    Boolean isFeedbackEnabled();

    void setFeedbackEnabled(Boolean status);

    Boolean isServiceDeskProjectsEnabled();

    void setServiceDeskProjectsEnabled(Boolean status);

    Boolean isBusinessProjectsEnabled();

    void setBusinessProjectsEnabled(Boolean status);
}
