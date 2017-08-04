package com.atlassian.excalibur.tabpanels;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import com.google.common.base.Preconditions;
import com.opensymphony.util.TextUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Context provider for project-centric navigation
 *
 * @since v2.9
 */
public class CaptureTestSessionsContextProvider implements ContextProvider {
    private static final String XSRF_TOKEN = "xsrfToken";

    @Resource(name = ProjectTestSessionViewHelper.SERVICE)
    private ProjectTestSessionViewHelper projectTestSessionViewHelper;

    @JIRAResource
    private JiraAuthenticationContext jiraAuthenticationContext;

    @JIRAResource
    private VelocityRequestContextFactory velocityRequestContextFactory;

    @Override
    public void init(Map<String, String> stringStringMap) throws PluginParseException {

    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> context) {
        final HttpServletRequest req = ExecutingHttpRequest.get();

        Project project = (Project) Preconditions.checkNotNull(context.get("project"));
        final VelocityRequestContext velocityContext = velocityRequestContextFactory.getJiraVelocityRequestContext();

        final Map<String, Object> params = projectTestSessionViewHelper.getNavigatorVelocityParams(jiraAuthenticationContext.getUser(), project, req);

        // ContextProvider does not inject it for us:
        params.put("requestContext", velocityContext);
        params.put("textutils", new TextUtils());
        params.put("req", req);
        params.put("project", project);
        params.put(XSRF_TOKEN, new JiraWebActionSupport().getXsrfToken());
        return params;
    }
}
