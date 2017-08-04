package com.atlassian.excalibur.tabpanels;

import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.bonfire.util.LightSessionUtils;
import com.atlassian.excalibur.model.FatNote;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Tag;
import com.atlassian.excalibur.service.controller.NoteController;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionControllerImpl;
import com.atlassian.excalibur.view.NotesFilterStateUI;
import com.atlassian.excalibur.view.PageContext;
import com.atlassian.excalibur.view.SessionsFilterStateUI;
import com.atlassian.excalibur.web.ExcaliburWebActionSupport;
import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.excalibur.web.util.QueryParamKit;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * Having an action simplies the .VM templates so we can call $action.xxxx instead of having to maintain a
 * separate version.  The action is just a container of variables in this case
 */
public class TabPanelAction extends ExcaliburWebActionSupport {
    private static final String BONFIRE_TAB_KEY = "com.atlassian.bonfire.plugin:capture-test-sessions";

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = SessionControllerImpl.SERVICE)
    private SessionController sessionController;

    @Resource(name = NoteController.SERVICE)
    private NoteController noteController;

    @Resource(name = LightSessionUtils.SERVICE)
    private LightSessionUtils lightSessionUtils;

    private NotesFilterStateUI notesFilterStateUI;
    private SessionsFilterStateUI sessionsFilterStateUI;
    private String updatedNoteId;
    private boolean sessionViewSelected;
    private Project project;

    private ApplicationUser user;

    private PageContext pageContext;
    private String paramJSON;
    private Issue issue;

    public TabPanelAction() {
    }

    @Override
    protected String doExecute() {
        // which view is selected
        boolean sessionViewSelected = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("sessionViewSelected"), "false"));
        boolean noteViewSelected = Boolean.valueOf(StringUtils.defaultIfEmpty(request.getParameter("noteViewSelected"), "false")) && !sessionViewSelected;
        if (!sessionViewSelected && !noteViewSelected) {
            sessionViewSelected = true;
        }

        this.sessionViewSelected = sessionViewSelected;

        this.updatedNoteId = defaultString(request.getParameter("updatedNoteId"));

        if (project != null) {
            pageContext = sessionViewSelected ? PageContext.ProjectTabPanelSessions : PageContext.ProjectTabPanelNotes;

            final String returnUrl = "/browse/" + project.getKey()
                    + "?sessionViewSelected=" + Boolean.toString(sessionViewSelected)
                    + "&noteViewSelected=" + Boolean.toString(noteViewSelected)
                    + "&selectedItem=" + BONFIRE_TAB_KEY;

            setReturnUrl(returnUrl);
            this.notesFilterStateUI = new NotesFilterStateUI(request, response, returnUrl);
            this.sessionsFilterStateUI = new SessionsFilterStateUI(request, response, returnUrl);
        }
        this.paramJSON = constructParamJSON();


        return SUCCESS;
    }

    List<LightSession> getSessions(final int startIndex, final int size) {
        return sessionController.getLightSessionsForProject(project, sessionsFilterStateUI.getSessionPredicate(), startIndex, size);
    }


    List<FatNote> getNotes(Integer startIndex, Integer size) {
        if (notesFilterStateUI.isNothing()) {
            return noteController.getNotesForProject(project, startIndex, size);
        } else {
            // Some filtering has been applied

            // Ok now work out what kind of query to do
            if (!notesFilterStateUI.isComplete() && !notesFilterStateUI.isIncomplete()) {
                return Collections.emptyList();
            }

            Set<String> tags = constructTagSet(notesFilterStateUI);
            // What this actually means is: if the filter says show all notes
            if (notesFilterStateUI.isComplete() && notesFilterStateUI.isIncomplete()) {
                return noteController.getNotesByProjectAndSetOfTags(project, tags, startIndex, size);
            } else {
                Note.Resolution res;
                if (notesFilterStateUI.isIncomplete())
                    res = Note.Resolution.INITIAL;
                else {
                    res = Note.Resolution.COMPLETED;
                }
                return noteController.getNotesByProjectAndResolutionAndSetOfTags(project, res, tags, startIndex, size);
            }
        }

    }

    private Set<String> constructTagSet(NotesFilterStateUI notesFilterStateUI) {
        Set<String> tags = new HashSet<String>();
        if (notesFilterStateUI.isQuestion()) {
            tags.add(Tag.QUESTION);
        }
        if (notesFilterStateUI.isFollowup()) {
            tags.add(Tag.FOLLOWUP);
        }
        if (notesFilterStateUI.isIdea()) {
            tags.add(Tag.IDEA);
        }
        if (notesFilterStateUI.isAssumption()) {
            tags.add(Tag.ASSUMPTION);
        }
        return tags;
    }

    Integer getPageNumber(HttpServletRequest request) {
        String pageRaw = request.getParameter("page");
        Integer pageNumber;
        try {
            pageNumber = Integer.valueOf(pageRaw);
        } catch (NumberFormatException e) {
            pageNumber = 0;
        }
        return pageNumber;
    }

    public int getRaisedIssueCount(LightSession session) {
        int count = lightSessionUtils.getIssuesRaisedCount(session, getLoggedInUser());
        return count;
    }

    public String getAvatarUrl(LightSession session) {
        return excaliburWebUtil.getLargeAvatarUrl(session.getAssignee());
    }

    public Duration getTimeLogged(LightSession session) {
        Duration timeLogged = lightSessionUtils.getTimeLogged(session);
        return timeLogged;
    }

    public DateTime getTimeCreated(LightSession session) {
        DateTime timeCreated = lightSessionUtils.getTimeCreated(session);
        return timeCreated;
    }

    public boolean canCreateSession() {
        ApplicationUser user = getLoggedInUser();
        if (user == null) {
            return false;
        }
        switch (pageContext) {
            case ProjectTabPanelNotes:
            case ProjectTabPanelSessions:
                return bonfirePermissionService.canCreateSession(user, project);
            default:
                throw new IllegalStateException("Bad programming juju");
        }
    }

    public String getNoSessionsMessage() {
        if (pageContext == PageContext.ProjectTabPanelSessions) {
            return getI18nHelper().getText("project.sessions.list.empty", project.getKey());
        } else {
            return StringUtils.EMPTY;
        }
    }

    private String constructParamJSON() {
        JSONObject topJSONObj = new JSONObject();
        JSONObject bonfireLastScreenJSON = new JSONObject();

        if (pageContext == PageContext.ProjectTabPanelSessions || pageContext == PageContext.ProjectTabPanelNotes) {
            JSONKit.put(bonfireLastScreenJSON, "i18nKey", "session.return.project");
            JSONKit.put(bonfireLastScreenJSON, "i18nParam", htmlEncode(project.getKey()));

        }
        JSONKit.put(bonfireLastScreenJSON, "URL", getReturnUrl());

        JSONKit.put(topJSONObj, "bonfireLastScreen", bonfireLastScreenJSON);

        return QueryParamKit.jsonToQueryParamString(topJSONObj);
    }

    public String getPrettyStatus(LightSession session) {
        return i18n.getText("session.status.pretty." + session.getStatus());
    }

    public String username() {
        return user.getName();
    }

    public ApplicationUser getUser() {
        return user;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public NotesFilterStateUI getNotesFilterStateUI() {
        return notesFilterStateUI;
    }


    public SessionsFilterStateUI getSessionsFilterStateUI() {
        return sessionsFilterStateUI;
    }

    public String getUpdatedNoteId() {
        return updatedNoteId;
    }

    public String getParamJSON() {
        return paramJSON;
    }

    public boolean isViewTestSession() {
        return false;
    }

    public boolean isSessionViewSelected() {
        return sessionViewSelected;
    }

    public boolean isNoteViewSelected() {
        return !sessionViewSelected;
    }

    // Mostly for GA
    public String getPageContext() {
        return pageContext.toString();
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    public Issue getIssue() {
        return issue;
    }

    public void sanityCheck(){
        if(isSessionViewSelected()) {
            if(pageContext != PageContext.ProjectTabPanelSessions) {
                throw new IllegalStateException("Bad programming juju");
            }
        } else if( pageContext != PageContext.ProjectTabPanelNotes) {
            throw new IllegalStateException("Bad programming juju");
        }
    }
}
