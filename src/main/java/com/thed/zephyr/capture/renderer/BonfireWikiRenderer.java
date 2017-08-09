package com.thed.zephyr.capture.renderer;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service(BonfireWikiRenderer.SERVICE)
public class BonfireWikiRenderer {
    public static final String SERVICE = "bonfireWikiRenderer";
    public static final String BONFIRE_MODE = "bonfireMode";
    public static final String EXTRACTED_TAG_SET = "extractedTagSet";

    @JIRAResource
    private RendererManager jiraRendererManager;

    public String renderWikiContent(String content, Issue issue) {
        return jiraRendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(content, buildContext(issue));
    }

    public String renderWikiContent(String content) {
        return jiraRendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(content, buildContext(null));
    }

    public Set<String> extractTags(String content) {
        IssueRenderContext context = buildExtractorContext();
        jiraRendererManager.getRendererForType(AtlassianWikiRenderer.RENDERER_TYPE).render(content, context);

        return (Set<String>) context.getParam(EXTRACTED_TAG_SET);
    }

    private IssueRenderContext buildContext(Issue issue) {
        IssueRenderContext issueRenderContext = new IssueRenderContext(issue);
        issueRenderContext.addParam(BONFIRE_MODE, true);
        return issueRenderContext;
    }

    private IssueRenderContext buildExtractorContext() {
        IssueRenderContext renderContext = new IssueRenderContext(null);
        renderContext.addParam(BonfireWikiRenderer.BONFIRE_MODE, true);
        renderContext.addParam(EXTRACTED_TAG_SET, new HashSet<String>());
        return renderContext;
    }
}
