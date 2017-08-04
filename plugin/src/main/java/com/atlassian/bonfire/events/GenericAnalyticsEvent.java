package com.atlassian.bonfire.events;

import com.atlassian.analytics.api.annotations.EventName;

/**
 * Represent Capture for JIRA tracking event
 *
 * @since 2.8.1
 */
public class GenericAnalyticsEvent {
    // Event name
    private final String name;

    // JIRA or extension
    private final String source;

    // User action
    private final String action;

    // dnd (drag-n-drop), clipboard, file_picker
    private final String dataSource;

    // Kept for compatibility reasons to work with Capture for JIRA prior to 2.8.1
    private final String label;

    // whether new issue is created or used existing
    private final Boolean newIssue;

    // number of items related to the event
    // @since 2.9.0
    private final Long count;

    // @since 2.9.0
    private final String browser;

    public GenericAnalyticsEvent(String name, String source, String action, String dataSource, String label, Boolean newIssue, Long count, String browser) {
        this.name = name;
        this.source = source;
        this.action = action;
        this.dataSource = dataSource;
        this.label = label;
        this.newIssue = newIssue;
        this.count = count;
        this.browser = browser;
    }

    public String getSource() {
        return source;
    }

    public String getAction() {
        return action;
    }

    public String getDataSource() {
        return dataSource;
    }

    public Boolean getIssue() {
        return newIssue;
    }

    public Long getCount() {
        return count;
    }

    public String getBrowser() {
        return browser;
    }

    /**
     * Used by JIRA analytics to get proper event name
     */
    @SuppressWarnings("unused")
    @EventName
    public String calculateEventName() {
        return name;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private String name;
        private String source;
        private String action;
        private String dataSource;
        private String label;
        private Boolean newIssue;
        private Long count;
        private String browser;

        public Builder() {

        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        /**
         * Deprecated since Capture for JIRA 2.8.1 since new JSON-like event attributes were added
         * Kept for backward compatibility reasons
         *
         * @param label label
         */
        @Deprecated
        public Builder label(String label) {
            // TODO: check whether there are any reasona
            this.label = label;
            return this;
        }

        public Builder dataSource(String dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder newIssue(Boolean newIssue) {
            this.newIssue = newIssue;
            return this;
        }

        public Builder count(Long count) {
            this.count = count;
            return this;
        }

        public Builder browser(String browser) {
            this.browser = browser;
            return this;
        }

        public GenericAnalyticsEvent build() {
            return new GenericAnalyticsEvent(name, source, action, dataSource, label, newIssue, count, browser);
        }
    }
}
