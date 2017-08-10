package com.thed.zephyr.capture.rest.model;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection.ErrorItem;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.MessageSet;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class represents a series of errors that can be sent back to the REST client
 * <p>
 * DONT use this as a general purpose error collection.
 * </p>
 * <p>
 * Use {@link com.atlassian.borrowed.greenhopper.web.ErrorCollection} instead
 * </p>
 */
@XmlRootElement
public class RequestErrorsBean {
    private static final String DATA_FIELD_NAME = "data";

    @XmlElement
    private List<Error> errors;

    public RequestErrorsBean() {
        this.errors = new ArrayList<Error>();
    }

    public RequestErrorsBean(ErrorCollection jiraErrorCollection) {
        this();
        this.addErrorCollectionImpl(jiraErrorCollection);
    }

    public void addError(String errorMessage, String field) {
        errors.add(new Error(errorMessage, field));
    }

    public void addError(String errorMessage, String field, String errorKey) {
        errors.add(new Error(errorMessage, field, errorKey));
    }

    /**
     * Turns a JIRA {@link ErrorCollection} into a set of errors for this object
     *
     * @param jiraErrorCollection the jira error collection in play
     */
    public void addErrors(ErrorCollection jiraErrorCollection) {
        addErrorCollectionImpl(jiraErrorCollection);
    }

    /**
     * Turns a JIRA {@link MessageSet}
     *
     * @param jiraMessageSet the jira message set in play
     */
    public void addErrors(MessageSet jiraMessageSet) {
        addErrorCollectionImpl(jiraMessageSet);
    }

    /**
     * Turns a GreenHopper {@link com.atlassian.borrowed.greenhopper.web.ErrorCollection} into a set of errors for this object
     *
     * @param errorCollection the error collection to add
     */
    public void addErrors(com.atlassian.borrowed.greenhopper.web.ErrorCollection errorCollection) {
        for (ErrorItem error : errorCollection.getErrors()) {
            this.addError(error.toString(), StringUtils.defaultString(error.getField(), DATA_FIELD_NAME));
        }
    }

    private void addErrorCollectionImpl(ErrorCollection jiraErrorCollection) {
        Map<String, String> fieldErrors = jiraErrorCollection.getErrors();
        for (String fieldName : fieldErrors.keySet()) {
            this.addError(fieldErrors.get(fieldName), fieldName);
        }
        Collection<String> errorMessages = jiraErrorCollection.getErrorMessages();
        for (String errorMessage : errorMessages) {
            this.addError(errorMessage, "data");
        }
    }

    private void addErrorCollectionImpl(MessageSet messageSet) {
        Collection<String> errorMessages = messageSet.getErrorMessages();
        for (String errorMessage : errorMessages) {
            this.addError(errorMessage, "data");
        }
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @XmlRootElement
    private static class Error {
        @XmlElement
        private String errorMessage;

        @XmlElement
        private String field;

        @XmlElement
        private String errorKey;

        public Error(String errorMessage, String field) {
            this(errorMessage, field, null);
        }

        public Error(String errorMessage, String field, @Nullable String errorKey) {
            this.errorMessage = errorMessage;
            this.field = field;
            this.errorKey = errorKey;
        }

        public Error() {
        }
    }
}
