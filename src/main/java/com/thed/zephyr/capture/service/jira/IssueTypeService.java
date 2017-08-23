package com.thed.zephyr.capture.service.jira;

import com.thed.zephyr.capture.model.jira.IssueType;

import java.util.List;

/**
 * Created by Masud on 8/18/17.
 */
public interface IssueTypeService {
    List<IssueType> getIssueTypes();
    IssueType getIssueType(Long id);
    List<IssueType> getIssueTypesByProject(Long projectId);
}
