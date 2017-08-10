package com.thed.zephyr.capture.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * JAXB bean for returning search results.
 *
 * @since v1.1
 */
@XmlRootElement
public class SearchResultsBean {

    @XmlElement
    public Integer startAt;

    @XmlElement
    public Integer maxResults;

    @XmlElement
    public Integer total;

    @XmlElement
    public List<IssueBean> issues;

    public SearchResultsBean() {
    }

    public SearchResultsBean(Integer startAt, Integer maxResults, Integer total, List<IssueBean> issues) {
        this.startAt = startAt;
        this.maxResults = maxResults;
        this.total = total;
        this.issues = issues;
    }

    public int getStartAt() {
        return startAt;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public int getTotal() {
        return total;
    }

    public List<IssueBean> getIssues() {
        return issues;
    }
}
