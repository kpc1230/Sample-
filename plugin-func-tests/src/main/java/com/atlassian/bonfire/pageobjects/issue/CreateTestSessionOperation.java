package com.atlassian.bonfire.pageobjects.issue;

import com.atlassian.jira.pageobjects.model.IssueOperation;


public class CreateTestSessionOperation implements IssueOperation {
    @Override
    public String id() {
        return "create-test-session";
    }

    @Override
    public String uiName() {
        return "Create test session";
    }

    @Override
    public String cssClass() {
        return "issueaction-create-test-session";
    }

    @Override
    public boolean hasShortcut() {
        return false;
    }

    @Override
    public CharSequence shortcut() {
        return null;
    }
}
