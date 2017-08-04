package com.atlassian.excalibur.web.util;

import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.web.util.CookieUtils;
import com.atlassian.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A class to help with cookie handling in Bonfire
 */
public class CookieKit {
    public static final String BONFIRE_CONGLOMERATE = "bonfire.conglomerate.cookie";

    /**
     * Returns JSON object into the bonfire conglomerate cookie under the specified key
     *
     * @param httpServletRequest the request in play
     * @param key                the key to lookup the JSON value as
     * @return a JSON object which is the stored value or an empty JSON object if none is there.
     */
    public static JSONObject getJSON(final HttpServletRequest httpServletRequest, final String key) {
        notNull("key", key);

        return getJSON(httpServletRequest, BONFIRE_CONGLOMERATE, key);
    }

    /**
     * Returns JSON object into the bonfire conglomerate cookie under the specified key
     *
     * @param httpServletRequest the request in play
     * @param cookieName         the name of the cookie to read
     * @param key                the key to lookup the JSON value as
     * @return a JSON object which is the stored value or an empty JSON object if none is there.
     */
    public static JSONObject getJSON(final HttpServletRequest httpServletRequest, final String cookieName, final String key) {
        notNull("cookieName", cookieName);
        notNull("key", key);

        JSONObject cookieJSON = parseConglomerateCookie(cookieName, httpServletRequest);
        return JSONKit.get(cookieJSON, key);
    }

    /**
     * Saves a JSON object in the bonfire conglomerate cookie under the specified key
     *
     * @param httpServletRequest  the http request in play
     * @param httpServletResponse the http response in play
     * @param key                 the key to save the JSON object under
     * @param json                the JSON to save
     */
    public static void saveJSON(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final String key, JSONObject json) {
        notNull("key", key);

        saveJSON(httpServletRequest, httpServletResponse, BONFIRE_CONGLOMERATE, key, json);
    }

    /**
     * Saves a JSON object in the bonfire conglomerate cookie under the specified key
     *
     * @param httpServletRequest  the http request in play
     * @param httpServletResponse the http response in play
     * @param cookieName          the cookie name to save to
     * @param key                 the key to save the JSON object under
     * @param json                the JSON to save
     */
    public static void saveJSON(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final String cookieName, final String key, JSONObject json) {
        notNull("cookieName", cookieName);
        notNull("key", key);

        JSONObject cookieJSON = parseConglomerateCookie(cookieName, httpServletRequest);
        JSONKit.put(cookieJSON, key, json);
        Cookie cookie = createConglomerateCookie(cookieName, cookieJSON, httpServletRequest);
        httpServletResponse.addCookie(cookie);
    }


    /**
     * Removes a value from the bonfire conglomerate cookie under the specified key
     *
     * @param httpServletRequest  the http request in play
     * @param httpServletResponse the http response in play
     * @param key                 the key to remove
     */
    public static void remove(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final String key) {
        remove(httpServletRequest, httpServletResponse, BONFIRE_CONGLOMERATE, key);
    }

    /**
     * Removes a value from the bonfire conglomerate cookie under the specified key
     *
     * @param httpServletRequest  the http request in play
     * @param httpServletResponse the http response in play
     * @param cookieName          the cookie name to read and write to
     * @param key                 the key to remove
     */
    public static void remove(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse, final String cookieName, final String key) {
        notNull("cookieName", cookieName);
        notNull("key", key);

        JSONObject cookieJSON = parseConglomerateCookie(cookieName, httpServletRequest);
        cookieJSON.remove(key);
        Cookie cookie = createConglomerateCookie(cookieName, cookieJSON, httpServletRequest);
        httpServletResponse.addCookie(cookie);
    }

    /**
     * Returns true if the bonfire conglomerate cookie has a value under the specified key
     *
     * @param httpServletRequest the http request in play
     * @param key                the key to check for
     * @return true if there is a value under the specified key
     */
    public static boolean has(HttpServletRequest httpServletRequest, String key) {
        JSONObject cookieJSON = parseConglomerateCookie(BONFIRE_CONGLOMERATE, httpServletRequest);
        return cookieJSON.has(key);
    }

    /**
     * Returns true if the bonfire conglomerate cookie has a value under the specified key
     *
     * @param httpServletRequest the http request in play
     * @param cookieName         the ane of the cookie to read
     * @param key                the key to check for
     * @return true if there is a value under the specified key
     */
    public static boolean has(HttpServletRequest httpServletRequest, final String cookieName, final String key) {
        notNull("cookieName", cookieName);
        notNull("key", key);

        JSONObject cookieJSON = parseConglomerateCookie(cookieName, httpServletRequest);
        return cookieJSON.has(key);
    }


    private static JSONObject parseConglomerateCookie(String cookieName, HttpServletRequest currentRequest) {
        final String cookieValue = CookieUtils.getCookieValue(cookieName, currentRequest);
        return JSONKit.to(decodeJSON(cookieValue));
    }

    private static Cookie createConglomerateCookie(final String cookieName, final JSONObject json, final HttpServletRequest request) {

        final String cookieValue = encodeJSON(json.toString());
        Cookie cookie = CookieUtils.createCookie(cookieName, cookieValue, request);
        cookie.setMaxAge(Integer.MAX_VALUE);
        return cookie;
    }

    private static String encodeJSON(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return "";
        }
        return JiraUrlCodec.encode(jsonStr, "UTF-8");
    }

    private static String decodeJSON(String cookieValue) {
        if (StringUtils.isBlank(cookieValue)) {
            return "";
        }
        return JiraUrlCodec.decode(cookieValue, "UTF-8");
    }

}
