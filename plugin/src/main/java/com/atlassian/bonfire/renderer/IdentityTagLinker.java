package com.atlassian.bonfire.renderer;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;

/**
 * Returns the
 */
class IdentityTagLinker implements TagLinker {

    @Override
    public Renderable createDecorator(String tag) {
        return new IdentityTagDecorator(tag);
    }

    public static class IdentityTagDecorator implements Renderable {
        private String tag;

        public IdentityTagDecorator(String tag) {
            this.tag = tag;
        }

        @Override
        public void render(SubRenderer subRenderer, RenderContext renderContext, StringBuffer stringBuffer) {
            stringBuffer.append(tag);
        }
    }
}
