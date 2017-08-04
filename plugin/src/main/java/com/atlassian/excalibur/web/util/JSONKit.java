package com.atlassian.excalibur.web.util;

import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * {@link JSONObject} is a cool class but it has checked exceptions and they can be a pain in the arse.  So these helpers
 * are there remove some of that annoying code paths.  Motsly useful when you KNOW you cant get an exception or you want default
 * behaviour if you do.
 */
public class JSONKit {
    /**
     * Add a child json object as a property of the parent JSON object and returns the parent Object
     *
     * @param parentJSON   the parent to add to
     * @param propertyName the property name
     * @param child        the value to put in the JSONObject
     * @return the parent JSON object
     */
    public static JSONObject put(JSONObject parentJSON, String propertyName, Object child) {
        notNull("parentJSON", parentJSON);
        notNull("propertyName", propertyName);
        try {
            parentJSON.put(propertyName, child);
        } catch (JSONException e) {
            throw new RuntimeException("I dont see how this can ever happen but if you are seeing this then they are some pretty pathetic last words eh?", e);
        }
        return parentJSON;
    }

    /**
     * Returns a JSONObject property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param propertyName the property name
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static JSONObject get(final JSONObject json, final String propertyName, final JSONObject defaultValue) {
        notNull("json", json);
        notNull("propertyName", propertyName);
        JSONObject property = json.optJSONObject(propertyName);
        return property == null ? defaultValue : property;
    }

    public static JSONObject get(final JSONObject json, final String propertyName) {
        return get(json, propertyName, new JSONObject());
    }

    /**
     * Returns a JSONArray property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param propertyName the property name
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static JSONArray getArray(final JSONObject json, final String propertyName, final JSONArray defaultValue) {
        notNull("json", json);
        notNull("propertyName", propertyName);
        JSONArray property = json.optJSONArray(propertyName);
        return property == null ? defaultValue : property;
    }

    public static JSONArray getArray(final JSONObject json, final String propertyName) {
        return getArray(json, propertyName, new JSONArray());
    }

    /**
     * Returns a String property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param propertyName the property name
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static String getString(final JSONObject json, final String propertyName, final String defaultValue) {
        notNull("json", json);
        notNull("propertyName", propertyName);
        String property = json.optString(propertyName);
        return property == null ? defaultValue : property;
    }

    public static String getString(final JSONObject json, final String propertyName) {
        return getString(json, propertyName, "");
    }

    /**
     * Returns a Long property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param propertyName the property name
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static Long getLong(final JSONObject json, final String propertyName, final Long defaultValue) {
        notNull("json", json);
        notNull("propertyName", propertyName);
        Long property = json.optLong(propertyName);
        return property == null ? defaultValue : property;
    }

    public static Long getLong(final JSONObject json, final String propertyName) {
        return getLong(json, propertyName, 0L);
    }

    /**
     * Returns a Boolean property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param propertyName the property name
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static Boolean getBoolean(final JSONObject json, final String propertyName, final Boolean defaultValue) {
        notNull("json", json);
        notNull("propertyName", propertyName);
        Boolean property = json.optBoolean(propertyName);
        return property == null ? defaultValue : property;
    }

    public static Boolean getBoolean(final JSONObject json, final String propertyName) {
        return getBoolean(json, propertyName, Boolean.FALSE);
    }

    /* ======================= */

    /**
     * Returns a JSONObject property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param index        the index to retrive
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static JSONObject get(final JSONArray json, final int index, final JSONObject defaultValue) {
        notNull("json", json);
        JSONObject property = json.optJSONObject(index);
        return property == null ? defaultValue : property;
    }

    public static JSONObject get(final JSONArray json, final int index) {
        return get(json, index, new JSONObject());
    }

    /**
     * Returns a JSONArray property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param index        the index to retrive
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static JSONArray getArray(final JSONArray json, final int index, final JSONArray defaultValue) {
        notNull("json", json);
        JSONArray property = json.optJSONArray(index);
        return property == null ? defaultValue : property;
    }

    public static JSONArray getArray(final JSONArray json, final int index) {
        return getArray(json, index, new JSONArray());
    }

    /**
     * Returns a String property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param index        the index to retrive
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static String getString(final JSONArray json, final int index, final String defaultValue) {
        notNull("json", json);
        String property = json.optString(index);
        return property == null ? defaultValue : property;
    }

    public static String getString(final JSONArray json, final int index) {
        return getString(json, index, "");
    }

    /**
     * Returns a Long property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param index        the index to retrive
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static Long getLong(final JSONArray json, final int index, final Long defaultValue) {
        notNull("json", json);
        Long property = json.optLong(index);
        return property == null ? defaultValue : property;
    }

    public static Long getLong(final JSONArray json, final int index) {
        return getLong(json, index, 0L);
    }

    /**
     * Returns a Boolean property value or the default if there isnt one
     *
     * @param json         the JSON in play
     * @param index        the index to retrive
     * @param defaultValue the default if the property is not present
     * @return the value or the default
     */
    public static Boolean getBoolean(final JSONArray json, final int index, final Boolean defaultValue) {
        notNull("json", json);
        Boolean property = json.optBoolean(index);
        return property == null ? defaultValue : property;
    }

    public static Boolean getBoolean(final JSONArray json, final int index) {
        return getBoolean(json, index, Boolean.FALSE);
    }


    /**
     * Converts a string to a JSON object and returns the default JSONObject if it cant be parsed
     *
     * @param jsonStr      the JSON String to parse
     * @param defaultValue the default to return if it can be parsed
     * @return a non null JSONObject or the default if it cant be parsed
     */
    public static JSONObject to(final String jsonStr, JSONObject defaultValue) {
        try {
            if (StringUtils.isBlank(jsonStr)) {
                return defaultValue;
            }
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    /**
     * Converts a string to a JSON object and returns an empty JSONObject if it cant be parsed
     *
     * @param jsonStr the JSON String to parse
     * @return a non null JSONObject or an non null empty one if its not parseable
     */
    public static JSONObject to(final String jsonStr) {
        return to(jsonStr, new JSONObject());
    }

    public static JSONObject to(final InputStream stream) throws IOException {
        String jsonStr = IOUtils.toString(stream);
        return to(jsonStr, new JSONObject());
    }

    /**
     * Converts a string to a JSON object and returns the default if it cant be parsed
     *
     * @param jsonStr      the JSON String to parse
     * @param defaultValue the default to return if it can be parsed
     * @return a non null JSONArray or the default if it cant be parsed
     */
    public static JSONArray toArray(final String jsonStr, JSONArray defaultValue) {
        try {
            if (StringUtils.isBlank(jsonStr)) {
                return defaultValue;
            }
            return new JSONArray(jsonStr);
        } catch (JSONException e) {
            return defaultValue;
        }
    }

    /**
     * Converts a string to a JSON object and returns an empty JSONArray if it cant be parsed
     *
     * @param jsonStr the JSON String to parse
     * @return a non null JSONArray or an non null empty one if its not parseable
     */
    public static JSONArray toArray(final String jsonStr) {
        return toArray(jsonStr, new JSONArray());
    }

}
