package com.atlassian.bonfire.model;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONObject;

import static org.apache.commons.lang.Validate.notNull;

/**
 * A variable is an alias of name javascript snippet, for extracting information from a page in Bonfire
 *
 * @since v1.8
 */
public class Variable {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_VALUE = "value";
    public static final String KEY_OWNER_NAME = "ownerName";

    private final Long id;
    private final String name;
    private final String value;
    private final String ownerName;

    public static Variable INVALID = new Variable(-1L, "", "", "");

    private Variable(Long id, String name, String value, String ownerName) {
        notNull(id);
        notNull(name);
        notNull(value);
        notNull(ownerName);

        this.id = id;
        this.name = name;
        this.value = value;
        this.ownerName = ownerName;
    }

    public static Variable create(Long id, String name, String value, ApplicationUser owner) {
        if (owner == null) {
            return INVALID;
        }
        return create(id, name, value, owner.getName());
    }

    public static Variable create(Long id, String name, String value, String ownerName) {
        if (id == null || name == null || value == null || ownerName == null) {
            return INVALID;
        }
        return new Variable(id, name, value, ownerName);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public static Variable create(JSONObject jsonObject) {
        return create(jsonObject.getLong(KEY_ID), jsonObject.getString(KEY_NAME), jsonObject.getString(KEY_VALUE), jsonObject.getString(KEY_OWNER_NAME));
    }

    public JSONObject toJSON() {
        return new JSONObject().put(KEY_ID, id)
                .put(KEY_NAME, name)
                .put(KEY_VALUE, value)
                .put(KEY_OWNER_NAME, ownerName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Variable variable = (Variable) o;

        if (id != null ? !id.equals(variable.id) : variable.id != null) return false;
        if (name != null ? !name.equals(variable.name) : variable.name != null) return false;
        if (ownerName != null ? !ownerName.equals(variable.ownerName) : variable.ownerName != null) return false;
        if (value != null ? !value.equals(variable.value) : variable.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (ownerName != null ? ownerName.hashCode() : 0);
        return result;
    }
}
