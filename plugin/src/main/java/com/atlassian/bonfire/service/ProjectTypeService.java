package com.atlassian.bonfire.service;

import com.atlassian.jira.project.Project;

/**
 * Adds support for Project types that are introduced in JIRA 7.0.
 *
 * @since v2.9.5
 */
public interface ProjectTypeService {
    String SERVICE = "capture-ProjectTypeService";

    boolean isProjectTypesSupported();

    boolean isServiceDeskProject(final Project project);

    boolean isBusinessProject(final Project project);

    boolean isSoftwareProject(Project project);

    boolean isProjectTypeUndefined(Project project);
}
