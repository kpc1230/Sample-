package it.com.atlassian.bonfire.toolbar;

import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since v2.9.5
 */
public class ProjectDeleteHelper {
    private static final Logger log = LoggerFactory.getLogger(ProjectDeleteHelper.class);

    static void deleteProjectSilently(final Backdoor backdoor, final String projectKey) {
        Long projectId = null;
        try {
            projectId = backdoor.project().getProjectId(projectKey);
        } catch (Exception e) {
            // NOOP: project does not exists
        }

        if (projectId == null) {
            log.debug("Project Key = '" + projectKey + "' does not exist.");
            return;
        }

        try {
            backdoor.project().deleteProject(projectKey);
        } catch (Exception e) {
            log.warn("Exception during the project delete. Project Key = '" + projectKey + "'", e);
        }
    }
}
