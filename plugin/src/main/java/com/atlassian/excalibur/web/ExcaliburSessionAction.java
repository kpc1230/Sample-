package com.atlassian.excalibur.web;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.model.Template;
import com.atlassian.bonfire.predicates.ActiveParticipantPredicate;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.bonfire.service.controller.TemplateController;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.model.SessionBuilder;
import com.atlassian.excalibur.service.controller.NoteController;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionController.CloneResult;
import com.atlassian.excalibur.service.controller.SessionController.SessionResult;
import com.atlassian.excalibur.service.controller.SessionController.UpdateResult;
import com.atlassian.excalibur.service.controller.SessionControllerImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@SuppressWarnings("serial")
public class ExcaliburSessionAction extends ExcaliburWebActionSupport implements ViewlessAction {
    private static final String SELECTED_TAB_BONFIRE = "#selectedTab=com.atlassian.bonfire.plugin:capture-ts-project-tabpanel";

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = SessionControllerImpl.SERVICE)
    private SessionController sessionController;

    @Resource(name = TemplateController.SERVICE)
    private TemplateController templateController;

    @Resource(name = NoteController.SERVICE)
    private NoteController noteController;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @JIRAResource
    private IssueManager jiraIssueManager;

    @JIRAResource
    private ProjectManager jiraProjectManager;

    @JIRAResource
    private ProjectService jiraProjectService;

    // Things that are used for create and edit session
    private String name = "";
    private String additionalInfo = "";
    private String assignee = "";
    private boolean shared = false;
    private boolean hasActiveParticipants = false;
    private String defaultTemplateId;
    private List<String> issueKeys = Lists.newArrayList();

    // ProjectKey is passed in by the tab panel contexts
    private String projectKey;
    // RelatedProjectKey is passed in by a project picker
    private String relatedProjectKey;
    // ProjectId used for jira issue picker
    private Long projectId;
    // issueId passed in by the view issue page
    private Long issueId;
    // issueKey needed for the heading of the create session dialog
    private String issueKey;
    private String returnOnDeleteUrl;
    private boolean noRedirect;

    private String testSessionId;
    private String noteId;
    private String noteText;
    private List<Template> templateList = Lists.newArrayList();
    private List<Project> projectList = Lists.newArrayList();

    Session session;

    /*****************************************
     * Actions
     *****************************************/

    public String doView() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }
        Issue issue = jiraIssueManager.getIssueObject(issueId);
        if (issue != null) {
            issueKey = issue.getKey();
        }
        if (StringUtils.isBlank(projectKey)) {
            populateProjectList(getLoggedInApplicationUser());
        }
        populateTemplateList();

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doCreate() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        ApplicationUser user = getLoggedInApplicationUser();

        // If issueId is empty, then we are creating from the view issue page
        if (issueId != null) {
            Issue issue = jiraIssueManager.getIssueObject(issueId);
            if (issue != null) {
                issueKey = issue.getKey();
                issueKeys.add(issue.getKey());
                projectKey = issue.getProjectObject().getKey();
            } else {
                getErrorCollection().addError(i18n.getText("session.issue.invalid", String.valueOf(issueId)));
            }
        } else {
            if (StringUtils.isBlank(projectKey)) {
                // If we don't have a projectKey, then we get the value passed in by the project picker
                populateProjectList(user);
                projectKey = relatedProjectKey;
            }

            String[] keysFromRequest = request.getParameterValues("issueKey");
            if (keysFromRequest != null) {
                for (String s : keysFromRequest) {
                    issueKeys.add(s);
                }
            }
        }
        SessionController.CreateResult createResult = sessionController.validateCreate(user, assignee, name, projectKey, issueKeys, additionalInfo,
                shared, defaultTemplateId);

        if (!createResult.isValid() || hasAnyErrors()) {
            getErrorCollection().addAllErrors(createResult.getErrorCollection());
            populateTemplateList();
            return ERROR;
        }

        Session newSession = sessionController.create(createResult).getSession();
        String sessionUrl = request.getContextPath() + BonfireConstants.SESSION_PAGE + newSession.getId().toString();

        addClientMessageToResponse(getText("session.create.client.message.pre", sessionUrl),
                getText("session.create.client.message.post"));

        return returnComplete();
    }

    public String doEdit() {
        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(testSessionId);

        if (!sessionResult.isValid()) {
            getErrorCollection().addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }

        this.session = sessionResult.getSession();

        setHasActiveParticipants(session);
        this.name = session.getName();
        this.additionalInfo = session.getAdditionalInfo();
        this.shared = session.isShared();
        this.projectId = session.getRelatedProject().getId();
        this.projectKey = session.getRelatedProject().getKey();
        this.defaultTemplateId = session.getDefaultTemplateId();
        for (Issue i : session.getRelatedIssues()) {
            if (bonfirePermissionService.canSeeIssue(getLoggedInApplicationUser(), i)) {
                this.issueKeys.add(i.getKey());
            }
        }
        populateTemplateList();

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doEditPost() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        ApplicationUser currentUser = getLoggedInApplicationUser();
        populateTemplateList();

        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(testSessionId);

        if (!sessionResult.isValid()) {
            getErrorCollection().addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }

        String[] keysFromRequest = request.getParameterValues("issueKey");
        if (keysFromRequest != null) {
            for (String s : keysFromRequest) {
                issueKeys.add(s);
            }
        }
        session = sessionResult.getSession();

        SessionBuilder sb = new SessionBuilder(session, getUtil())
                .setName(name)
                .setAdditionalInfo(additionalInfo)
                .setShared(shared)
                .setDefaultTemplateId(defaultTemplateId);

        SessionController.UpdateResult updateResult = sessionController.validateUpdate(currentUser, sb.build(), issueKeys);

        if (!updateResult.isValid()) {
            getErrorCollection().addAllErrors(updateResult.getErrorCollection());
            return ERROR;
        }

        sessionController.update(updateResult);

        return returnComplete();
    }

    public String doClone() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        LightSession ls = sessionController.getLightSession(testSessionId);
        if (ls == null) {
            getErrorCollection().addError(i18n.getText("session.invalid.long"));
            return ERROR;
        } else {
            name = i18n.getText("session.clone.prefix") + ls.getName();
        }
        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doClonePost() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }
        CloneResult result = sessionController.validateClone(testSessionId, name, getLoggedInApplicationUser());
        if (!result.isValid()) {
            getErrorCollection().addAllErrors(result.getErrorCollection());
            return ERROR;
        }
        Session newSession = sessionController.clone(result).getSession();

        String sessionUrl = request.getContextPath() + BonfireConstants.SESSION_PAGE + newSession.getId().toString();
        addClientMessageToResponse(getText("session.create.client.message.pre", sessionUrl),
                getText("session.create.client.message.post"));

        return returnComplete();
    }

    public String doAssign() {
        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(testSessionId);

        if (!sessionResult.isValid()) {
            getErrorCollection().addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }

        session = sessionResult.getSession();
        setHasActiveParticipants(session);

        assignee = session.getAssignee().getName();
        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAssignPost() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        SessionController.UpdateResult updateResult = sessionController.validateAssignSession(testSessionId, getLoggedInApplicationUser(), assignee);

        if (!updateResult.isValid()) {
            getErrorCollection().addAllErrors(updateResult.getErrorCollection());
            return ERROR;
        }

        sessionController.update(updateResult);

        return returnComplete();
    }

    public String doConfirm() {
        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(testSessionId);

        if (!sessionResult.isValid()) {
            getErrorCollection().addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }

        session = sessionResult.getSession();
        setHasActiveParticipants(session);

        name = session.getName();

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doUnshare() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        UpdateResult sessionResult = sessionController.validateUnshareSession(testSessionId, getLoggedInApplicationUser());
        if (!sessionResult.isValid()) {
            getErrorCollection().addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }

        sessionController.update(sessionResult);

        return returnComplete();
    }

    @RequiresXsrfCheck
    public String doDelete() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        SessionController.DeleteResult deleteResult = sessionController.validateDelete(getLoggedInApplicationUser(), testSessionId);

        if (!deleteResult.isValid()) {
            getErrorCollection().addAllErrors(deleteResult.getErrorCollection());
            return ERROR;
        }

        // Perform the delete operation
        SessionResult result = sessionController.delete(deleteResult);

        if (noRedirect) {
            return returnComplete();
        }
        if (StringUtils.isEmpty(returnOnDeleteUrl)) {
            Project project = result.getSession().getRelatedProject();
            return returnCompleteWithInlineRedirect("/browse/" + project.getKey() + SELECTED_TAB_BONFIRE);
        } else {
            return returnCompleteWithInlineRedirect(returnOnDeleteUrl);
        }
    }

    @RequiresXsrfCheck
    public String doDeleteNote() {
        String errorRedirect = getErrorRedirect(false, true);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        final String errorReturnUrl = getReturnUrl() + "#session-note-" + noteId;
        ApplicationUser currentUser = getLoggedInApplicationUser();

        NoteController.DeleteResult deleteResult = noteController.validateDelete(currentUser, noteId);

        if (!deleteResult.isValid()) {
            return redirectTo(errorReturnUrl);
        }

        noteController.delete(deleteResult);

        return redirectTo(getReturnUrl());
    }

    public String doAddRaise() {
        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(testSessionId);

        if (!sessionResult.isValid()) {
            getErrorCollection().addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }

        this.session = sessionResult.getSession();

        this.projectId = session.getRelatedProject().getId();
        this.projectKey = session.getRelatedProject().getKey();
        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAddRaisePost() {
        String errorRedirect = getErrorRedirect(true, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(testSessionId);
        if (!sessionResult.isValid()) {
            getErrorCollection().addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }
        this.session = sessionResult.getSession();
        this.projectId = session.getRelatedProject().getId();
        this.projectKey = session.getRelatedProject().getKey();

        String[] keysFromRequest = request.getParameterValues("issueKey");
        if (keysFromRequest != null) {
            for (String s : keysFromRequest) {
                if (!issueKeys.contains(s)) {
                    issueKeys.add(s);
                }
            }
        }
        UpdateResult result = sessionController.validateAddRaisedIssues(getLoggedInApplicationUser(), session, issueKeys);
        if (!result.isValid()) {
            getErrorCollection().addAllErrors(result.getErrorCollection());
            return ERROR;
        }
        sessionController.update(result);

        return returnComplete();
    }

    /*****************************************
     * Public Util Methods (Shorthands for the vms)
     *****************************************/

    public String getPostContext() {
        if (issueId != null) {
            return "issueId=" + issueId + "&issueKey=" + issueKey;
        } else if (StringUtils.isNotBlank(projectKey) && projectId != null) {
            return "projectKey=" + projectKey + "&projectId=" + projectId;
        }
        return "";
    }

    private void setHasActiveParticipants(Session session) {
        this.hasActiveParticipants = Iterables.any(session.getParticipants(), new ActiveParticipantPredicate());
    }

    private void populateTemplateList() {
        if (StringUtils.isNotBlank(projectKey)) {
            Project project = jiraProjectManager.getProjectObjByKey(projectKey);
            if (project != null) {
                templateList = templateController.loadSharedTemplatesForProject(project);
            }
        } else if (StringUtils.isNotBlank(issueKey)) {
            Issue issue = jiraIssueManager.getIssueObject(issueKey);
            if (issue != null) {
                templateList = templateController.loadSharedTemplatesForProject(issue.getProjectObject());
            }
        }
    }

    private void populateProjectList(ApplicationUser user) {
        if (StringUtils.isBlank(projectKey)) {
            final ServiceOutcome<List<Project>> outcome = jiraProjectService.getAllProjects(user);
            if (outcome.isValid()) {
                for (Project p : outcome.getReturnedValue()) {
                    projectList.add(p);
                }
            }
        }
    }

    /*****************************************
     * Getters n Setters
     *****************************************/

    public boolean hasIssueKey() {
        return issueId != null;
    }

    public Session getSession() {
        return session;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setTestSessionId(String testSessionId) {
        this.testSessionId = testSessionId;
    }

    public String getTestSessionId() {
        return testSessionId;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setReturnOnDeleteUrl(String returnOnDeleteUrl) {
        this.returnOnDeleteUrl = returnOnDeleteUrl;
    }

    public String getReturnOnDeleteUrl() {
        return returnOnDeleteUrl;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean isShared() {
        return shared;
    }

    public int getNameMaxLength() {
        return BonfireConstants.SESSION_NAME_LENGTH_LIMIT;
    }

    public boolean isHasActiveParticipants() {
        return hasActiveParticipants;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setIssueKeys(List<String> issueKeys) {
        this.issueKeys = issueKeys;
    }

    public List<String> getIssueKeys() {
        return issueKeys;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public List<Template> getTemplateList() {
        return templateList;
    }

    public void setDefaultTemplateId(String defaultTemplateId) {
        this.defaultTemplateId = defaultTemplateId;
    }

    public String getDefaultTemplateId() {
        return defaultTemplateId;
    }

    public void setNoRedirect(boolean noRedirect) {
        this.noRedirect = noRedirect;
    }

    public boolean isNoRedirect() {
        return noRedirect;
    }

    public List<Project> getProjectList() {
        return projectList;
    }

    public void setRelatedProjectKey(String relatedProjectKey) {
        this.relatedProjectKey = relatedProjectKey;
    }

    public String getRelatedProjectKey() {
        return relatedProjectKey;
    }
}
