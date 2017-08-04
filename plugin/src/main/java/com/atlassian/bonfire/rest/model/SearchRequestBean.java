package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB bean for search requests.
 *
 * @since v1.1
 */
@XmlRootElement
public class SearchRequestBean {
    /**
     * A JQL query string.
     */
    public String jql;

    /**
     * The index of the first issue to return (0-based).
     */
    public Integer startAt;

    /**
     * The maximum number of issues to return (defaults to 50). The maximum allowable value is dictated by the JIRA
     * property 'jira.search.views.default.max'. If you specify a value that is higher than this number, your search
     * results will be truncated.
     */
    public Integer maxResults;

    public SearchRequestBean() {
    }

    public SearchRequestBean(String jql, Integer startAt, Integer maxResults) {
        this.jql = jql;
        this.startAt = startAt;
        this.maxResults = maxResults;
    }

    public String getJql() {
        return jql;
    }

    public Integer getStartAt() {
        return startAt;
    }

    public Integer getMaxResults() {
        return maxResults;
    }
}
