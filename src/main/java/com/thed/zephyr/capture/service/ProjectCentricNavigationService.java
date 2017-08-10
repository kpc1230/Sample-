package com.thed.zephyr.capture.service;

/**
 * Checks whether project-centric navigation is enabled
 * This new experience was introduced by <strong>jira-projects</strong> plug-in since JIRA 6.4
 *
 * @since v2.9
 */
public interface ProjectCentricNavigationService {
    public static final String SERVICE = "capture-ProjectCentricNavigationService";

    boolean isProjectCentricNavigationEnabled();
}
