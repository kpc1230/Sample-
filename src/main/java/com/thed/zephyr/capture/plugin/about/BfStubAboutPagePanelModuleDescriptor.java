package com.thed.zephyr.capture.plugin.about;

// stub about page originally sourced from JIRA Agile

import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * Stub module descriptor to keep our "about-page-panel" module type, so UPM dos not complain that modules
 * were not loaded.
 */
public class BfStubAboutPagePanelModuleDescriptor extends AbstractJiraModuleDescriptor<Void> {
    protected BfStubAboutPagePanelModuleDescriptor(final JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory) {
        super(authenticationContext, moduleFactory);
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException {
        super.init(plugin, element);

        // by nulling out the class name, we wont load the class
        this.moduleClassName = null;
    }
}
