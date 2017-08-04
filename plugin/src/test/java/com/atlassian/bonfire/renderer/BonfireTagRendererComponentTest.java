package com.atlassian.bonfire.renderer;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.components.TokenRendererComponent;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junitx.framework.Assert.assertEquals;

public class BonfireTagRendererComponentTest {

    private static final ReverseTagLinker REVERSE_TAG_LINKER = new ReverseTagLinker();
    private static final IdentityTagLinker IDENTITY_TAG_LINKER = new IdentityTagLinker();

    private static final String TAG_RAW_1 = "#ff is a tag as is #ie and questions such as #? and #!";
    private static final String TAG_RAW_EDGE_CASE_1 = "#ff dontmatch#this ok";
    private static final String TAG_RAW_EDGE_CASE_2 = "#If I say #? it says ?#";

    private static final String TAG_RAW_EDGE_CASE_3 = "#start" +
            " This is " +
            "#tag" +
            " and " +
            "\t#tab" +
            "\n#newline\n" +
            "some html likely characters &#234; " +
            "and wiki markup such as [#anchor] " +
            "and {panel:title=My Title| borderStyle=dashed| borderColor=#ccc| titleBGColor=#F7D6C1| bgColor=#FFFFCE} " +
            "and finally a tag at the " +
            "#end";

    private static final String TAG_RAW_EDGE_CASE_3_REVERSED = "trats#" +
            " This is " +
            "gat#" +
            " and " +
            "\tbat#" +
            "\nenilwen#\n" +
            "some html likely characters &#234; " +
            "and wiki markup such as [#anchor] " +
            "and {panel:title=My Title| borderStyle=dashed| borderColor=#ccc| titleBGColor=#F7D6C1| bgColor=#FFFFCE} " +
            "and finally a tag at the " +
            "dne#";

    private RenderContext NOT_BonfireContext;

    @Before
    public void setUp() throws Exception {
        NOT_BonfireContext = new RenderContext();
    }

    private RenderContext newBonfireContext() {
        // the RenderContext we use is stateful.  We turn it off after we have rendered
        // once so we need a  new object for each test, just like in real life
        RenderContext bonfireRenderContext = new RenderContext();
        bonfireRenderContext.addParam("bonfireMode", true);
        return bonfireRenderContext;
    }

    private RenderContext newBonfireExtractorContext() {
        RenderContext bonfireRenderContext = new RenderContext();
        bonfireRenderContext.addParam("bonfireMode", true);
        bonfireRenderContext.addParam("extractedTagSet", new HashSet<String>());
        return bonfireRenderContext;
    }

    @Test
    public void testBasicSubstitution() {
        BonfireTagRendererComponent bonfireTagRC = new BonfireTagRendererComponent(REVERSE_TAG_LINKER);

        RenderContext context = newBonfireContext();
        String output = bonfireTagRC.render(TAG_RAW_1, context);
        output = new TokenRendererComponent(null).render(output, context);
        assertEquals(output, "ff# is a tag as is ei# and questions such as ?# and !#");
    }

    @Test
    public void testEdgeCases() {
        BonfireTagRendererComponent bonfireTagRC = new BonfireTagRendererComponent(REVERSE_TAG_LINKER);

        RenderContext context = newBonfireContext();
        String output = bonfireTagRC.render(TAG_RAW_EDGE_CASE_1, context);
        output = new TokenRendererComponent(null).render(output, context);
        assertEquals(output, "ff# dontmatch#this ok");

        context = newBonfireContext();
        output = bonfireTagRC.render(TAG_RAW_EDGE_CASE_2, context);
        output = new TokenRendererComponent(null).render(output, context);
        assertEquals(output, "fI# I say ?# it says ?#");

        context = newBonfireContext();
        output = bonfireTagRC.render(TAG_RAW_EDGE_CASE_3, context);
        output = new TokenRendererComponent(null).render(output, context);
        assertEquals(output, TAG_RAW_EDGE_CASE_3_REVERSED);
    }

    @Test
    public void testContextAwareness() {
        BonfireTagRendererComponent bonfireTagRC = new BonfireTagRendererComponent(REVERSE_TAG_LINKER);

        String output = bonfireTagRC.render(TAG_RAW_1, NOT_BonfireContext);
        assertEquals(output, TAG_RAW_1);
    }

    @Test
    public void testExtraction() {
        BonfireTagRendererComponent bonfireTagRC = new BonfireTagRendererComponent(IDENTITY_TAG_LINKER);
        RenderContext context = newBonfireExtractorContext();
        String output = bonfireTagRC.render(TAG_RAW_EDGE_CASE_1, context);
        output = new TokenRendererComponent(null).render(output, context);
        assertEquals(output, TAG_RAW_EDGE_CASE_1);
        Set<String> tags = (Set<String>) context.getParam("extractedTagSet");
        assertEquals(1, tags.size());

        context = newBonfireExtractorContext();
        output = bonfireTagRC.render(TAG_RAW_EDGE_CASE_2, context);
        output = new TokenRendererComponent(null).render(output, context);
        assertEquals(output, TAG_RAW_EDGE_CASE_2);
        tags = (Set<String>) context.getParam("extractedTagSet");
        assertEquals(2, tags.size());

        context = newBonfireExtractorContext();
        output = bonfireTagRC.render(TAG_RAW_EDGE_CASE_3, context);
        output = new TokenRendererComponent(null).render(output, context);
        assertEquals(output, TAG_RAW_EDGE_CASE_3);
        tags = (Set<String>) context.getParam("extractedTagSet");
        assertEquals(5, tags.size());
    }
}
