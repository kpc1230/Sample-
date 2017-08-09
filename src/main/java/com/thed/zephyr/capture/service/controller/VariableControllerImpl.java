package com.thed.zephyr.capture.service.controller;

import com.thed.zephyr.capture.model.Variable;
import com.thed.zephyr.capture.service.BonfireI18nService;
import com.thed.zephyr.capture.service.dao.VariableDao;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.service.dao.IdDao;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Controller for storing and retrieving variables
 *
 * @since v1.8
 */
@Service(VariableController.SERVICE)
public class VariableControllerImpl implements VariableController {
    @Resource(name = TemplateController.SERVICE)
    private TemplateController templateController;

    @Resource(name = VariableDao.SERVICE)
    private VariableDao variableDao;

    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource
    private PermissionManager jiraPermissionManager;

    private final Logger log = Logger.getLogger(this.getClass());

    @Override
    public CreateResult validateCreate(ApplicationUser creator, JSONObject variableJSON) {
        notNull(creator);
        notNull(variableJSON);

        ErrorCollection errorCollection = new ErrorCollection();

        Variable variable = Variable.INVALID;

        try {
            variable = Variable.create(idDao.genNextId(), variableJSON.getString(Variable.KEY_NAME), variableJSON.getString(Variable.KEY_VALUE), creator);
            validate(variable, errorCollection);
        } catch (JSONException e) {
            errorCollection.addError(i18n.getText("rest.resource.malformed.json"));
        }

        if (variableDao.exists(variable)) {
            errorCollection.addError(i18n.getText("variable.already.present"));
        }

        return new CreateResult(errorCollection, variable, creator);
    }

    public VariableResult create(CreateResult result) {
        if (!result.isValid()) {
            return result;
        }
        Variable var = result.getReturnedValue();
        ApplicationUser user = result.getUser();

        variableDao.save(var);
        templateController.variableUpdated(var, user);

        return VariableResult.ok(var, user);
    }

    @Override
    public UpdateResult validateUpdate(ApplicationUser updater, Variable updatedVariable) {
        notNull(updater);
        notNull(updatedVariable);

        ErrorCollection errorCollection = new ErrorCollection();

        validate(updatedVariable, errorCollection);
        if (!errorCollection.hasErrors()) {
            if (!canModifyVariable(updater, updatedVariable)) {
                errorCollection.addError(i18n.getText("variable.validate.permission.update"));
            }
        }

        if (!variableDao.exists(updatedVariable)) {
            errorCollection.addError(i18n.getText("variable.not.present"));
        }

        return new UpdateResult(errorCollection, updatedVariable, updater);
    }

    public VariableResult update(UpdateResult result) {
        if (!result.isValid()) {
            return result;
        }
        Variable var = result.getReturnedValue();
        ApplicationUser user = result.getUser();

        variableDao.save(var);
        templateController.variableUpdated(var, user);

        return VariableResult.ok(var, user);
    }

    @Override
    public DeleteResult validateDelete(ApplicationUser deleter, Variable variable) {
        ErrorCollection errorCollection = new ErrorCollection();

        // Check that the variable is present
        if (!variableDao.exists(variable)) {
            errorCollection.addError(i18n.getText("variable.not.present"));
        }

        // Check we have permission to delete it
        if (!canModifyVariable(deleter, variable)) {
            errorCollection.addError(i18n.getText("variable.validate.permission.delete"));
        }

        return new DeleteResult(errorCollection, variable, deleter);
    }

    @Override
    public VariableResult delete(DeleteResult result) {
        if (!result.isValid()) {
            return result;
        }
        Variable var = result.getReturnedValue();
        ApplicationUser user = result.getUser();

        variableDao.delete(var);
        templateController.variableDeleted(var, user);

        return VariableResult.ok(var, user);
    }

    private void validate(Variable variable, ErrorCollection errorCollection) {
        if (variable.getName().trim().isEmpty()) {
            errorCollection.addError(i18n.getText("variable.validate.name.empty"));
        }

        if (variable.getValue().trim().isEmpty()) {
            errorCollection.addError(i18n.getText("variable.validate.value.empty"));
        }

        if (variable.getOwnerName().trim().isEmpty()) {
            errorCollection.addError(i18n.getText("variable.validate.owner.empty"));
        }
    }

