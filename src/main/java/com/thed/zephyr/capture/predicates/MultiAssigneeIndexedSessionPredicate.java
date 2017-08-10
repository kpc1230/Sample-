package com.thed.zephyr.capture.predicates;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.jira.usercompatibility.UserCompatibilityHelper;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class MultiAssigneeIndexedSessionPredicate implements Predicate<IndexedSession> {
    private List<String> userNames;

    public MultiAssigneeIndexedSessionPredicate(List<String> userNames) {
        this.userNames = Lists.newArrayList();
        for (String s : userNames) {
            if (StringUtils.isNotBlank(s)) {
                this.userNames.add(s);
            }
        }
    }

    @Override
    public boolean apply(IndexedSession input) {
        User assignee = UserCompatibilityHelper.getUserForKey(input.getAssignee());
        for (String s : userNames) {
            if (assignee != null && s.equals(assignee.getName())) {
                return true;
            }
        }
        return false;
    }
}
