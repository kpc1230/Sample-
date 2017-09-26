package com.thed.zephyr.capture.service.jira;


import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;

import java.util.List;

/**
 * Created by Masud on 8/18/17.
 */
public interface IssueTypeService {
    List<IssueType> getIssueTypes();
    IssueType getIssueType(Long id);
    public List<IssuelinksType> getIssuelinksType();
    List<IssueType> getIssueTypesByProject(Long projectId);
}
