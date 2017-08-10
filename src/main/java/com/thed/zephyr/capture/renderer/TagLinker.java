package com.thed.zephyr.capture.renderer;

import com.atlassian.renderer.v2.Renderable;

/**
 * This will tag a raw tag and ask for a replacement wiki link for that tag
 */
public interface TagLinker {
    /**
     * Given a tag in the form #xxxx this should a decorator for rendering the tag
     *
     * @param tag the raw tag including the # at the start
     * @return the decorator that will render the tag
     */
    Renderable createDecorator(String tag);
}
