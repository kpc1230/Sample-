package com.thed.zephyr.capture.service.dao;

import com.thed.zephyr.capture.model.Variable;
import com.thed.zephyr.capture.service.BonfireUserService;
import com.atlassian.borrowed.greenhopper.service.PersistenceService;
import com.atlassian.excalibur.index.iterators.IndexUtils;
import com.atlassian.excalibur.index.iterators.JSONArrayIterator;
import com.atlassian.excalibur.service.lock.LockOperations;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONObject;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Dao for Variables
 *
 * @since v1.8
 */
@Service(VariableDao.SERVICE)
public class VariableDao {
    public static final String SERVICE = "bonfire-variableDao";

    @VisibleForTesting
    static final String LOCK_NAME = VariableDao.class.getName();

    private static final String KEY_VARIABLES = "Bonfire.Variable.Data";

    private static final Long DATA_VERSION = 1L;

    @Resource(name = PersistenceService.SERVICE)
    private PersistenceService persistenceService;

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService bonfireUserService;

    @Resource
    private LockOperations lockOperations;

    public Iterable<Variable> loadVariables(final ApplicationUser user) {
        return new Iterable<Variable>() {
            @Override
            public Iterator<Variable> iterator() {
                return new VariablesIterator(user.getName());
            }
        };
    }

    public Iterable<Variable> loadAllVariables() {
        return loadAllVariablesIndex();
    }

    public void save(final Variable variable) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                final JSONArrayIterator variablesIterator = loadVariableIndex(variable.getOwnerName());
                final String rebuiltIndex = IndexUtils.addToJSONIndex(variable.toJSON(), variablesIterator);
                saveVariableIndex(variable.getOwnerName(), rebuiltIndex);
            }
        });
    }

    public void delete(final Variable variable) {
        lockOperations.runUnderLock(LOCK_NAME, new Runnable() {
            @Override
            public void run() {
                final JSONArrayIterator variablesIterator = loadVariableIndex(variable.getOwnerName());
                final String rebuiltIndex = IndexUtils.deleteFromJSONIndex(variable.getId(), variablesIterator);
                saveVariableIndex(variable.getOwnerName(), rebuiltIndex);
            }
        });
    }

    /**
     * Checks that the given variable exists (with the same value)
     *
     * @param variable the variable to check (required)
     * @return if the variable is present
     */
    public boolean exists(final Variable variable) {
        final VariablesIterator variablesIterator = new VariablesIterator(variable.getOwnerName());
        while (variablesIterator.hasNext()) {
            if (variablesIterator.next().getId().equals(variable.getId())) {
                return true;
            }
        }
        return false;
    }

    private void saveVariableIndex(String ownerName, String rebuiltIndex) {
        String ownerKey = bonfireUserService.getUserKey(ownerName);
        persistenceService.setText(KEY_VARIABLES, DATA_VERSION, ownerKey, rebuiltIndex);
    }

    private JSONArrayIterator loadVariableIndex(String ownerName) {
        String ownerKey = bonfireUserService.getUserKey(ownerName);
        return persistenceService.getJSONArrayIterator(KEY_VARIABLES, DATA_VERSION, ownerKey);
    }

    private Iterable<Variable> loadAllVariablesIndex() {
        final Set<String> variableOwnerKeys = persistenceService.getKeys(KEY_VARIABLES, DATA_VERSION);
        final int initialCapacity = variableOwnerKeys.size() * 4; // each user has at least 4 default variables
        final List<Variable> allVariables = new ArrayList<Variable>(initialCapacity);

        for (String ownerKey : variableOwnerKeys) {
            final JSONArrayIterator jsonArrayIterator = persistenceService.getJSONArrayIterator(KEY_VARIABLES, DATA_VERSION, ownerKey);
            while (jsonArrayIterator.hasNext()) {
                final Variable variable = Variable.create((JSONObject) jsonArrayIterator.next());
                allVariables.add(variable);
            }
        }
        return allVariables;
    }

    private class VariablesIterator implements Iterator<Variable> {
        private final JSONArrayIterator variablesIterator;

        public VariablesIterator(final String userName) {
            this.variablesIterator = loadVariableIndex(userName);
        }

        @Override
        public boolean hasNext() {
            return variablesIterator.hasNext();
        }

        @Override
        public Variable next() {
            return Variable.create((JSONObject) variablesIterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
