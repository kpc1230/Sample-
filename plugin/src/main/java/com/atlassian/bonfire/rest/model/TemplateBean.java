package com.atlassian.bonfire.rest.model;

import com.atlassian.bonfire.model.FavouriteTemplate;
import com.atlassian.bonfire.model.Template;
import com.atlassian.bonfire.model.Variable;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.project.Project;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;

/**
 * Wrapper for Templates which allows jersey to spit out JSON.
 * Annoyingly forced to create this as we can't return JSONObject + have jersey know what to do
 *
 * @since v1.7
 */
public class TemplateBean {
    @XmlElement
    private Long id;
    @XmlElement
    private Long projectId;
    @XmlElement
    private String projectKey;
    @XmlElement
    private String projectIconUrl;
    @XmlElement
    private String name;
    @XmlElement
    private String ownerName;
    @XmlElement
    private String source;
    @XmlElement
    private String timeCreated;
    @XmlElement
    private String timeUpdated;
    @XmlElement
    private String timeVariablesUpdated;
    @XmlElement
    private String timeFavourited;
    @XmlElement
    private String shared;
    @XmlElement
    private boolean variablesChanged;
    @XmlElement
    private boolean favourited;
    @XmlElement
    private String ownerDisplayName;
    @XmlElement
    private Collection<VariableBean> variables;

    public TemplateBean() {
    }

    public TemplateBean(Template template, FavouriteTemplate favouriteData, ExcaliburWebUtil excaliburWebUtil) {
        this.id = template.getId();
        this.projectId = template.getProjectId();
        this.name = template.getName();
        this.ownerName = template.getOwnerName();
        this.ownerDisplayName = excaliburWebUtil.getDisplayName(this.ownerName);
        this.source = template.getJsonSource();
        this.timeCreated = template.getTimeCreated().toString(ISODateTimeFormat.dateTime());
        this.timeUpdated = template.getTimeUpdated().toString(ISODateTimeFormat.dateTime());
        this.timeVariablesUpdated = template.getTimeVariablesUpdated().toString(ISODateTimeFormat.dateTime());
        this.timeFavourited = favouriteData.getTimeFavourited().toString(ISODateTimeFormat.dateTime());
        this.variablesChanged = template.getTimeVariablesUpdated().isAfter(favouriteData.getTimeFavourited());
        this.favourited = favouriteData.isFavourited();
        this.variables = ImmutableList.copyOf(Collections2.transform(template.getVariables(), new Function<Variable, VariableBean>() {
            @Override
            public VariableBean apply(@Nullable Variable from) {
                return new VariableBean(from);
            }
        }));
        this.shared = String.valueOf(template.isShared());
        Project relatedProject = excaliburWebUtil.getProjectFromId(template.getProjectId());
        if (relatedProject != null) {
            this.projectKey = relatedProject.getKey();
            this.projectIconUrl = excaliburWebUtil.getProjectIconUrl(relatedProject);
        }
    }
}
