package com.atlassian.excalibur.view;

import com.atlassian.excalibur.web.util.PreferenceKit;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NotesFilterStateUI {
    private static final String NOTES_FILTER_STATE = "notesFilterState";

    private final boolean filterNothing;
    private final boolean filterApplied;
    private final boolean filterIssues;
    private final boolean filterQuestion;
    private final boolean filterFollowup;
    private final boolean filterIdea;
    private final boolean filterAssumption;
    private final boolean filterUntagged;
    private final boolean filterIncomplete;
    private final boolean filterComplete;
    private final String filterURL;

    private final JSONObject initialCookieState;

    public NotesFilterStateUI(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String filterURL) {
        initialCookieState = PreferenceKit.getJSON(httpServletRequest, NOTES_FILTER_STATE);

        boolean noCookie = !PreferenceKit.has(httpServletRequest, NOTES_FILTER_STATE);
        boolean filterSpecified = hasParam(httpServletRequest, "notesFilterSpecified");

        boolean showEverything = bool(httpServletRequest, "notesFilterNothing") || (noCookie && !filterSpecified);
        if (showEverything) {
            filterNothing = true;
            filterApplied = false;
            filterIssues = false;
            filterQuestion = false;
            filterFollowup = false;
            filterIdea = false;
            filterAssumption = false;
            filterUntagged = false;
            filterIncomplete = false;
            filterComplete = false;
        } else {
            filterNothing = false;
            filterApplied = true;
            filterIssues = bool(httpServletRequest, "notesFilterIssues");
            filterQuestion = bool(httpServletRequest, "notesFilterQuestion");
            filterFollowup = bool(httpServletRequest, "notesFilterFollowup");
            filterIdea = bool(httpServletRequest, "notesFilterIdea");
            filterAssumption = bool(httpServletRequest, "notesFilterAssumption");
            filterUntagged = bool(httpServletRequest, "notesFilterUntagged");
            filterIncomplete = bool(httpServletRequest, "notesFilterIncomplete");
            filterComplete = bool(httpServletRequest, "notesFilterComplete");
        }
        this.filterURL = filterURL;

        // now save this into the cookie if they have applied a new filter
        if (filterSpecified) {
            PreferenceKit.saveJSON(httpServletRequest, httpServletResponse, NOTES_FILTER_STATE, buildCurrentStateJSON());
        }
    }

    private JSONObject buildCurrentStateJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("notesFilterNothing", filterNothing);
            json.put("notesFilterIssues", filterIssues);
            json.put("notesFilterQuestion", filterQuestion);
            json.put("notesFilterFollowup", filterFollowup);
            json.put("notesFilterIdea", filterIdea);
            json.put("notesFilterAssumption", filterAssumption);
            json.put("notesFilterUntagged", filterUntagged);
            json.put("notesFilterIncomplete", filterIncomplete);
            json.put("notesFilterComplete", filterComplete);
        } catch (JSONException e) {
            // just never gonna happen
        }
        return json;
    }


    private boolean bool(HttpServletRequest httpServletRequest, String parameterName) {
        String parameter = httpServletRequest.getParameter(parameterName);
        if (parameter == null && !hasParam(httpServletRequest, "notesFilterSpecified")) {
            // they have not pressed the filter button so lest see what the defaults tell us
            return initialCookieState.optBoolean(parameterName);
        }
        return Boolean.valueOf(parameter);
    }

    private boolean hasParam(HttpServletRequest httpServletRequest, String parameterName) {
        return StringUtils.isNotBlank(httpServletRequest.getParameter(parameterName));
    }

    public boolean isNothing() {
        return filterNothing;
    }

    public boolean isApplied() {
        return filterApplied;
    }

    public boolean isIssues() {
        return filterIssues;
    }

    public boolean isQuestion() {
        return filterQuestion;
    }

    public boolean isFollowup() {
        return filterFollowup;
    }

    public boolean isIdea() {
        return filterIdea;
    }

    public boolean isAssumption() {
        return filterAssumption;
    }

    public boolean isUntagged() {
        return filterUntagged;
    }

    public boolean isIncomplete() {
        return filterIncomplete;
    }

    public boolean isComplete() {
        return filterComplete;
    }

    public String getFilterURL() {
        return filterURL;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
