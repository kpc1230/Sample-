package com.thed.zephyr.capture.model;

import static org.apache.commons.lang.Validate.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 
 * @author Venkatareddy on 08/24/17.
 *
 */
@Document(indexName = "capture", type = "variable")
//@DynamoDBTable(tableName = ApplicationConstants.VARIABLE_TABLE_NAME)
public class Variable implements Comparable<Variable>{
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_VALUE = "value";
    public static final String KEY_OWNER_NAME = "ownerName";

    private String id;
    private String name;
    private String value;
    private String ownerName;
    private String clientKey;

    public static Variable INVALID = new Variable("-1", "", "", "", "");

    private Variable(String id, String name, String value, String ownerName, String clientKey) {
        notNull(id);
        notNull(name);
        notNull(value);
        notNull(ownerName);

        this.id = id;
        this.name = name;
        this.value = value;
        this.ownerName = ownerName;
        this.clientKey = clientKey;
    }

    public static Variable create(String id, String name, String value, String ownerName, String clientKey) {
        if (id == null || name == null || value == null || ownerName == null //TODO, check this || clientKey == null
        		) {
            return INVALID;
        }
        return new Variable(id, name, value, ownerName, clientKey);
    }

    public String getId() {
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
    
    public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getClientKey() {
		return clientKey;
	}

	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
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

    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this);
    }
    @Override
    public int compareTo(Variable var) {
        return  this.getName().compareTo(var.getName());
    }
}
