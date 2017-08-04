package com.atlassian.bonfire.service.controller;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection;

/**
 * Bonfire's very own ServiceOutcome
 * This uses an {@link com.atlassian.borrowed.greenhopper.web.ErrorCollection} error collection, because that's what we use everywhere else.
 *
 * @since v1.7
 */
public interface ServiceOutcome<T> {
    /**
     * @return true if there are no errors, false otherwise.
     */
    boolean isValid();

    /**
     * @return an {@link com.atlassian.borrowed.greenhopper.web.ErrorCollection} that contains any errors that may have happened as a result of the validations.
     */
    ErrorCollection getErrorCollection();

    /**
     * Returns the value that was returned by the service, or null.
     *
     * @return the value returned by the service, or null
     */
    T getReturnedValue();
}
