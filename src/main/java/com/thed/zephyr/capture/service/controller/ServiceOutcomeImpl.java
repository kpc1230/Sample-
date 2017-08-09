package com.thed.zephyr.capture.service.controller;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection;

import javax.annotation.Nullable;

/**
 * Implementation of ServiceOutcome
 *
 * @since v1.7
 */
public class ServiceOutcomeImpl<T> implements ServiceOutcome<T> {
    private ErrorCollection errorCollection;
    private T value;

    /**
     * Creates a new ServiceOutcomeImpl with the given errors and returned value.
     *
     * @param errorCollection an ErrorCollection
     * @param value           the wrapped value
     */
    public ServiceOutcomeImpl(ErrorCollection errorCollection, @Nullable T value) {
        this.errorCollection = errorCollection;
        this.value = value;
    }

    @Override
    public boolean isValid() {
        return !errorCollection.hasErrors();
    }

    @Override
    public ErrorCollection getErrorCollection() {
        return errorCollection;
    }

    @Override
    public
    @Nullable
    T getReturnedValue() {
        return value;
    }
}
