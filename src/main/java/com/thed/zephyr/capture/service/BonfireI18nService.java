package com.thed.zephyr.capture.service;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection;

import java.util.Locale;

/**
 * I18n related services and error handling for i18n messages
 *
 * @since v1.4
 */
public interface BonfireI18nService {
    public static final String SERVICE = "bonfire-i18nService";

    /**
     * This will return the text for the specified message key
     *
     * @param key    the message key to retrieve text for
     * @param params optional parameters for that message
     * @return a resolved i18n string based on the current users locale
     */
    public String getText(String key, Object... params);


    /**
     * @return the Locale of the currently logged in user or System locale if there is no logged in user
     */
    public Locale getLocale();

    /**
     * This will add an error message to the specified {@link ErrorCollection} with the specified I18n message key
     *
     * @param errorCollection the place to put the error
     * @param fieldName       the field to record the error against
     * @param errorKey        the message key to retrieve text for
     * @param params          optional parameters for that message
     */
    public void addError(ErrorCollection errorCollection, final String fieldName, String errorKey, Object... params);


}
