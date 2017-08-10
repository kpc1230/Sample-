package com.thed.zephyr.capture.web.filters;

import com.atlassian.excalibur.web.util.VersionKit;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.servlet.*;
import java.io.IOException;

public class BonfireLegacyResourceFilter implements Filter {
    private final WebResourceManager webResourceManager;
    private final BuildUtilsInfo buildUtilsInfo;

    public BonfireLegacyResourceFilter(WebResourceManager webResourceManager, BuildUtilsInfo buildUtilsInfo) {
        this.webResourceManager = webResourceManager;
        this.buildUtilsInfo = buildUtilsInfo;
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        includeVersionSpecificResources();

        chain.doFilter(request, response);
    }

    private void includeVersionSpecificResources() {
        VersionKit.SoftwareVersion five1 = VersionKit.version(5, 1);
        VersionKit.SoftwareVersion five2 = VersionKit.version(5, 2);
        VersionKit.SoftwareVersion six0 = VersionKit.version(6, 0);
        VersionKit.SoftwareVersion jiraVersion = VersionKit.parse(buildUtilsInfo.getVersion());
        if (jiraVersion.isLessThan(six0)) {
            webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-legacy-five-two");
        }
        if (jiraVersion.isLessThan(five2)) {
            webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-legacy-five-one");
        }
        if (jiraVersion.isLessThan(five1)) {
            webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-legacy-five-zero");
        }
    }
}
