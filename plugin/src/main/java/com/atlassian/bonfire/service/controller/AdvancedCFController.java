package com.atlassian.bonfire.service.controller;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONObject;

import java.util.Map;

public interface AdvancedCFController {
    public static final String SERVICE = "bonfire-advancedCFController";

    public static final String PROJECT_ID_KEY = "pid";
    public static final String ISSUE_TYPE_ID_KEY = "itid";
    public static final String RANDOM_ID_KEY = "rid";

    public SaveAdvancedCFResult validateAdvancedCF(ApplicationUser user, JSONObject advancedFields);

    public void saveAdvancedCF(ApplicationUser user, SaveAdvancedCFResult result);

    public JSONObject getAdvancedCFAsJSON(ApplicationUser user, Long pid, Long itid, String rid);

    public Map<String, String[]> getAdvancedCFAsMap(ApplicationUser user, Long pid, Long itid, String rid);

    public void clearAdvancedCF(ApplicationUser user);

    public static class SaveAdvancedCFResult extends ServiceOutcomeImpl<JSONObject> {
        public SaveAdvancedCFResult(ErrorCollection errorCollection, JSONObject advancedFields) {
            super(errorCollection, advancedFields);
        }

        /**
         * Convenience method that returns a new ServiceOutcomeImpl instance containing no errors, and with the provided returned value.
         *
         * @param returnedValue the returned value
         * @return a new ServiceOutcomeImpl
         */
        public static SaveAdvancedCFResult ok(JSONObject returnedValue) {
            return new SaveAdvancedCFResult(new ErrorCollection(), returnedValue);
        }
    }
}
