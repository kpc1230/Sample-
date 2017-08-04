package com.atlassian.bonfire.service.controller;

import com.atlassian.bonfire.model.Variable;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONObject;

/**
 * Controller for storing and retrieving variables
 *
 * @since v1.8
 */
public interface VariableController {
    public static final String SERVICE = "bonfire-variableController";

    public CreateResult validateCreate(ApplicationUser creator, JSONObject variable);

    public VariableResult create(CreateResult result);

    public UpdateResult validateUpdate(ApplicationUser updater, Variable updatedVariable);

    public VariableResult update(UpdateResult result);

    public DeleteResult validateDelete(ApplicationUser deleter, Variable variable);

    public VariableResult delete(DeleteResult result);

    public Iterable<Variable> loadVariables(ApplicationUser user);

    public Iterable<Variable> loadVariablesForTemplate(ApplicationUser creator, JSONObject source);

    public Iterable<Variable> loadVariablesForAdmin();

    public static class CreateResult extends VariableResult {
        public CreateResult(ErrorCollection errorCollection, Variable variable, ApplicationUser user) {
            super(errorCollection, variable, user);
        }
    }

    public static class UpdateResult extends VariableResult {
        public UpdateResult(ErrorCollection errorCollection, Variable variable, ApplicationUser user) {
            super(errorCollection, variable, user);
        }
    }

    public static class DeleteResult extends VariableResult {
        public DeleteResult(ErrorCollection errorCollection, Variable variable, ApplicationUser user) {
            super(errorCollection, variable, user);
        }
    }

    public static class VariableResult extends ServiceOutcomeImpl<Variable> {
        private final ApplicationUser user;

        public VariableResult(ErrorCollection errorCollection, Variable variable, ApplicationUser user) {
            super(errorCollection, variable);
            this.user = user;
        }

        public ApplicationUser getUser() {
            return user;
        }

        /**
         * Convenience method that returns a new ServiceOutcomeImpl instance containing no errors, and with the provided
         * returned value.
         *
         * @param returnedValue the returned value
         * @return a new ServiceOutcomeImpl
         */
        public static VariableResult ok(Variable returnedValue, ApplicationUser user) {
            return new VariableResult(new ErrorCollection(), returnedValue, user);
        }
    }
}
