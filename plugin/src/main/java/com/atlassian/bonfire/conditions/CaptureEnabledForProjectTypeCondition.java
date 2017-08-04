package com.atlassian.bonfire.conditions;

import com.atlassian.bonfire.service.CaptureAdminSettingsService;
import com.atlassian.bonfire.service.ProjectTypeService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Determines whether we should hide Capture link for Service Desk projects or Business projects.
 * <p>
 * The condition is used in JIRA 6.4 and later, for the project-centric navigation.
 * </p>
 *
 * @since v2.9.5
 */
public class CaptureEnabledForProjectTypeCondition implements Condition {
    private static final Logger log = LoggerFactory.getLogger(CaptureEnabledForProjectTypeCondition.class);
    private static final String PROJECT = "project";
    private static final String CAPTURE_SHOW_FOR_OTHERS_PROJECT_TYPES = "capture.show.for.others.project.types";

    @Resource(name = CaptureAdminSettingsService.SERVICE)
    private CaptureAdminSettingsService captureAdminSettingsService;

    @Resource(name = ProjectTypeService.SERVICE)
    private ProjectTypeService projectTypeService;

    @JIRAResource
    private FeatureManager featureManager;

    @Override
    public void init(final Map<String, String> map) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(final Map<String, Object> projectContext) {
        if (!projectTypeService.isProjectTypesSupported()) {
            // pre JIRA 7.0.0: No difference of project types yet, show Capture always
            return true;
        }
        final Project project = (Project) projectContext.get(PROJECT);
        if (project == null) {
            return false;
        }

        if (projectTypeService.isProjectTypeUndefined(project)) {
            log.debug("Capture is enabled for the undefined project type.");
            return true;
        }

        if (!captureAdminSettingsService.isServiceDeskProjectsEnabled()
                && projectTypeService.isServiceDeskProject(project)) {
            log.debug("Capture is disabled for Service Desk project type.");
            return false;
        }

        if (!captureAdminSettingsService.isBusinessProjectsEnabled()
                && projectTypeService.isBusinessProject(project)) {
            log.debug("Capture is disabled for Business project type.");
            return false;
        }

        if (!featureManager.isEnabled(CAPTURE_SHOW_FOR_OTHERS_PROJECT_TYPES)
                && !projectTypeService.isServiceDeskProject(project)
                && !projectTypeService.isBusinessProject(project)
                && !projectTypeService.isSoftwareProject(project)) {
            log.debug("Capture is disabled as project type is unknown and '" + CAPTURE_SHOW_FOR_OTHERS_PROJECT_TYPES + "' flag is unset.");
            return false;
        }
        return true;
    }
}
