package com.thed.zephyr.capture.renderer;

import com.atlassian.excalibur.model.Tag;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;

public class BonfireTagLinker implements TagLinker {
    @Override
    public Renderable createDecorator(String tag) {
        return new BonfireTagDecorator(tag);
    }

    public static class BonfireTagDecorator implements Renderable {
        private String tag;

        public BonfireTagDecorator(String tag) {
            this.tag = tag;
        }

        @Override
        public void render(SubRenderer subRenderer, RenderContext renderContext, StringBuffer stringBuffer) {
            tag = tag.toLowerCase();
            String cssClass = "tag-unknown";
            if (Tag.ASSUMPTION.equals(tag)) {
                cssClass = "tag-assumption";
            }
            if (Tag.FOLLOWUP.equals(tag)) {
                cssClass = "tag-followUp";
            }
            if (Tag.IDEA.equals(tag)) {
                cssClass = "tag-idea";
            }
            if (Tag.QUESTION.equals(tag)) {
                cssClass = "tag-question";
            }
            boolean tagIsUnknown = cssClass.equals("tag-unknown");

            stringBuffer.append("<span class=\"note-tag ").append(cssClass).append("\">").append(tagIsUnknown ? tag : "").append("</span>");
        }
    }
}
