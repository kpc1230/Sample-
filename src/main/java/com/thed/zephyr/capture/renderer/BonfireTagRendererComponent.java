package com.thed.zephyr.capture.renderer;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.AbstractRegexRendererComponent;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A render component that turns #tags into links to those tags
 *
 * @since 1.4
 */
public class BonfireTagRendererComponent extends AbstractRegexRendererComponent {
    private static final String START_OR_WHITEPSACE = "(^|\\s|>|\\|)";
    private static final String HASH = "#";
    private static final String WORD_CHARACTER = "[\\w\\?\\!\\-]+";
    private static final Pattern TAG_PATTERN = Pattern.compile(START_OR_WHITEPSACE + "(" + HASH + WORD_CHARACTER + ")");

    private TagLinker tagLinker;

    public BonfireTagRendererComponent(final TagLinker tagLinker) {
        this.tagLinker = tagLinker;
    }

    public boolean shouldRender(RenderMode renderMode) {
        return renderMode.renderLinks();
    }

    public String render(String wiki, RenderContext context) {
        if (inBonfireMode(context)) {
            if (wiki.indexOf('#') != -1) {
                wiki = regexRender(wiki, context, TAG_PATTERN);
            }
        }
        return wiki;
    }

    /**
     * To ensure we only run on Bonfire code and not affect the JIRA wiki rendering
     */
    private boolean inBonfireMode(RenderContext context) {
        Object param = context.getParam(BonfireWikiRenderer.BONFIRE_MODE);
        return Boolean.parseBoolean(String.valueOf(param));
    }

    /**
     * Adds a tag to the tag list within the context, if a tag list exists
     */
    private void extractTag(RenderContext context, String tag) {
        Object param = context.getParam(BonfireWikiRenderer.EXTRACTED_TAG_SET);
        if (param != null) {
            ((Set<String>) param).add(tag);
        }
    }

    public void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher) {
        String precendingWhitespace = matcher.group(1);
        String tag = matcher.group(2);

        // Add the tag to the tag list
        extractTag(context, tag);

        // Replace the tag with a token
        buffer.append(precendingWhitespace)
                .append(context.getRenderedContentStore().addInline(tagLinker.createDecorator(tag)));
    }
}
