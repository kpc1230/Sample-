package com.atlassian.bonfire.rest.model.request;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SettingsAllRequest {
    @JsonProperty
    private Boolean analytics;

    @JsonProperty
    private Boolean feedback;

    @JsonProperty
    private Boolean serviceDeskProjectsEnabled;

    @JsonProperty
    private Boolean businessProjectsEnabled;

    public SettingsAllRequest() {
    }

    public Boolean getAnalytics() {
        return analytics;
    }

    public void setAnalytics(Boolean analytics) {
        this.analytics = analytics;
    }

    public Boolean getFeedback() {
        return feedback;
    }

    public void setFeedback(Boolean feedback) {
        this.feedback = feedback;
    }

    public Boolean getServiceDeskProjectsEnabled() {
        return serviceDeskProjectsEnabled;
    }

    public void setServiceDeskProjectsEnabled(final Boolean serviceDeskProjectsEnabled) {
        this.serviceDeskProjectsEnabled = serviceDeskProjectsEnabled;
    }

    public Boolean getBusinessProjectsEnabled() {
        return businessProjectsEnabled;
    }

    public void setBusinessProjectsEnabled(final Boolean businessProjectsEnabled) {
        this.businessProjectsEnabled = businessProjectsEnabled;
    }
}
