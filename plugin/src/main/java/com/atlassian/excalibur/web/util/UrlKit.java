package com.atlassian.excalibur.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlKit {
    /**
     * This will add a URL parameter if its not present or replace it if it is to the specified values
     *
     * @param url       the URL to mutate
     * @param parameter the parameter name
     * @param value     the new value
     * @return the changed url
     */

    public static String addOrReplaceUrlParameter(String url, String parameter, Object value) {
        String regex = parameter + "=[^&]*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.replaceAll(parameter + "=" + value);
        } else {
            StringBuilder sb = new StringBuilder(url);
            if (url.contains("?") || url.contains("&")) {
                sb.append('&').append(parameter).append('=').append(value);
            } else {
                sb.append('?').append(parameter).append('=').append(value);
            }
            return sb.toString();
        }
    }

}
