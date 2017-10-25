package com.thed.zephyr.capture.model.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Masud on 9/5/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldOption {

    private String text;

    private String value;

    private List<FieldOption> children; // For cascading selects

    private boolean hasChildren; // for simplicity

    public FieldOption(String text, String value) {
        this.text = text;
        this.value = value;
        this.children = new ArrayList<>();// so it isn't null
        this.hasChildren = false;
    }

    public FieldOption(String text, String value, List<FieldOption> children) {
        this.text = text;
        this.value = value;
        this.children = children;
        this.hasChildren = !children.isEmpty(); // True if children is not empty
    }

    public FieldOption() {
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }

    public List<FieldOption> getChildren() {
        if (children != null){
            return children;
        }

        return new ArrayList<>();
    }

    public boolean isHasChildren() {
        return hasChildren;
    }
}