    private static boolean isUserOwnerOfVariable(ApplicationUser user, Variable variable) {
        return user.getKey().equals(variable.getOwnerName());
    }

    @Override
    public Iterable<Variable> loadVariables(ApplicationUser user) {
        notNull(user);
        Iterable<Variable> variables = variableDao.loadVariables(user);
        if (Iterables.isEmpty(variables)) {
            return createDefaultVariables(user);
        }
        return variables;
    }

    public Iterable<Variable> loadVariablesForTemplate(final ApplicationUser creator, final JSONObject source) {
        return new Iterable<Variable>() {
            public Iterator<Variable> iterator() {
                return new VariablesInTemplateIterator(creator, source);
            }
        };
    }

    @Override
    public Iterable<Variable> loadVariablesForAdmin() {
        return variableDao.loadAllVariables();
    }

    // Could be smarter, but not worth it since this doesn't get called so much
    private Iterable<Variable> createDefaultVariables(ApplicationUser user) {
        List<Variable> variableList = new ArrayList<Variable>();
        for (String name : BonfireDefaultVariables.DEFAULT_VARIABLES.keySet()) {
            Variable var = Variable.create(idDao.genNextId(), name, BonfireDefaultVariables.DEFAULT_VARIABLES.get(name), user);
            variableDao.save(var);
            templateController.variableUpdated(var, user);
            variableList.add(var);
        }
        return variableList;
    }

    private static final Pattern variablePattern = Pattern.compile("\\{(\\w+)\\}"); // So many escape characters, it's almost like perl!

    private class VariablesInTemplateIterator implements Iterator<Variable> {
        private final JSONObject templateJSON;
        private final ApplicationUser owner;
        private final Iterator<String> keys;
        private Matcher currentMatcher;
        private Variable variable;
        private Map<String, Variable> userVariables;

        public VariablesInTemplateIterator(ApplicationUser owner, JSONObject templateJSON) {
            this.templateJSON = templateJSON;
            this.keys = templateJSON.keys();
            this.owner = owner;
            this.variable = Variable.INVALID;
            // Might be nice if we passed this map in, to avoid re-building it again and again for the same user.
            this.userVariables = new HashMap<String, Variable>();
            Iterator<Variable> variableIterator = variableDao.loadVariables(owner).iterator();
            while (variableIterator.hasNext()) {
                Variable nextVariable = variableIterator.next();
                userVariables.put(String.format("{%s}", nextVariable.getName()), nextVariable);
            }
        }

        @Override
        public boolean hasNext() {
            variable = Variable.INVALID;
            if (currentMatcher != null) {
                // Have a matcher in play already, try and squeeze more variables from it
                while (currentMatcher.find()) {
                    // Okay, we have a variable name. Does it correspond to a variable?
                    String variableName = currentMatcher.group();
                    if (userVariables.containsKey(variableName)) {
                        variable = userVariables.get(variableName);
                        userVariables.remove(variableName);
                        return true;
                    }
                }
            }
            while (keys.hasNext()) {
                try {
                    String currentItem = templateJSON.getString(keys.next());
                    currentMatcher = variablePattern.matcher(currentItem);
                    while (currentMatcher.find()) {
                        // Okay, we have a variable name. Does it correspond to a variable?
                        String variableName = currentMatcher.group();
                        if (userVariables.containsKey(variableName)) {
                            variable = userVariables.get(variableName);
                            userVariables.remove(variableName);
                            return true;
                        }
                    }
                } catch (JSONException e) {
                    continue;
                }
            }
            return false;
        }

        @Override
        public Variable next() {
            if (variable.equals(Variable.INVALID)) {
                throw new NoSuchElementException("hasNext() has not be called or ignored");
            }
            return variable;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }

    }

    private boolean isSysAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    private boolean isAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

    private boolean canModifyVariable(ApplicationUser user, Variable oldVariable) {
        return isUserOwnerOfVariable(user, oldVariable) || isAdmin(user) || isSysAdmin(user);
    }
}
