package com.atlassian.bonfire.model;

import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;

/**
 * IndexedTemplate stores some of the data from Templates such that it can be efficiently searched, cut and sliced
 *
 * @since v1.7
 */
public class IndexedTemplate {
    private final Long id;
    private final Long projectId;

    public IndexedTemplate(Long id, Long projectId) {
        this.id = id;
        this.projectId = projectId;
    }

    public IndexedTemplate(Template template) {
        this.id = template.getId();
        this.projectId = template.getProjectId();
    }

    public IndexedTemplate(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getLong("id");
            this.projectId = jsonObject.getLong("projectId");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject toJSON() {
        try {
            return new JSONObject().put("id", id)
                    .put("projectId", projectId);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }
}
