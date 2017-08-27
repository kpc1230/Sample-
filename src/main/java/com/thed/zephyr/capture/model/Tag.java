package com.thed.zephyr.capture.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by aliakseimatsarski on 8/15/17.
 */
public class Tag implements Comparable<Tag>{
    // Static tag types with special meanings
    public static final String QUESTION = "#?";
    public static final String FOLLOWUP = "#f";
    public static final String ASSUMPTION = "#!";
    public static final String IDEA = "#i";

    private String name;
    private Long id;

    public Tag() {
    }

    public Tag(Long id, String name) {
        this.name = name.toLowerCase();
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tag tag = (Tag) o;

        return id.equals(tag.id) && name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public JsonNode toJson() {
        ObjectMapper om = new ObjectMapper();
        JsonNode jsonNode = om.convertValue(this, JsonNode.class);

        return jsonNode;
    }

    @Override
    public int compareTo(Tag tag) {
        return  this.getName().compareTo(tag.getName());
    }
}
