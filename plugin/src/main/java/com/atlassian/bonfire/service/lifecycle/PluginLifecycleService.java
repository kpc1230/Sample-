package com.atlassian.bonfire.service.lifecycle;

import cloud.atlassian.provisioning.TenantedEventRegistrar;
import cloud.atlassian.provisioning.TenantedPlatformInitialisedEvent;
import cloud.atlassian.provisioning.TenantedPlatformInitialisedListener;
import cloud.atlassian.provisioning.exception.PermanentProvisioningFailureException;
import cloud.atlassian.provisioning.exception.TransientProvisioningFailureException;
import com.atlassian.bonfire.service.BonfireServiceSupport;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.util.AttachmentPathManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.Map;

/**
 * This service will be invoked during plugin install and de-install.  We can then perform tasks that make
 * BonFires life cycle better
 *
 * @since v1.2
 */
@Service(PluginLifecycleService.SERVICE)
public class PluginLifecycleService extends BonfireServiceSupport implements TenantedPlatformInitialisedListener {
    private static final Logger log = Logger.getLogger(PluginLifecycleService.class);

    public static final String SERVICE = "bonfire-LifeCycle";

    @JIRAResource
    private ApplicationProperties jiraApplicationProperties;

    @JIRAResource
    private AttachmentPathManager attachmentPathManager;

    @Autowired
    private TenantedEventRegistrar tenantedEventRegistrar;

    @PostConstruct
    private void registerPlatformInitialisedListener() {
        tenantedEventRegistrar.registerPlatformInitialisedListener(this, 1);
    }

    @PreDestroy
    private void unregisterPlatformInitialisedListener() {
        tenantedEventRegistrar.unregisterPlatformInitialisedListener(this);
    }

    @Override
    protected void onPluginStart() {
    }

    @Nonnull
    @Override
    public Map<String, String> platformInitialised(@Nonnull TenantedPlatformInitialisedEvent tenantedPlatformInitialisedEvent) throws TransientProvisioningFailureException, PermanentProvisioningFailureException {
        addIssueLinkingSupport();
        addAttachmentSupport();
        return Collections.emptyMap();
    }

    private void addIssueLinkingSupport() {
        jiraApplicationProperties.setOption(APKeys.JIRA_OPTION_ISSUELINKING, true);
    }

    private void addAttachmentSupport() {
        boolean attachmentsEnabled = jiraApplicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS);
        if (!attachmentsEnabled) {
            log.warn("Enabling attachments for Bonfire use.");

            attachmentPathManager.setUseDefaultDirectory();
            jiraApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
            jiraApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWTHUMBNAILS, true);
            jiraApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOW_ZIP_SUPPORT, true);
        }
    }

    @Override
    @PreDestroy
    protected void onPluginStop() {
    }

    @Override
    protected void onClearCache() {
    }
}