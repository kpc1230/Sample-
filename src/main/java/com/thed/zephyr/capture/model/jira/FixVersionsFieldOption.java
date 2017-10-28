package com.thed.zephyr.capture.model.jira;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FixVersionsFieldOption extends FieldOption {

    private Boolean released;

    public FixVersionsFieldOption(String text, String value, Boolean released) {
        super(text, value);
        this.released = released;
    }

    public FixVersionsFieldOption(String text, String value, List<FieldOption> children, Boolean released){
        super(text, value, children);
        this.released = released;
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }
}
