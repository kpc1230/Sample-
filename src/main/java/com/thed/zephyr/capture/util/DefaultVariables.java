package com.thed.zephyr.capture.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultVariables {
    public static final Map<String, String> DEFAULT_VARIABLES;

    static {
        Map<String, String> defaultVars = new HashMap<String, String>();
        defaultVars.put("cookies", "navigator.cookieEnabled");
        defaultVars.put("useragent", "navigator.userAgent");
        defaultVars.put("title", "document.title");
        defaultVars.put("url", "document.URL");
        DEFAULT_VARIABLES = Collections.unmodifiableMap(defaultVars);
    }
}
