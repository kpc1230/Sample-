package com.atlassian.bonfire.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since 1.2
 */
@XmlRootElement(name = "project")
public class ProjectBean {
    @XmlElement
    private String id;

    @XmlElement
    private String key;

    @XmlElement
    private String name;

    public ProjectBean(final Long id, final String key, final String name) {
        this.id = String.valueOf(id);
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProjectBean() {
    }
}
