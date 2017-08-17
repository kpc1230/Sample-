package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.model.jira.Issue;

/**
 * Created by Masud on 8/13/17.
 */
public interface IssueService {
    Issue getIssueObject(Long issueId);
}
