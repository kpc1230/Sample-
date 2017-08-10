package com.thed.zephyr.capture.renderer;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.renderer.v2.components.PluggableRendererComponentFactory;
import com.atlassian.renderer.v2.components.RendererComponent;

/**
 * A renderer factory that substitutes #tags with links to that text
 */
public class BonfireRendererComponentFactory implements PluggableRendererComponentFactory {
    private RendererComponent component;

    public void init(ModuleDescriptor moduleDescriptor) throws PluginParseException {
        component = new BonfireTagRendererComponent(new BonfireTagLinker());
    }

    public RendererComponent getRendererComponent() {
        return component;
    }
}
