package com.atlassian.bonfire.service;

import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.web.util.VersionKit;
import com.atlassian.jira.util.BuildUtilsInfo;
import org.springframework.stereotype.Service;

/**
 * @since v2.9
 */
@Service(ProjectCentricNavigationService.SERVICE)
public class ProjectCentricNavigationServiceImpl implements ProjectCentricNavigationService {

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private static final VersionKit.SoftwareVersion JIRA_6_4 = VersionKit.version(6, 4);

    @JIRAResource
    private BuildUtilsInfo buildUtilsInfo;

    @Override
    public boolean isProjectCentricNavigationEnabled() {
        // Currently do not respect the dark feature flags, only the JIRA version
        VersionKit.SoftwareVersion version = VersionKit.version(buildUtilsInfo);
        return version.isGreaterThanOrEqualTo(JIRA_6_4);
    }
}
