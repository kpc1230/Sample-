package com.atlassian.bonfire.service.logging;

import com.atlassian.bonfire.service.BonfireServiceSupport;
import com.atlassian.bonfire.service.BuildPropertiesService;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.jira.config.util.JiraHome;
import org.apache.log4j.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * This gives us log4j support so we can have our very own atlassian-bonfire.log
 *
 * @since v1.1
 */
@Service(LogSupport.SERVICE)
public class LogSupport extends BonfireServiceSupport {
    public static final String SERVICE = "bonfire-LogSupport";

    private static final Logger log = Logger.getLogger(LogSupport.class);
    private static final String ATLASSIAN_BONFIRE = "atlassian-bonfire";
    private static final String MARKER_LINE = "*********************************************************************************\n";

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private static String[] LOGGER_NAMES = {"com.atlassian.bonfire", "com.atlassian.excalibur"};

    @Resource(name = BuildPropertiesService.SERVICE)
    private BuildPropertiesService buildPropertiesService;

    @Resource
    private JiraHome jiraHome;

    @Override
    protected void onPluginStart() {
        Appender bonfireAppender = null;
        for (String loggerName : LOGGER_NAMES) {
            Logger logger = Logger.getLogger(loggerName);

            if (logger.getAppender(ATLASSIAN_BONFIRE) == null) {
                bonfireAppender = bonfireAppender == null ? createBonFireAppender() : bonfireAppender;
                logger.addAppender(bonfireAppender);
            }
        }

        logPluginMessage("plugin started");
    }

    @Override
    protected void onPluginStop() {
        logPluginMessage("plugin stopped");
    }

    @Override
    protected void onClearCache() {
        logPluginMessage("onClearCache() - clearing cache state");
    }

    private Appender createBonFireAppender() {
        try {
            final PatternLayout layout = new PatternLayout("%d %t %p %X{jira.username} %X{jira.request.id} %X{jira.request.assession.id} %X{jira.request.ipaddr} %X{jira.request.url} [%c{4}] %m%n");
            final String fileName = new File(jiraHome.getLogDirectory(), "atlassian-bonfire.log").getAbsolutePath();
            final RollingFileAppender appender = new RollingFileAppender(layout, fileName, true);
            appender.setName("atlassian-bonfire");
            appender.setMaxFileSize("20480KB");
            appender.setMaxBackupIndex(5);

            return appender;

        } catch (IOException ioe) {
            final IllegalStateException illegalStateException = new IllegalStateException("Unable to initialise Bonfire log4j support", ioe);
            log.error(illegalStateException.getMessage());
            throw illegalStateException;
        }
    }

    private void logPluginMessage(final String msg) {
        StringBuilder sb = new StringBuilder().append("Atlassian Bonfire v")
                .append(buildPropertiesService.getVersion())
                .append(" #")
                .append(buildPropertiesService.getBuildNumber())
                .append(" ").append(msg).append("\n");

        log.setLevel(Level.INFO);
        log.info("\n\n" +
                        MARKER_LINE +
                        sb +
                        MARKER_LINE
        );
    }
}
