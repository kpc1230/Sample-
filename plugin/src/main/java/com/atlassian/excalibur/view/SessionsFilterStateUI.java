package com.atlassian.excalibur.view;

import com.atlassian.excalibur.model.IndexedSession;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.web.util.PreferenceKit;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.base.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Captures the filter state variables for session filtering
 */
public class SessionsFilterStateUI {
    private static final String FILTER_SESSION_STATE = "sessionFilterState";

    private final boolean filterNothing;
    private final boolean filterApplied;
    private final boolean filterStatusCreated;
    private final boolean filterStatusStarted;
    private final boolean filterStatusPaused;
    private final boolean filterStatusCompleted;
    private final String filterURL;

    private final JSONObject initialCookieState;

    public SessionsFilterStateUI(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String filterURL) {
        initialCookieState = PreferenceKit.getJSON(httpServletRequest, FILTER_SESSION_STATE);

        boolean noCookie = !PreferenceKit.has(httpServletRequest, FILTER_SESSION_STATE);
        boolean filterSpecified = hasParam(httpServletRequest, "sessionsFilterSpecified");

        boolean showEverything = bool(httpServletRequest, "sessionsFilterNothing") || (noCookie && !filterSpecified);
        if (showEverything) {
            filterNothing = true;
            filterApplied = false;
            filterStatusCreated = false;
            filterStatusStarted = false;
            filterStatusPaused = false;
            filterStatusCompleted = false;
        } else {
            filterNothing = false;
            filterApplied = true;
            filterStatusCreated = bool(httpServletRequest, "sessionsFilterStatusCreated");
            filterStatusStarted = bool(httpServletRequest, "sessionsFilterStatusStarted");
            filterStatusPaused = bool(httpServletRequest, "sessionsFilterStatusPaused");
            filterStatusCompleted = bool(httpServletRequest, "sessionsFilterStatusCompleted");
        }
        this.filterURL = filterURL;

        // now save this into the cookie if they have applied a new filter
        if (filterSpecified) {
            PreferenceKit.saveJSON(httpServletRequest, httpServletResponse, FILTER_SESSION_STATE, buildCurrentStateJSON());
        }
    }

    private JSONObject buildCurrentStateJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("sessionsFilterNothing", filterNothing);
            json.put("sessionsFilterStatusCreated", filterStatusCreated);
            json.put("sessionsFilterStatusStarted", filterStatusStarted);
            json.put("sessionsFilterStatusPaused", filterStatusPaused);
            json.put("sessionsFilterStatusCompleted", filterStatusCompleted);
        } catch (JSONException e) {
            // just never gonna happen
        }
        return json;
    }


    private boolean bool(HttpServletRequest httpServletRequest, String parameterName) {
        String parameter = httpServletRequest.getParameter(parameterName);
        if (parameter == null && !hasParam(httpServletRequest, "sessionsFilterSpecified")) {
            // they have not pressed the filter button so lest see what the defaults tell us
            return initialCookieState.optBoolean(parameterName);
        }
        return Boolean.valueOf(parameter);
    }

    private boolean hasParam(HttpServletRequest httpServletRequest, String parameterName) {
        return StringUtils.isNotBlank(httpServletRequest.getParameter(parameterName));
    }

    public boolean isFilterNothing() {
        return filterNothing;
    }

    public boolean isApplied() {
        return filterApplied;
    }

    public boolean isFilterStatusCreated() {
        return filterStatusCreated;
    }

    public boolean isFilterStatusStarted() {
        return filterStatusStarted;
    }

    public boolean isFilterStatusPaused() {
        return filterStatusPaused;
    }

    public boolean isFilterStatusCompleted() {
        return filterStatusCompleted;
    }

    public String getFilterURL() {
        return filterURL;
    }

    /**
     * @return a Predicate based on the current filter state
     */
    public Predicate<IndexedSession> getSessionPredicate() {
        return new Predicate<IndexedSession>() {
            @Override
            public boolean apply(IndexedSession input) {
                if (isFilterNothing()) {
                    return true;
                }
                if (input.getStatus() == Session.Status.CREATED) {
                    return isFilterStatusCreated();
                }
                if (input.getStatus() == Session.Status.STARTED) {
                    return isFilterStatusStarted();
                }
                if (input.getStatus() == Session.Status.PAUSED) {
                    return isFilterStatusPaused();
                }
                if (input.getStatus() == Session.Status.COMPLETED) {
                    return isFilterStatusCompleted();
                }
                return false;
            }
        };
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
