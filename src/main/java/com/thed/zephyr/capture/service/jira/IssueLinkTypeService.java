package com.thed.zephyr.capture.service.jira;


import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;

import java.util.List;

public interface IssueLinkTypeService {
    public List<IssuelinksType> getIssuelinksType(AtlassianHostUser hostUser);
    IssuelinksType createIssuelinkType(IssuelinksType issuelinksType, AtlassianHostUser hostUser);
}
