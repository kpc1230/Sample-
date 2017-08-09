package com.thed.zephyr.capture.service;

import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;
import com.atlassian.plugin.util.ClassLoaderUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Stores information about this build of Bonfire.
 */
@Service(BuildPropertiesService.SERVICE)
public class BuildPropertiesServiceImpl implements BuildPropertiesService {

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private final Properties buildProperties;


    public BuildPropertiesServiceImpl() {
        buildProperties = readBuildProperties();
    }

    private Properties readBuildProperties() {
        InputStream resourceAsStream = ClassLoaderUtils.getResourceAsStream("build/build.properties", getClass());
        if (resourceAsStream == null) {
            throw new IllegalStateException("Unable to find Bonfire build.properties");
        }
        final Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load Bonfire build.properties");
        }
    }

    public String getVersion() {
        return buildProperties.getProperty("build.version");
    }

    public String getBuildNumber() {
        return buildProperties.getProperty("build.number");
    }

    public DateTime getBuildDate() {
        return new DateTime(buildProperties.getProperty("build.time"));
    }

    public String getVersionDirectory() {
        return buildProperties.getProperty("extension.version.directory");
    }
}
