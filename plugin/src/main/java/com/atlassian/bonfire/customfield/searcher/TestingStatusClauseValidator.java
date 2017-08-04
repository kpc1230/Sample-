package com.atlassian.bonfire.customfield.searcher;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

import javax.annotation.Nonnull;

public class TestingStatusClauseValidator implements ClauseValidator {
    private SupportedOperatorsValidator supportedOperatorsValidator;

    public TestingStatusClauseValidator() {
        supportedOperatorsValidator = getSupportedOperatorsValidator();
    }

    /**
     * Validate the provided terminal clause.
     *
     * @return a message set containing validation errors. An empty set is returned if no errors are found (as required by JIRA, null is not allowed)
     * @deprecated Use {@link TestingStatusClauseValidator#validate(com.atlassian.jira.user.ApplicationUser, com.atlassian.query.clause.TerminalClause)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public MessageSet validate(User searcher, TerminalClause terminalClause) {
        return validate(ApplicationUsers.from(searcher), terminalClause);
    }

    @SuppressWarnings("unchecked")
    protected SupportedOperatorsValidator getSupportedOperatorsValidator() {
        return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS);
    }

    @Nonnull
    public MessageSet validate(ApplicationUser searcher, TerminalClause terminalClause) {
        // ensure the operator is supported
        MessageSet errors = supportedOperatorsValidator.validate(searcher, terminalClause);

        return errors;
    }
}
