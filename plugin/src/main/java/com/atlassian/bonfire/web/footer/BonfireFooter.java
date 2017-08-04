package com.atlassian.bonfire.web.footer;

import com.atlassian.bonfire.service.BuildPropertiesService;
import com.atlassian.jira.plugin.navigation.FooterModuleDescriptor;
import com.atlassian.jira.plugin.navigation.PluggableFooter;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.google.common.collect.Maps;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Pattern;

public class BonfireFooter implements PluggableFooter {
    @Resource(name = BuildPropertiesService.SERVICE)
    private BuildPropertiesService buildPropertiesService;

    private FooterModuleDescriptor descriptor;

    @Override
    public void init(FooterModuleDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getFullFooterHtml(HttpServletRequest request) {
        return descriptor.getFooterHtml(request, buildParameters(false));
    }

    @Override
    public String getSmallFooterHtml(HttpServletRequest request) {
        return descriptor.getFooterHtml(request, buildParameters(true));
    }

    private Map<String, Object> buildParameters(final boolean smallFooter) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("showFooter", showFooter());
        parameters.put("bonfireVersion", buildPropertiesService.getVersion());
        if (smallFooter) {
            parameters.put("smallFooter", Boolean.TRUE);
        }
        return parameters;
    }

    Pattern URLS = Pattern.compile("secure/(" +
            "BonfireGettingStarted.jspa" +
            "|GetBonfire.jspa" +
            "|SessionNavigator.jspa" +
            "|ViewSession.jspa" +
            ")");

    private boolean showFooter() {
        HttpServletRequest httpServletRequest = ExecutingHttpRequest.get();
        return URLS.matcher(httpServletRequest.getRequestURL().toString()).find();
    }
}
