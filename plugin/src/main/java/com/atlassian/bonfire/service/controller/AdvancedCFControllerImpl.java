package com.atlassian.bonfire.service.controller;

import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfireJiraHelperService;
import com.atlassian.bonfire.service.dao.AdvancedCFDao;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service(AdvancedCFController.SERVICE)
public class AdvancedCFControllerImpl implements AdvancedCFController {
    @Resource(name = BonfireJiraHelperService.SERVICE)
    private BonfireJiraHelperService bonfireJiraHelperService;

    @Resource(name = AdvancedCFDao.SERVICE)
    private AdvancedCFDao advancedCFDao;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Override
    public SaveAdvancedCFResult validateAdvancedCF(ApplicationUser user, JSONObject advancedFields) {
        ErrorCollection errorCollection = new ErrorCollection();
        String projectId = advancedFields.getString(PROJECT_ID_KEY);
        bonfireJiraHelperService.getAndValidateProject(user, projectId, errorCollection);
        String issueTypeId = advancedFields.getString(ISSUE_TYPE_ID_KEY);
        bonfireJiraHelperService.getAndValidateIssueType(user, issueTypeId, errorCollection);
        String randomId = advancedFields.getString(RANDOM_ID_KEY);
        validateRID(randomId, errorCollection);

        return new SaveAdvancedCFResult(errorCollection, advancedFields);
    }

    @Override
    public void saveAdvancedCF(ApplicationUser user, SaveAdvancedCFResult result) {
        if (result.isValid()) {
            // Blindly save because it is a temporary location that is overridden anyways
            advancedCFDao.save(user, result.getReturnedValue().toString());
        }
    }

    @Override
    public void clearAdvancedCF(ApplicationUser user) {
        if (user != null) {
            // Blindly delete because it is a temporary location that is overridden anyways
            advancedCFDao.delete(user);
        }
    }

    @Override
    public JSONObject getAdvancedCFAsJSON(ApplicationUser user, Long pid, Long itid, String rid) {
        if (user != null) {
            String rawData = advancedCFDao.load(user);
            if (StringUtils.isNotBlank(rawData)) {
                final JSONObject json;
                try {
                    json = new JSONObject(rawData);
                } catch (JSONException e) {
                    // Data is bad so ignore it.
                    return null;
                }
                String projectId = json.getString(PROJECT_ID_KEY);
                String issueTypeId = json.getString(ISSUE_TYPE_ID_KEY);
                String randomId = json.getString(RANDOM_ID_KEY);
                if (validateLongAndStringEqual(pid, projectId) && validateLongAndStringEqual(itid, issueTypeId) && checkRID(rid, randomId)) {
                    return json;
                }
            }
        }
        // if the project, issuetype and random id isn't the same then ignore it
        return null;
    }

    /**
     * This builds a Map that is in a form the customFieldsImpl likes. This *only* includes custom fields
     */
    @Override
    public Map<String, String[]> getAdvancedCFAsMap(ApplicationUser user, Long pid, Long itid, String rid) {
        // Gets relevant data in JSON form, or null otherwise
        JSONObject json = getAdvancedCFAsJSON(user, pid, itid, rid);
        if (json == null) {
            return null;
        }
        Map<String, String[]> map = Maps.newHashMap();
        for (Object idObj : json.names()) {
            String id = (String) idObj;
            if (id.startsWith("customfield_")) {
                JSONArray jsonArray = json.getJSONArray(id);
                String[] values = new String[jsonArray.length()];
                for (int i = 0; i != jsonArray.length(); i++) {
                    values[i] = jsonArray.getString(i);
                }
                map.put(id, values);
            }
        }
        return map;
    }

    private boolean validateLongAndStringEqual(Long l, String s) {
        if (l == null || StringUtils.isBlank(s)) {
            return false;
        }
        try {
            Long converted = Long.parseLong(s);
            return l.equals(converted);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkRID(String one, String two) {
        if (StringUtils.isBlank(one) || StringUtils.isBlank(two)) {
            return false;
        }

        return one.equals(two);
    }

    private void validateRID(String rid, ErrorCollection errorCollection) {
        if (StringUtils.isBlank(rid)) {
            errorCollection.addError(i18n.getText("advanced.cft.invalid.rid"));
        }
    }

}
