package com.thed.zephyr.capture.web.sessions;

import com.thed.zephyr.capture.properties.BonfireConstants;
import com.thed.zephyr.capture.service.web.SessionUIService;
import com.thed.zephyr.capture.web.model.CompleteSessionResult;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.service.BonfireUserSettingsService;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionController.SessionResult;
import com.atlassian.excalibur.service.controller.SessionController.UpdateResult;
import com.atlassian.excalibur.view.ActivityStreamFilterUI;
import com.atlassian.excalibur.view.NotesFilterStateUI;
import com.atlassian.excalibur.view.SessionUI;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.excalibur.web.util.QueryParamKit;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.annotation.Resource;

/**
 * The individual Session details page
 */
public class ViewSessionAction extends ExcaliburWebActionSupport {
    private static final String PERMISSION = "permission";

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = SessionUIService.SERVICE)
    private SessionUIService sessionUIService;

    @Resource(name = BonfireUserSettingsService.SERVICE)
    private BonfireUserSettingsService bonfireUserSettingsService;

    @Resource
    private WebResourceManager webResourceManager;

    private SessionUI sessionui;
    private String updatedNoteId = "";
    private String newNote = "";
    private String focusTo = "";
    private boolean isCompleted = false;
    // Query params
    private Long testSessionId;
    private String origin = "";

    // Filter parameters
    private NotesFilterStateUI notesFilterStateUI;
    private CompleteSessionResult completeSessionResult;

    private ErrorCollection completeErrorCollection = new ErrorCollection();

    public String doView() {
        String errorRedirect = getErrorRedirect(false, true, BonfireConstants.SESSION_PAGE + testSessionId);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        ApplicationUser user = getLoggedInApplicationUser();

        // Resources
        webResourceManager.requireResourcesForContext("bf-context");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-shared");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-test-session-view");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-editable-notes");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-test-session-dialog");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-issuecollector");
        webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-jquery-fancybox");

        if (bonfireUserSettingsService.showExtensionCallout(user)) {
            webResourceManager.requireResource("com.atlassian.bonfire.plugin:bonfire-extension-callout-resources");
        }

        setReturnUrl(BonfireConstants.SESSION_PAGE + testSessionId);
        notesFilterStateUI = new NotesFilterStateUI(request, response, getReturnUrl());

        SessionUIService.SessionUIResult result = sessionUIService.getSessionUI(
                user,
                testSessionId,
                new ActivityStreamFilterUI(notesFilterStateUI),
                JSONKit.get(QueryParamKit.getJSON(request), "bonfireLastScreen"));

        if (result.isPermissionViolation()) {
            return PERMISSION;
        }
        if (!result.isValid()) {
            getErrorCollection().addAllErrors(result.getErrorCollection());
            return ERROR;
        }
        sessionui = result.getReturnedValue();

        return SUCCESS;
    }

    /**
     * TODO Move complete methods elsewhere.
     */
    @RequiresXsrfCheck
    public String doComplete() {
        ApplicationUser user = getLoggedInApplicationUser();
        notesFilterStateUI = new NotesFilterStateUI(request, response, getReturnUrl());
        SessionUIService.SessionUIResult result = sessionUIService.getSessionUI(
                user,
                testSessionId,
                new ActivityStreamFilterUI(notesFilterStateUI),
                JSONKit.get(QueryParamKit.getJSON(request), "bonfireLastScreen"));

        if (!result.isValid()) {
            getErrorCollection().addAllErrors(result.getErrorCollection());
            return ERROR;
        }
        sessionui = result.getReturnedValue();

        return SUCCESS;
    }

    public boolean checkIssueLink(String id) {
        if (completeSessionResult != null) {
            Iterable<String> issuesToLink = completeSessionResult.getIssuesToLink();
            if (issuesToLink != null) {
                for (String s : issuesToLink) {
                    if (s.equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isLogTimeChecked() {
        if (completeSessionResult != null) {
            return completeSessionResult.isDoLogTime();
        }
        return false;
    }

    @RequiresXsrfCheck
    public String doJoinSession() {
        String errorRedirect = getErrorRedirect(false, true);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        UpdateResult result = sessionController.validateJoinSession(request.getParameter("testSessionId"), getLoggedInApplicationUser());

        return doUpdate(result);
    }

    @RequiresXsrfCheck
    public String doLeaveSession() {
        String errorRedirect = getErrorRedirect(false, true);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        UpdateResult result = sessionController.validateLeaveSession(request.getParameter("testSessionId"), getLoggedInApplicationUser());

        return doUpdate(result);
    }

    private String doUpdate(UpdateResult result) {
        if (!result.isValid()) {
            getErrorCollection().addAllErrors(result.getErrorCollection());
            return ERROR;
        }
        SessionResult updateResult = sessionController.update(result);

        return redirectTo(BonfireConstants.SESSION_PAGE + updateResult.getSession().getId());
    }

    // Not nice but the activity streams need it
    public String getIconUrl(Issue i) {
        return excaliburWebUtil.getFullIconUrl(i);
    }

    public ErrorCollection getCompleteErrorCollection() {
        return completeErrorCollection;
    }

    public SessionUI getSessionUI() {
        return sessionui;
    }


    public Long getTestSessionId() {
        return testSessionId;
    }

    public void setTestSessionId(Long testSessionId) {
        this.testSessionId = testSessionId;
    }

    public String getNewNote() {
        return newNote;
    }

    public void setNewNote(String newNote) {
        this.newNote = newNote;
    }

    public String getFocusTo() {
        return focusTo;
    }

    public void setFocusTo(String focusTo) {
        this.focusTo = focusTo;
    }

    public String getUpdatedNoteId() {
        return updatedNoteId;
    }

    public void setUpdatedNoteId(String updatedNoteId) {
        this.updatedNoteId = updatedNoteId;
    }

    public NotesFilterStateUI getNotesFilterStateUI() {
        return notesFilterStateUI;
    }

    public boolean isViewTestSession() {
        return true;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }
}
