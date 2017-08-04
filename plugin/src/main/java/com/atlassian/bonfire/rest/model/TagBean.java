package com.atlassian.bonfire.rest.model;

import com.atlassian.excalibur.model.Tag;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean for returning a Note tags in remote resources
 *
 * @since v1.3
 */
@XmlRootElement(name = "tag")
public class TagBean {
    /**
     * Unique identifier for this note tag
     */
    @XmlElement
    private Long id;

    /**
     * The name of this tag
     */
    @XmlElement
    private String name;


    public TagBean(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }

    public TagBean() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
