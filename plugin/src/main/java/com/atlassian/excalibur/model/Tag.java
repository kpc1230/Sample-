package com.atlassian.excalibur.model;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A note can have zero or more tags associated with it.  Tags are case INsenstiVe.
 *
 * @since v1.3
 */
public class Tag {
    // Static tag types with special meanings
    public static final String QUESTION = "#?";
    public static final String FOLLOWUP = "#f";
    public static final String ASSUMPTION = "#!";
    public static final String IDEA = "#i";

    private final String name;
    private final Long id;


    public Tag(Long id, String name) {
        this.name = name.toLowerCase();
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
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
}
