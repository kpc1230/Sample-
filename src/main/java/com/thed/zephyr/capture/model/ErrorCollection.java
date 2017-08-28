package com.thed.zephyr.capture.model;

import com.opensymphony.util.TextUtils;
import com.thed.zephyr.capture.exception.model.ErrorDto;
import com.thed.zephyr.capture.model.ErrorCollection.ErrorItem.Type;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;


/**
 * Class holds the validation errors.
 * 
 * @author manjunath
 *
 */
public class ErrorCollection {
	
    private List<ErrorItem> errors = new ArrayList<ErrorItem>();

    public ErrorCollection() {
    }

    public ErrorCollection(String messageKey, Object... params) {
        addFieldError(null, messageKey, params);
    }

    /**
     * Checks any errors and returns boolean value.
     * 
     * @return -- Returns boolean value true or false based on condition.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Adds the error message to the list.
     * 
     * @param field -- Filed name
     * @param message -- Error message
     * @param params -- Parameter values for the place holders in the error message.
     */
    public void addFieldError(String field, String message, Object... params) {
        notNull(message);
        errors.add(new ErrorItem(Type.error, field, message, params));
    }

    /**
     * Adds the error message to the list. 
     * 
     * @param message -- Error message
     * @param params -- Parameter values for the place holders in the error message.
     */
    public void addError(String message, Object... params) {
        addFieldError(null, message, params);
    }

    /**
     * Adds the warning message to the list. 
     * 
     * @param message -- Error message
     * @param params -- Parameter values for the place holders in the warning message.
     */
    public void addWarning(String message, Object... params) {
        notNull(message);
        errors.add(new ErrorItem(Type.warning, null, message, params));
    }

    /**
     * @return -- Returns the list of errors.
     */
    public List<ErrorItem> getErrors() {
        return errors;
    }

    /**
     * Clears the error list which holds the errors.
     */
    public void clear() {
        errors.clear();
    }

    /**
     * @param toAdd
     */
    public void addAllErrors(ErrorCollection toAdd) {
        errors.addAll(toAdd.getErrors());
    }

    /**
     * Fetch the error message for the specific field name.
     * 
     * @param field -- Field Name
     * @return -- Returns the fetched list of error message for the field name.
     */
    public List<ErrorItem> getFieldErrors(String field) {
        isTrue(field != null && field.length() > 0);
        List<ErrorItem> items = new ArrayList<ErrorItem>();
        for (ErrorItem item : errors) {
            if (item.getField().equals(field)) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * @return -- Returns the list of non field error messages.
     */
    public List<ErrorItem> getNonFieldErrors() {
        List<ErrorItem> items = new ArrayList<ErrorItem>();
        for (ErrorItem item : errors) {
            if (!item.isFieldError()) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * @return -- Returns the list of error messages
     */
    public Set<String> getErrorFields() {
        Set<String> fields = new HashSet<String>();
        for (ErrorItem item : errors) {
            if (item.isFieldError()) {
                fields.add(item.getField());
            }
        }
        return fields;
    }

    /**
     * @return -- Converts the error messages into error dto object.
     */
    public List<ErrorDto> toErrorDto() {
    	List<ErrorDto> listOfErrors = new ArrayList<>();
    	ErrorDto errorDto = null;
        for (ErrorItem error : errors) {
        	errorDto = new ErrorDto(null, error.getFormattedMessage());
        	listOfErrors.add(errorDto);
        }
        return listOfErrors;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class ErrorItem {
    	
        private String field;
        private String message;
        private Object[] params;
        private Type type;

        public ErrorItem(Type type, String field, String message, Object... params) {
            this.field = field;
            this.message = message;
            this.params = params;
            this.type = type;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public String getMessageKeyEscaped() {
            return TextUtils.htmlEncode(message);
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getFormattedMessage() {
            return type.name() + " for field - " + field + " : " + MessageFormat.format(message, params);
        }

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }

        public boolean isFieldError() {
            return this.field != null && this.field.length() > 0;
        }

        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }

        public static enum Type {
            error,
            warning
        }
    }
}
