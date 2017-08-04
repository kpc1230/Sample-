package com.atlassian.bonfire.model;

import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.Supplier;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

/**
 * A template is a shareable, repeatable state for the create issue form in Bonfire.
 *
 * @since v1.7
 */
public class Template {
    public static final String KEY_TEMPLATE_ID = "id";
    public static final String KEY_TEMPLATE_PROJECT_ID = "projectId";
    public static final String KEY_TEMPLATE_OWNER = "ownerName";
    public static final String KEY_TEMPLATE_TIME_TEMPLATE_CREATED = "timeCreated";
    public static final String KEY_TEMPLATE_TIME_TEMPLATE_UPDATED = "timeUpdated";
    public static final String KEY_TEMPLATE_TIME_VARS_UPDATED = "timeVariablesUpdated";
    public static final String KEY_TEMPLATE_SOURCE = "source";
    public static final String KEY_TEMPLATE_VARIABLES = "variables";
    public static final String KEY_TEMPLATE_SHARED = "shared";

    private final Long id;
    private final Long projectId;
    private final String ownerName;
    // Lazy reference to avoid parsing the String into a JSONObject until we have to.
    private final Supplier<JSONObject> jsonObject = new LazyReference<JSONObject>() {
        @Override
        protected JSONObject create() throws Exception {
            return new JSONObject(jsonSource);
        }
    };
    private final String jsonSource;
    private final List<Variable> variables;
    private final DateTime timeCreated;
    private final DateTime timeUpdated;
    private final DateTime timeVariablesUpdated;
    private final boolean shared;

    public static Template EMPTY = new Template(-1L, -1L, "", "", Collections.<Variable>emptyList(), new DateTime(0), new DateTime(0), new DateTime(0), false);

    public Template(Long id, Long projectId, String ownerName, String jsonSource, List<Variable> variables, DateTime timeCreated, DateTime timeUpdated, DateTime timeVariablesUpdated, boolean shared) {
        notNull(id);
        notNull(projectId);
        notNull(ownerName);
        notNull(jsonSource);
        notNull(timeCreated);
        notNull(timeUpdated);
        notNull(timeVariablesUpdated);
        notNull(shared);

        isTrue(timeUpdated.isAfter(timeCreated) || timeUpdated.equals(timeCreated));
        isTrue(timeUpdated.isAfter(timeVariablesUpdated) || timeUpdated.equals(timeVariablesUpdated));

        this.id = id;
        this.projectId = projectId;
        this.ownerName = ownerName;
        this.jsonSource = jsonSource;
        this.variables = ImmutableList.copyOf(variables);
        this.timeCreated = timeCreated;
        this.timeUpdated = timeUpdated;
        this.timeVariablesUpdated = timeVariablesUpdated;
        this.shared = shared;
    }

    public JSONObject toJSON() {
        try {
            // Get the user with the username
            ApplicationUser user = ComponentAccessor.getUserManager().getUserByName(ownerName);
            // Get the userkey from the user
            String userKey = user.getKey();
            return new JSONObject().put(KEY_TEMPLATE_ID, id)
                    .put(KEY_TEMPLATE_PROJECT_ID, projectId)
                    .put(KEY_TEMPLATE_OWNER, userKey)
                    .put(KEY_TEMPLATE_TIME_TEMPLATE_CREATED, timeCreated.toString(ISODateTimeFormat.dateTime()))
                    .put(KEY_TEMPLATE_TIME_TEMPLATE_UPDATED, timeUpdated.toString(ISODateTimeFormat.dateTime()))
                    .put(KEY_TEMPLATE_TIME_VARS_UPDATED, timeVariablesUpdated.toString(ISODateTimeFormat.dateTime()))
                    .put(KEY_TEMPLATE_SOURCE, jsonObject.get())
                    .put(KEY_TEMPLATE_VARIABLES, ImmutableList.copyOf(Collections2.<Variable, JSONObject>transform(variables, new Function<Variable, JSONObject>() {

                        @Override
                        public JSONObject apply(@Nullable Variable from) {
                            return from.toJSON();
                        }
                    })))
                    .put(KEY_TEMPLATE_SHARED, String.valueOf(shared));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public JSONObject getJsonObject() {
        return jsonObject.get();
    }

    public DateTime getTimeCreated() {
        return timeCreated;
    }

    public DateTime getTimeUpdated() {
        return timeUpdated;
    }

    public DateTime getTimeVariablesUpdated() {
        return timeVariablesUpdated;
    }

    public Long getId() {
        return id;
    }

    public boolean isShared() {
        return shared;
    }

    public String getJsonSource() {
        return jsonSource;
    }

    public List<Variable> getVariables() {
        return ImmutableList.copyOf(variables);
    }

    // Cheat method for getting the name out of the JSON source
    public String getName() {
        return JSONKit.getString(getJsonObject(), "name");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Template template = (Template) o;

        if (shared != template.shared) return false;
        if (id != null ? !id.equals(template.id) : template.id != null) return false;
        // We don't care if the jsonObjects are equal. Either way it will always return false because JsonObject returns true only if both are the
        // same instance. Since these are lazy references that are created when called, they will never be equal.
        if (jsonSource != null ? !jsonSource.equals(template.jsonSource) : template.jsonSource != null) return false;
        if (ownerName != null ? !ownerName.equals(template.ownerName) : template.ownerName != null) return false;
        if (projectId != null ? !projectId.equals(template.projectId) : template.projectId != null) return false;
        if (timeCreated != null ? !timeCreated.equals(template.timeCreated) : template.timeCreated != null)
            return false;
        if (timeUpdated != null ? !timeUpdated.equals(template.timeUpdated) : template.timeUpdated != null)
            return false;
        if (timeVariablesUpdated != null ? !timeVariablesUpdated.equals(template.timeVariablesUpdated) : template.timeVariablesUpdated != null)
            return false;
        if (variables != null ? !variables.equals(template.variables) : template.variables != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (ownerName != null ? ownerName.hashCode() : 0);
        result = 31 * result + (jsonSource != null ? jsonSource.hashCode() : 0);
        result = 31 * result + (variables != null ? variables.hashCode() : 0);
        result = 31 * result + (timeCreated != null ? timeCreated.hashCode() : 0);
        result = 31 * result + (timeUpdated != null ? timeUpdated.hashCode() : 0);
        result = 31 * result + (timeVariablesUpdated != null ? timeVariablesUpdated.hashCode() : 0);
        result = 31 * result + (shared ? 1 : 0);
        return result;
    }
}
