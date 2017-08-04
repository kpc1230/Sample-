package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since 2.7.0
 */
@XmlRootElement
public class VariablesBean {
    @XmlElement
    public Iterable<VariableBean> variables;

    public VariablesBean(Iterable<VariableBean> variables) {
        this.variables = variables;
    }
}
