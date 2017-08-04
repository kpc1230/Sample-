package com.atlassian.borrowed.greenhopper.web;

import com.atlassian.borrowed.greenhopper.web.ErrorCollection.ErrorItem.Type;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.collect.Lists;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;


public class ErrorCollection {
    private List<ErrorItem> errors = new ArrayList<ErrorItem>();

    public ErrorCollection() {
    }

    public ErrorCollection(String messageKey, Object... params) {
        addFieldError(null, messageKey, params);
    }

    public static ErrorCollection toErrorCollection(JSONArray jsonArray) {
        ErrorCollection errorCollection = new ErrorCollection();
        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject json = jsonArray.optJSONObject(i);
            if (json.length() > 0) {
                ErrorItem error = ErrorItem.toErrorItem(json);
                if (error != null) {
                    errorCollection.errors.add(error);
                }
            }
        }
        return errorCollection;
    }


    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addFieldError(String field, String messageKey, Object... params) {
        notNull(messageKey);
        errors.add(new ErrorItem(Type.error, field, messageKey, params));
    }

    public void addError(String messageKey, Object... params) {
        addFieldError(null, messageKey, params);
    }

    public void addWarning(String messageKey, Object... params) {
        notNull(messageKey);
        errors.add(new ErrorItem(Type.warning, null, messageKey, params));
    }

    public List<ErrorItem> getErrors() {
        return errors;
    }

    public void clear() {
        errors.clear();
    }

    public void addAllErrors(ErrorCollection toAdd) {
        errors.addAll(toAdd.getErrors());
    }

    public void addAllJiraErrors(com.atlassian.jira.util.ErrorCollection toAdd) {
        for (String error : toAdd.getErrorMessages()) {
            addFieldError(null, error);
        }
    }

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
     * Get all fields for which errors exist
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

    public JSONArray toJSON() {
        JSONArray jsonArr = new JSONArray();
        for (ErrorItem error : errors) {
            jsonArr.put(error.toJSON());
        }
        return jsonArr;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class ErrorItem {
        /**
         * Field this error item belongs to. Might be null.
         */
        private String field;
        private String messageKey;
        private Object[] params;
        private Type type;

        public static ErrorItem toErrorItem(JSONObject json) {
            try {
                String field = json.getString("field");
                String messageKey = json.getString("messageKey");
                Type type = Type.valueOf(json.getString("type"));

                JSONArray array = json.optJSONArray("params");
                Object[] params;
                if (array == null) {
                    params = new Object[0];
                } else {
                    params = new Object[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        params[i] = array.get(i);

                    }
                }
                return new ErrorItem(type, field, messageKey, params);
            } catch (JSONException e) {
                return null;
            }
        }

        public ErrorItem(Type type, String field, String messageKey, Object... params) {
            this.field = field;
            this.messageKey = messageKey;
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

        public String getMessageKey() {
            return messageKey;
        }

        public String getMessageKeyEscaped() {
            return TextUtils.htmlEncode(messageKey);
        }

        public void setMessageKey(String messageKey) {
            this.messageKey = messageKey;
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

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            try {
                json.put("type", type.toString());
                json.put("messageKey", messageKey);
                json.put("field", field);
                json.put("parms", Lists.newArrayList(params));
                return json;
            } catch (JSONException e) {
                throw new RuntimeException("This is not possible.  Famous last words??", e);
            }
        }
    }
}
