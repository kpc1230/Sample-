package com.thed.zephyr.capture.customfield.jql;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.JiraDataTypeImpl;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.plugin.jql.function.AbstractJqlFunction;
import com.atlassian.jira.plugin.jql.function.JqlFunction;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.jira.util.NotNull;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * A JQL Function which returns the test session ID for the current user's active session - if one exists.
 *
 * @since v3.0
 */
public class MyActiveSessionJqlFunction extends AbstractJqlFunction implements JqlFunction {
    public static final String NAME = "myActiveSession";

    private final SessionController sessionController;

    public MyActiveSessionJqlFunction(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    /**
     * @deprecated Use {@link MyActiveSessionJqlFunction#validate(com.atlassian.jira.user.ApplicationUser, com.atlassian.query.operand.FunctionOperand, com.atlassian.query.clause.TerminalClause)} instead. Since JIRA v7.0.0.
     */
    @Deprecated
    public MessageSet validate(User user, @NotNull FunctionOperand functionOperand, @NotNull TerminalClause terminalClause) {
        return this.validate(ApplicationUsers.from(user), functionOperand, terminalClause);
    }

    @Nonnull
    public MessageSet validate(ApplicationUser applicationUser, FunctionOperand functionOperand, TerminalClause terminalClause) {
        MessageSet messageSet = new MessageSetImpl();
        return messageSet;
    }

    @Override
    public List<QueryLiteral> getValues(@NotNull QueryCreationContext queryCreationContext, @NotNull FunctionOperand functionOperand, @NotNull TerminalClause terminalClause) {
        SessionController.SessionResult activeSession = sessionController.getActiveSession(queryCreationContext.getApplicationUser());
        if (activeSession.isValid()) {
            Session session = activeSession.getSession();
            if (session != null) {
                return Collections.singletonList(new QueryLiteral(functionOperand, session.getId().toString()));
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public int getMinimumNumberOfExpectedArguments() {
        return 0;
    }

    @Override
    public String getFunctionName() {
        return NAME;
    }

    @Override
    public JiraDataType getDataType() {
        return new JiraDataTypeImpl(String.class);
    }
}
