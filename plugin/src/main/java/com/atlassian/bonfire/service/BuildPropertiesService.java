package com.atlassian.bonfire.service;

import org.joda.time.DateTime;

/**
 * Provides access to Bonfire build properties
 */
public interface BuildPropertiesService {

    public static final String SERVICE = "bonfire-BuildPropertiesService";

    /**
     * Get the version of this build
     */
    public String getVersion();

    /**
     * Get the number of this build
     */
    public String getBuildNumber();

    /**
     * Get the version directory of this build
     */
    public String getVersionDirectory();

    /**
     * Get the build date
     */
    public DateTime getBuildDate();

}
