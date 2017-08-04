package com.atlassian.bonfire.rest.model;

import com.atlassian.bonfire.model.Variable;

import javax.xml.bind.annotation.XmlElement;

/**
 * Wrapper for Variables which allows jersey to spit out JSON
 *
 * @since v1.8
 */
public class VariableBean {
    @XmlElement
    private Long id;
    @XmlElement
    private String name;
    @XmlElement
    private String value;
    @XmlElement
    private String ownerName;

    public VariableBean() {
        // jersey requires a public no arg constructor.
    }

    public VariableBean(Variable variable) {
        id = variable.getId();
        name = variable.getName();
        value = variable.getValue();
        ownerName = variable.getOwnerName();
    }
}
