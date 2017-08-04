package com.atlassian.excalibur.web;

import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

import javax.annotation.Resource;

@SuppressWarnings("serial")
public class ExcaliburSessionStatusAction extends ExcaliburWebActionSupport {
    private static final String LOGIN_URL = "login.jsp?permissionViolation=true";

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    /**
     * Model for view *
     */
    // TODO Should this be a SessionUI instead?
    private Session session;
    private boolean isDialog = false;

    /**
     * Errors *
     */
    private ErrorCollection errorCollection = new ErrorCollection();

    /**
     * This is the action for activating a session in the session view
     */
    @RequiresXsrfCheck
    public String doActivateInSession() {
        String errorRedirect = getErrorRedirect(false, false);
        if (errorRedirect != null) {
            return errorRedirect;
        }

        String sessionIdRaw = request.getParameter("sessionId");
        SessionController.SessionResult sessionResult = sessionController.getSessionWithoutNotes(sessionIdRaw);

        if (!sessionResult.isValid()) {
            errorCollection.addAllErrors(sessionResult.getErrorCollection());
            return ERROR;
        }

        session = sessionResult.getSession();

        SessionController.UpdateResult updateResult;
        if (session.getStatus().equals(Session.Status.STARTED)) {
            updateResult = sessionController.validatePauseSession(getLoggedInApplicationUser(), session);
        } else {
            updateResult = sessionController.validateStartSession(getLoggedInApplicationUser(), session);
        }

        if (!updateResult.isValid()) {
            errorCollection.addAllErrors(updateResult.getErrorCollection());
            return ERROR;
        }

        sessionController.update(updateResult);

        isDialog = Boolean.valueOf(request.getParameter("isDialog"));
        if (isDialog) {
            return returnCompleteWithInlineRedirect(BonfireConstants.SESSION_PAGE + session.getId());
        } else {
            return redirectTo(BonfireConstants.SESSION_PAGE + session.getId());
        }
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setErrorCollection(ErrorCollection errorCollection) {
        this.errorCollection = errorCollection;
    }

    public ErrorCollection getErrorCollection() {
        return errorCollection;
    }

    public void setDialog(boolean isDialog) {
        this.isDialog = isDialog;
    }

    public boolean isDialog() {
        return isDialog;
    }
}
