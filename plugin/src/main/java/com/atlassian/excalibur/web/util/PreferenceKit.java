package com.atlassian.excalibur.web.util;

import com.atlassian.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * <p>
 * A class to help with preference "remembering" within Bonfire.
 * </p>
 */
public class PreferenceKit {
    /**
     * Returns JSON object preference under the specified key
     *
     * @param httpServletRequest the request in play
     * @param key                the key to lookup the JSON value as
     * @return a JSON object which is the stored value or an empty JSON object if none is there.
     */
    public static JSONObject getJSON(final HttpServletRequest httpServletRequest, final String key) {
        notNull("key", key);
        return CookieKit.getJSON(httpServletRequest, key);
    }


    /**
     * Saves a JSON object as a preference under the specified key
     *
     * @param httpServletRequest  the http request in play
     * @param httpServletResponse the http response in play
     * @param key                 the key to save the JSON object under
     * @param json                the JSON to save
     */
    public static void saveJSON(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final String key, JSONObject json) {
        notNull("key", key);
        CookieKit.saveJSON(httpServletRequest, httpServletResponse, key, json);
    }

    /**
     * Removes a value from the preference under the specified key
     *
     * @param httpServletRequest  the http request in play
     * @param httpServletResponse the http response in play
     * @param key                 the key to remove
     */
    public static void remove(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final String key) {
        notNull("key", key);
        CookieKit.remove(httpServletRequest, httpServletResponse, key);
    }

    /**
     * Returns true if the preference has a value under the specified key
     *
     * @param httpServletRequest the http request in play
     * @param key                the key to check for
     * @return true if there is a value under the specified key
     */
    public static boolean has(HttpServletRequest httpServletRequest, String key) {
        return CookieKit.has(httpServletRequest, key);
    }
}
