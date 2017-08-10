package com.thed.zephyr.capture.rest.model;

import com.thed.zephyr.capture.service.BonfireLicenseService;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Internal references to 'licence' are spelled the Australian way, external ones are spelled the American way. This is for compat reasons
 * </p>
 * TODO: Seriously questioning the value of using the builder pattern here
 */
@XmlRootElement
public class PluginDetailsBean {
    @XmlElement
    private String version;

    @XmlElement
    private String buildDate;

    @XmlElement
    private String buildNumber;

    @XmlElement
    private String licenceStatus;

    @XmlElement
    private String jiraVersion;

    @XmlElement
    private String jiraBuildInfo;

    @XmlElement
    private boolean sendAnalytics;

    public PluginDetailsBean() {
    }

    public String getVersion() {
        return version;
    }

    public String getLicenceStatus() {
        return licenceStatus;
    }

    public String getJiraVersion() {
        return jiraVersion;
    }

    public String getJiraBuildInfo() {
        return jiraBuildInfo;
    }


    public static class Builder {
        private String version;
        private String buildDate;
        private String buildNumber;
        private String jiraVersion;
        private String licenceStatus;
        private String jiraBuildInfo;
        private boolean sendAnalytics;

        public Builder setVersion(final String version) {
            this.version = version;
            return this;
        }

        public Builder setBuildDate(final DateTime buildDate) {
            this.buildDate = buildDate.toString(ISODateTimeFormat.dateTime());
            return this;
        }

        public Builder setBuildNumber(final String buildNumber) {
            this.buildNumber = buildNumber;
            return this;
        }

        public Builder setJiraVersion(final String jiraVersion) {
            this.jiraVersion = jiraVersion;
            return this;
        }

        public Builder setJiraBuildInfo(final String jiraBuildInfo) {
            this.jiraBuildInfo = jiraBuildInfo;
            return this;
        }

        public Builder setLicenseStatus(final BonfireLicenseService.Status licenseStatus) {
            this.licenceStatus = licenseStatus.name();
            return this;
        }

        @Deprecated
        /**
         * @deprecated: BONDEV-123
         */
        public Builder setSendAnalytics(final boolean sendAnalytics) {
            this.sendAnalytics = sendAnalytics;
            return this;
        }

        public PluginDetailsBean build() {
            PluginDetailsBean bean = new PluginDetailsBean();
            bean.version = this.version;
            bean.buildDate = this.buildDate;
            bean.buildNumber = this.buildNumber;
            bean.licenceStatus = this.licenceStatus;
            bean.jiraVersion = this.jiraVersion;
            bean.jiraBuildInfo = this.jiraBuildInfo;
            bean.sendAnalytics = this.sendAnalytics;
            return bean;
        }

    }

}

