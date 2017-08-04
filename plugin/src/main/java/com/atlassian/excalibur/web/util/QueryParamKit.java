package com.atlassian.excalibur.web.util;

import com.atlassian.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * A class to help with storing/retrieving data in query params in Bonfire.
 *
 * @since v1.4
 */
public class QueryParamKit {
    public static final String BONFIRE_QUERY_STORE = "bonQueryStore";

    /**
     * Returns JSON object in the query params under the specified key
     *
     * @param request the request in play
     * @return a JSON object which is the stored value or an empty JSON object if none is there.
     */
    public static JSONObject getJSON(final HttpServletRequest request) {
        return JSONKit.to(decodeJSON(request.getParameter(BONFIRE_QUERY_STORE)));
    }

    public static String jsonToQueryParamString(JSONObject json) {
        return BONFIRE_QUERY_STORE + "=" + encodeJSON(json.toString());
    }

    private static String decodeJSON(String encodedJSON) {
        if (StringUtils.isBlank(encodedJSON)) {
            return "";
        }

        return new String(Base64.decodeBase64(encodedJSON));
    }

    private static String encodeJSON(String rawJSON) {
        if (StringUtils.isBlank(rawJSON)) {
            return "";
        }
        return Base64.encodeBase64String(rawJSON.getBytes());
    }
}
