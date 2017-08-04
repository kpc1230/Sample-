package com.atlassian.bonfire.customfield.searcher;

import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.TestingStatusService.TestingStatus;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.values.ClauseValuesGenerator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.google.common.collect.Lists;

import java.util.List;

public class TestingStatusClauseValuesGenerator implements ClauseValuesGenerator {
    private final BonfireI18nService i18n;

    public TestingStatusClauseValuesGenerator(BonfireI18nService i18n) {
        this.i18n = i18n;
    }

    /**
     * We will always return the same suggestions every time
     *
     * @deprecated Use {@link TestingStatusClauseValuesGenerator#getPossibleValues(com.atlassian.jira.user.ApplicationUser, String, String, int)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public Results getPossibleValues(User args, String that, String dont, int matter) {
        return getPossibleValues(ApplicationUsers.from(args), that, dont, matter);
    }

    public Results getPossibleValues(ApplicationUser applicationUser, String s, String s1, int i) {
        List<Result> results = Lists.newArrayList();
        results.add(new Result(i18n.getText(TestingStatus.NOT_STARTED.getI18nKey())));
        results.add(new Result(i18n.getText(TestingStatus.IN_PROGRESS.getI18nKey())));
        results.add(new Result(i18n.getText(TestingStatus.COMPLETED.getI18nKey())));
        results.add(new Result(i18n.getText(TestingStatus.INCOMPLETE.getI18nKey())));
        return new Results(results);
    }
}
