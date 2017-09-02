package com.thed.zephyr.capture.model;

import static org.apache.commons.lang.Validate.notNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 
 * @author Venkatareddy on 08/24/17.
 *
 */
public class Variable implements Comparable<Variable>{


    private String id;
    private String ctId;
    private String createdBy;
    private String name;
    private String value;

    public Variable() {
    }

    public Variable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCtId() {
        return ctId;
    }

    public void setCtId(String ctId) {
        this.ctId = ctId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Variable variable = (Variable) o;

        if (name != null ? !name.equals(variable.name) : variable.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
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

    public JsonNode toJson() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }
}

