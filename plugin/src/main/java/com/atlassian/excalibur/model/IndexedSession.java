package com.atlassian.excalibur.model;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * An IndexedSession
 * This is a smaller Session object which holds the fields we are interested in for a Session
 *
 * @since v1.3
 */
public final class IndexedSession {
    // Cheating to prevent an upgrade task to update the name of the key
    private static final String RELATED_ISSUES_JSON_INDEX = "issueId";

    private final Long id;
    private final String name;
    private final Long timeCreatedMillis;
    private final Long projectId;
    private final String projectName;
    private final List<Long> issueIds;
    // These two are actually keys for the creator and assignee.
    private final String creator;
    private final String assignee;

    private final String assigneeDisplayName;
    private final Session.Status status;
    private final boolean shared;

    public IndexedSession(Session session) {
        id = session.getId();
        name = session.getName();
        if (session.getTimeCreated() != null) {
            timeCreatedMillis = session.getTimeCreated().getMillis();
        } else {
            // Not yet started, so this is as good as any
            timeCreatedMillis = 0L;
        }
        Project project = session.getRelatedProject();
        projectId = (project != null) ? session.getRelatedProject().getId() : -1L;
        projectName = (project != null) ? project.getName() : "";
        List<Issue> issues = session.getRelatedIssues();
        issueIds = new ArrayList<Long>();
        if (issues != null) {
            for (Issue i : issues) {
                issueIds.add(i.getId());
            }
        }
        // use they keys, not the names
        String creatorKey = ApplicationUsers.getKeyFor(session.getCreator());
        creator = creatorKey;
        String assigneeKey = ApplicationUsers.getKeyFor(session.getAssignee());
        assignee = assigneeKey;
        assigneeDisplayName = session.getAssignee() != null ? session.getAssignee().getDisplayName() : "";
        status = session.getStatus();
        shared = session.isShared();
    }

    public IndexedSession(JSONObject jsonObject) throws JSONException {
        // Long -> JSON -> Integer if Long is small enough to fit into Integer
        id = jsonObject.getLong("id");
        name = jsonObject.getString("name");
        timeCreatedMillis = Long.valueOf(jsonObject.get("timeStarted").toString());
        projectId = jsonObject.getLong("projectId");
        projectName = jsonObject.getString("projectName");
        assigneeDisplayName = jsonObject.getString("assigneeDisplayName");

        issueIds = new ArrayList<Long>();
        String[] relatedIssueIds = jsonObject.get(RELATED_ISSUES_JSON_INDEX).toString().split(",");
        for (String s : relatedIssueIds) {
            if (!StringUtils.isEmpty(s)) {
                issueIds.add(Long.valueOf(s));
            }
        }

        creator = jsonObject.get("userName").toString();
        // BON-518 Adding assignee. If no assignee, default to assignee to creator.
        // This is here to avoid having an upgrade task until we move to Lucene.
        // And when we move to Lucene this class most likely will cease to exist.
        if (jsonObject.has("assignee")) {
            assignee = jsonObject.get("assignee").toString();
        } else {
            assignee = creator;
        }
        status = Session.Status.valueOf(jsonObject.get("status").toString());
        shared = jsonObject.has("shared") ? jsonObject.getBoolean("shared") : false;
    }

    public JSONObject marshal() throws JSONException {
        // Comma separated issue ids
        StringBuilder issueIndex = new StringBuilder();
        for (Long i : issueIds) {
            issueIndex.append(i).append(",");
        }
        return new JSONObject().put("id", id)
                .put("timeStarted", timeCreatedMillis)
                .put("projectId", projectId)
                .put(RELATED_ISSUES_JSON_INDEX, issueIndex)
                .put("userName", creator)
                .put("assignee", assignee)
                .put("status", status)
                .put("shared", shared)
                .put("name", name)
                .put("projectName", projectName)
                .put("assigneeDisplayName", assigneeDisplayName);
    }

    public Long getId() {
        return id;
    }

    public List<Long> getIssueIds() {
        return issueIds;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getCreator() {
        return creator;
    }

    public Long getTimeCreatedMillis() {
        return timeCreatedMillis;
    }

    public String getAssignee() {
        return assignee;
    }

    public Session.Status getStatus() {
        return status;
    }

    public boolean isShared() {
        return shared;
    }

    public String getAssigneeDisplayName() {
        return assigneeDisplayName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getName() {
        return name;
    }
}
