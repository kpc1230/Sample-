package com.atlassian.bonfire.renderer;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;
import org.apache.commons.lang.StringUtils;

class ReverseTagLinker implements TagLinker {
    @Override
    public Renderable createDecorator(String tag) {
        return new ReverseTagDecorator(tag);
    }

    public static class ReverseTagDecorator implements Renderable {
        private String tag;

        public ReverseTagDecorator(String tag) {
            this.tag = tag;
        }

        @Override
        public void render(SubRenderer subRenderer, RenderContext renderContext, StringBuffer stringBuffer) {
            stringBuffer.append(StringUtils.reverse(tag));
        }
    }
}
