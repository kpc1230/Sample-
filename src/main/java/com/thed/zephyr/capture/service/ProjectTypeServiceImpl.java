package com.thed.zephyr.capture.service;

import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.web.util.VersionKit;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.BuildUtilsInfo;
import org.springframework.stereotype.Service;

/**
 * @since v2.9.5
 */
@Service(ProjectTypeService.SERVICE)
public class ProjectTypeServiceImpl implements ProjectTypeService {

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private static final VersionKit.SoftwareVersion JIRA_7_0 = VersionKit.version(7, 0);

    private static final String JIRA_SERVICEDESK_PROJECT_TYPE = "service_desk";
    private static final String JIRA_BUSINESS_PROJECT_TYPE = "business";
    private static final String JIRA_SOFTWARE_PROJECT_TYPE = "software";

    @JIRAResource
    private BuildUtilsInfo buildUtilsInfo;

    @Override
    public boolean isProjectTypesSupported() {
        VersionKit.SoftwareVersion version = VersionKit.version(buildUtilsInfo);
        return version.isGreaterThanOrEqualTo(JIRA_7_0);
    }

    @Override
    public boolean isServiceDeskProject(final Project project) {
        return JIRA_SERVICEDESK_PROJECT_TYPE.equals(project.getProjectTypeKey().getKey());
    }

    @Override
    public boolean isBusinessProject(final Project project) {
        return JIRA_BUSINESS_PROJECT_TYPE.equals(project.getProjectTypeKey().getKey());
    }

    @Override
    public boolean isSoftwareProject(final Project project) {
        return JIRA_SOFTWARE_PROJECT_TYPE.equals(project.getProjectTypeKey().getKey());
    }

    @Override
    public boolean isProjectTypeUndefined(final Project project) {
        return project.getProjectTypeKey().getKey() == null;
    }
}
