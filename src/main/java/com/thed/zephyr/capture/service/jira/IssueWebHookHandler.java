package com.thed.zephyr.capture.service.jira;

import com.thed.zephyr.capture.model.AcHostModel;

public interface IssueWebHookHandler {

    @Deprecated
    void issueDeleteEventHandler(AcHostModel acHostModel, Long issueId);
}
