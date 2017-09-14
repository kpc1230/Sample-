package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by niravshah on 9/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaptureEnvironment implements Serializable {

    @JsonProperty
    private String operatingSystem;

    @JsonProperty
    private String browser;

    @JsonProperty
    private String jQueryVersion;

    @JsonProperty
    private String userAgent;

    @JsonProperty
    private String documentMode;

    @JsonProperty
    private String screenResolution;

    @JsonProperty
    private String url;


    public CaptureEnvironment() {
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getjQueryVersion() {
        return jQueryVersion;
    }

    public void setjQueryVersion(String jQueryVersion) {
        this.jQueryVersion = jQueryVersion;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDocumentMode() {
        return documentMode;
    }

    public void setDocumentMode(String documentMode) {
        this.documentMode = documentMode;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
