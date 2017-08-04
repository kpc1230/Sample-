package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.events.RestCreateNoteEvent;
import com.atlassian.bonfire.events.RestJoinSessionEvent;
import com.atlassian.bonfire.events.RestLeaveSessionEvent;
import com.atlassian.bonfire.model.LightSession;
import com.atlassian.bonfire.properties.BonfireConstants;
import com.atlassian.bonfire.rest.model.*;
import com.atlassian.bonfire.rest.model.request.CompleteSessionRequest;
import com.atlassian.bonfire.rest.model.response.CompleteSessionResponse;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.bonfire.service.controller.BonfireCompleteSessionService;
import com.atlassian.bonfire.service.controller.BonfireCompleteSessionService.CompleteSessionResult;
import com.atlassian.bonfire.util.LightSessionUtils;
import com.atlassian.bonfire.util.SessionDisplayUtils;
import com.atlassian.bonfire.util.model.SessionDisplayHelper;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.excalibur.model.*;
import com.atlassian.excalibur.model.Session.Status;
import com.atlassian.excalibur.service.controller.NoteController;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionController.SessionResult;
import com.atlassian.excalibur.service.controller.SessionController.UpdateResult;
import com.atlassian.excalibur.service.controller.SessionControllerImpl;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * REST Resource for CRUD of Sessions
 */
@Path("/sessions")
public class SessionResource extends BonfireRestResource {
    private static final int SESSIONS_PER_REQUEST = 50;

    @Resource(name = SessionControllerImpl.SERVICE)
    private SessionController sessionController;

    @Resource(name = NoteController.SERVICE)
    private NoteController noteController;

    @Resource(name = BonfireCompleteSessionService.SERVICE)
    private BonfireCompleteSessionService bonfireCompleteSessionService;

    @Resource(name = LightSessionUtils.SERVICE)
    private LightSessionUtils lightSessionUtils;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = SessionDisplayUtils.SERVICE)
    private SessionDisplayUtils sessionDisplayUtils;

    @JIRAResource
    private EventPublisher eventPublisher;

    @JIRAResource
    private ProjectManager jiraProjectManager;

    @Resource
    private ExcaliburWebUtil excaliburWebUtil;


    public SessionResource() {
        super(SessionResource.class);
    }

    /**
     * ******************************************************************************************
     * <p>
     * POST RESOURCES
     * </p>
     * *******************************************************************************************
     */

    @POST
    @Path("/{sessionId}/complete")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response completeSessionRequest(final @PathParam("sessionId") String id, final CompleteSessionRequest request) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                CompleteSessionResult result = bonfireCompleteSessionService.validateComplete(getLoggedInUser(), id, request);
                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }
                bonfireCompleteSessionService.complete(result);
                Session session = result.getSessionUpdateResult().getSession();
                return ok(new CompleteSessionResponse(session.getName(), BonfireConstants.SESSION_PAGE + session.getId().toString()));
            }
        });
    }

    @POST
    @Path("/{sessionId}/start")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response startSessionRequest(final @PathParam("sessionId") String id) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                SessionController.SessionResult result = sessionController.getSessionWithoutNotes(id);
                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }

                UpdateResult updateResult = sessionController.validateStartSession(getLoggedInUser(), result.getSession());
                if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }

                sessionController.update(updateResult);
                return noContent();
            }
        });
    }

    @POST
    @Path("/{sessionId}/pause")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response pauseSessionRequest(final @PathParam("sessionId") String id) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                SessionController.SessionResult result = sessionController.getSessionWithoutNotes(id);
                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }

                UpdateResult updateResult = sessionController.validatePauseSession(getLoggedInUser(), result.getSession());
                if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }

                sessionController.update(updateResult);
                return noContent();
            }
        });
    }

    @POST
    @Path("/{id}/note")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response addNoteRequest(final @PathParam("id") String id, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Validate the JSON object
                final JSONObject json;
                try {
                    json = new JSONObject(requestBody);
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");
                }

                try {
                    String noteDataRaw = json.getString("note");
                    NoteController.CreateResult createResult = noteController.validateCreate(id, jiraAuthenticationContext.getUser(),
                            noteDataRaw);
                    if (!createResult.isValid()) {
                        return badRequest(createResult.getErrorCollection());
                    }
                    NoteController.NoteResult noteResult = noteController.create(createResult);
                    eventPublisher.publish(new RestCreateNoteEvent(jiraAuthenticationContext.getUser(), noteResult.getSession(), noteResult
                            .getNote()));
                    return ok(new NoteBean(noteResult.getNote(), excaliburWebUtil, bonfirePermissionService.canEditNote(getLoggedInUser(), noteResult
                            .getSession().getAssignee(), noteResult.getNote())));
                } catch (JSONException e) {
                    return badRequest("session.resource.json.incorrect");
                }
            }
        });
    }

    @POST
    @Path("/{sessionId}/participate")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response joinSessionRequest(final @PathParam("sessionId") String sessionId, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                UpdateResult validationResult = sessionController.validateJoinSession(sessionId, jiraAuthenticationContext.getUser());
                if (!validationResult.isValid()) {
                    return badRequest(validationResult.getErrorCollection());
                }
                SessionResult result = sessionController.update(validationResult);
                eventPublisher.publish(new RestJoinSessionEvent(getLoggedInUser(), result.getSession()));

                return noContent();
            }
        });
    }

    @POST
    @Path("/{sessionId}/additionalInfo")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response editAdditionalInfo(final @PathParam("sessionId") String sessionId, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                // Validate the JSON object
                final JSONObject json;
                try {
                    json = new JSONObject(requestBody);
                    String additionalInfoDataRaw = json.getString("additionalInfo");
                    UpdateResult validationResult = sessionController.validateEditAdditionalInfoSession(sessionId,
                            jiraAuthenticationContext.getUser(), additionalInfoDataRaw);
                    if (!validationResult.isValid()) {
                        return badRequest(validationResult.getErrorCollection());
                    }
                    SessionResult result = sessionController.update(validationResult);

                    return Response.ok(new AdditionalInfoResponse(result.getSession().getAdditionalInfo(), excaliburWebUtil)).build();
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");
                }
            }
        });
    }

    @POST
    @Path("/{sessionId}/shared")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response shareSessionRequest(final @PathParam("sessionId") String sessionId, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                UpdateResult validationResult = sessionController.validateShareSession(sessionId, jiraAuthenticationContext.getUser());
                if (!validationResult.isValid()) {
                    return badRequest(validationResult.getErrorCollection());
                }
                sessionController.update(validationResult);

                return noContent();
            }
        });
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response createSessionRequest(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Validate the JSON object
                final JSONObject sessionJson;
                try {
                    sessionJson = new JSONObject(requestBody);
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");
                }

                // Convert the JSON object to a Session, and add it
                try {
                    SessionController.CreateResult createResult = validateCreateSession(sessionJson);

                    if (!createResult.isValid()) {
                        return badRequest(createResult.getErrorCollection());
                    }

                    Session newSession = sessionController.create(createResult).getSession();

                    // Start the session if "startNow" is true
                    boolean startNow = sessionJson.has("startNow") ? Boolean.valueOf(sessionJson.getString("startNow")) : false;
                    if (startNow) {
                        UpdateResult updateResult = sessionController.validateStartSession(getLoggedInUser(), newSession);
                        if (!updateResult.isValid()) {
                            return badRequest(updateResult.getErrorCollection());
                        }

                        sessionController.update(updateResult);
                    }

                    return ok(createSessionBean(newSession));
                } catch (JSONException e) {
                    return badRequest("session.resource.json.incorrect");
                }
            }
        });
    }

    /**
     * ******************************************************************************************
     * <p>
     * PUT RESOURCES
     * </p>
     * *******************************************************************************************
     */

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateSessionRequest(final @PathParam("id") String id, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                // Validate the JSON object
                final JSONObject json;
                try {
                    json = new JSONObject(requestBody);
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");
                }
                SessionResult oldSessionResult = sessionController.getSessionWithoutNotes(id);
                if (!oldSessionResult.isValid()) {
                    return badRequest(oldSessionResult.getErrorCollection());
                }
                // Get fields from json Object and validate them
                UpdateResult result = validateEditSession(json, oldSessionResult.getSession());
                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }
                // All is good
                sessionController.update(result);

                return ok(new EmptyBean());
            }
        });
    }

    /**
     * ******************************************************************************************
     * <p>
     * DELETE RESOURCES
     * </p>
     * *******************************************************************************************
     */

    @DELETE
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response deleteSessionRequest(final @PathParam("id") String id) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                SessionController.DeleteResult deleteResult = sessionController.validateDelete(getLoggedInUser(), id);

                if (!deleteResult.isValid()) {
                    return badRequest(deleteResult.getErrorCollection());
                }

                // Perform the delete operation
                sessionController.delete(deleteResult);
                return ok(new EmptyBean());
            }
        });
    }

    @DELETE
    @Path("/{sessionId}/raisedin/{issueKey}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response unraiseIssueSessionRequest(final @PathParam("sessionId") String sessionId, final @PathParam("issueKey") String issueKey,
                                               final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                UpdateResult updateResult = sessionController.validateRemoveRaisedIssue(getLoggedInUser(), sessionId, issueKey);
                if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }
                sessionController.update(updateResult);

                return ok(new EmptyBean());
            }
        });
    }

    @DELETE
    @Path("/{sessionId}/participate")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response leaveSessionRequest(final @PathParam("sessionId") String sessionId, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                UpdateResult updateResult = sessionController.validateLeaveSession(sessionId, jiraAuthenticationContext.getUser());
                if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }
                SessionResult result = sessionController.update(updateResult);
                eventPublisher.publish(new RestLeaveSessionEvent(getLoggedInUser(), result.getSession()));

                return noContent();
            }
        });
    }

    @DELETE
    @Path("/{sessionId}/shared")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response unshareSessionRequest(final @PathParam("sessionId") String sessionId, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                UpdateResult updateResult = sessionController.validateUnshareSession(sessionId, jiraAuthenticationContext.getUser());
                if (!updateResult.isValid()) {
                    return badRequest(updateResult.getErrorCollection());
                }
                sessionController.update(updateResult);

                return noContent();
            }
        });
    }

    /**
     * ******************************************************************************************
     * <p>
     * GET RESOURCES
     * </p>
     * *******************************************************************************************
     */

    @GET
    @Path("/project")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSessionsForProject(final @QueryParam("pkey") String projectKey)// TODO Add pagination parameters to the query
    {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                Project project = jiraProjectManager.getProjectObjByKey(projectKey);
                if (project == null) {
                    return badRequest(i18n.getText("session.project.key.invalid", projectKey));
                }
                List<LightSession> sessions = sessionController.getLightSessionsForProject(project, 0, 50);
                List<SessionBean> projectSessions = Lists.newArrayList();
                for (LightSession s : sessions) {
                    projectSessions.add(createSessionBean(s));
                }
                return ok(new ProjectSessionsResponse(projectSessions));
            }
        });
    }

    @GET
    @Path("/user")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSessionsForExtension()// TODO Add pagination parameters to the query
    {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                // Filter the list if the status is present
                Predicate<IndexedSession> incompleteSessionsPredicate = new Predicate<IndexedSession>() {
                    public boolean apply(IndexedSession input) {
                        return !input.getStatus().equals(Session.Status.COMPLETED);
                    }
                };

                // TODO Add pagination parameters to the query
                List<LightSession> userSessions = sessionController.getLightSessionsForUserActiveFirst(
                        jiraAuthenticationContext.getUser(), incompleteSessionsPredicate, 0, 50);
                List<LightSession> userSharedSessions = sessionController.getSharedLightSessionsForUser(getLoggedInUser(), 0, 50);
                Long activeSessionId = sessionController.getActiveSessionId(getLoggedInUser());

                List<SessionBean> privateSessions = Lists.newArrayList();
                for (LightSession s : userSessions) {
                    boolean isActive = s.getId().equals(activeSessionId);
                    privateSessions.add(createSessionBeanForExtension(s, isActive, isActive));
                }
                List<SessionBean> sharedSessions = Lists.newArrayList();
                for (LightSession s : userSharedSessions) {
                    boolean isActive = s.getId().equals(activeSessionId);
                    sharedSessions.add(createSessionBeanForExtension(s, isActive, isActive));
                }
                SessionsBean sessionsBean = new SessionsBean(privateSessions, sharedSessions);

                return ok(toGenericEntity(sessionsBean));
            }
        });
    }

    @GET
    @Path("/filtered")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getFilteredSessions(final @QueryParam("userFilter") List<String> userNames,
                                        final @QueryParam("projectFilter") List<Long> projectIds, final @QueryParam("statusFilter") List<String> statuses,
                                        final @QueryParam("startAt") int startAt, final @QueryParam("sortField") String sortField,
                                        final @QueryParam("sortOrder") String sortOrder, final @QueryParam("searchTerm") String searchTerm,
                                        final @QueryParam("size") int size) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }
                ApplicationUser currentUser = getLoggedInUser();

                int requestSize = SESSIONS_PER_REQUEST;
                if (size > 0 && size < 50) {
                    requestSize = size;
                }
                // Load 1 more to check if there are more
                List<LightSession> sessions = sessionController.getAllVisibleLightSessionsFiltered(currentUser, startAt, requestSize + 1,
                        userNames, projectIds, translateStatuses(statuses), sortField, "ASC".equals(sortOrder), searchTerm);

                boolean hasMore = false;
                int nextStart = 0;
                if (sessions.size() > requestSize && sessions.remove(requestSize) != null) {
                    hasMore = true;
                    nextStart = startAt + requestSize;
                }

                int filteredCount = sessionController.getFilteredSessionCount(currentUser, userNames, projectIds, translateStatuses(statuses),
                        searchTerm);

                List<SessionBean> allSessions = Lists.newArrayList();
                for (LightSession s : sessions) {
                    allSessions.add(createSessionBean(s));
                }
                boolean hasAny = true;
                if (sessions.size() == 0) {
                    // If the session count is 0 then we don't hasAny sessions
                    hasAny = sessionController.getSessionCount(currentUser) != 0;
                }
                return ok(new FilteredSessionResponse(allSessions, hasAny, hasMore, nextStart, filteredCount));
            }
        });
    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getSingleSessionDetails(final @PathParam("id") String id) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                LightSession lightSession = sessionController.getLightSession(Long.valueOf(id));

                if (lightSession == null) {
                    return badRequest("session.resource.non.existant.session", id);
                }

                Long activeSessionId = sessionController.getActiveSessionId(getLoggedInUser());
                return ok(createSessionBeanForExtension(lightSession, lightSession.getId().equals(activeSessionId), true));
            }
        });
    }

    /**
     * ******************************************************************************************
     * <p>
     * PRIVATE METHODS
     * </p>
     * *******************************************************************************************
     */


    private String getOptionalValueFromJson(JSONObject sessionJson, String key) {
        return sessionJson.has(key) ? sessionJson.getString(key) : "";
    }

    // Some code duplication here...
    private SessionController.CreateResult validateCreateSession(JSONObject sessionJson) {
        List<String> relatedIssues = getRelatedIssues(sessionJson);

        ApplicationUser user = jiraAuthenticationContext.getUser();
        String name = sessionJson.getString("name");
        String projectKey = sessionJson.getString("projectKey");

        String assignee = getOptionalValueFromJson(sessionJson, "assignee");
        String additionalInfo = getOptionalValueFromJson(sessionJson, "additionalInfo");
        boolean shared = sessionJson.has("shared") ? Boolean.valueOf(sessionJson.getString("shared")) : false;
        String defaultTemplateId = getOptionalValueFromJson(sessionJson, "defaultTemplateId");

        SessionController.CreateResult createResult;

        createResult = sessionController.validateCreate(user, assignee, name, projectKey, relatedIssues, additionalInfo, shared, defaultTemplateId);
        return createResult;
    }

    private SessionController.UpdateResult validateEditSession(JSONObject sessionJson, Session oldSession) {
        List<String> relatedIssues = getRelatedIssues(sessionJson);

        ApplicationUser user = jiraAuthenticationContext.getUser();
        String name = sessionJson.getString("name");
        String additionalInfo = getOptionalValueFromJson(sessionJson, "additionalInfo");
        boolean shared = sessionJson.has("shared") ? Boolean.valueOf(sessionJson.getString("shared")) : false;
        String defaultTemplateId = getOptionalValueFromJson(sessionJson, "defaultTemplateId");

        SessionBuilder sb = new SessionBuilder(oldSession, excaliburWebUtil)
                .setName(name)
                .setAdditionalInfo(additionalInfo)
                .setShared(shared)
                .setDefaultTemplateId(defaultTemplateId);

        SessionController.UpdateResult updateResult;
        updateResult = sessionController.validateUpdate(user, sb.build(), relatedIssues);
        return updateResult;
    }

    private List<String> getRelatedIssues(JSONObject sessionJson) {
        List<String> relatedIssues = Lists.newArrayList();
        if (sessionJson.has("issueKey")) {
            JSONArray issueKeys = sessionJson.getJSONArray("issueKey");
            for (String issueKey : issueKeys.strings()) {
                relatedIssues.add(issueKey);
            }
        }
        return relatedIssues;
    }

    private SessionBean createSessionBean(Session session) {
        ApplicationUser user = getLoggedInUser();
        SessionDisplayHelper permissions = sessionDisplayUtils.getDisplayHelper(user, session);
        return new SessionBean(session, excaliburWebUtil, false, permissions);
    }

    private SessionBean createSessionBean(LightSession session) {
        ApplicationUser user = getLoggedInUser();
        Integer participantCount = lightSessionUtils.getParticipants(session).size();
        Integer activeParticipantCount = lightSessionUtils.getActiveParticipants(session).size();
        Integer noteCount = lightSessionUtils.getNoteCount(session);
        Integer issuesRaisedCount = lightSessionUtils.getIssuesRaisedCount(session, user);
        Duration loggedTime = lightSessionUtils.getTimeLogged(session);
        DateTime timeCreated = lightSessionUtils.getTimeCreated(session);
        SessionDisplayHelper permissions = sessionDisplayUtils.getDisplayHelper(user, session);
        return new DetailedSessionBean(session, excaliburWebUtil, noteCount, issuesRaisedCount, activeParticipantCount, participantCount, loggedTime,
                timeCreated, permissions);
    }

    private SessionBean createSessionBeanForExtension(LightSession session, boolean isActive, boolean sendFull) {
        ApplicationUser user = getLoggedInUser();
        SessionDisplayHelper permissions = sessionDisplayUtils.getDisplayHelper(user, session);
        List<Issue> relatedIssues = lightSessionUtils.getRelatedToIssues(session, user);
        List<Participant> participants = lightSessionUtils.getActiveParticipants(session);
        Integer activeParticipantCount = participants.size();

        if (Status.STARTED.equals(session.getStatus())) {
            // If started then add the assignee
            activeParticipantCount++;
        }
        List<IssueBean> relatedIssuesBeans = getIssueBeans(user, relatedIssues);
        if (sendFull) {
            List<Issue> issuesRaised = lightSessionUtils.getIssuesRaised(session, user);
            List<Note> notes = lightSessionUtils.getNotes(session);

            List<IssueBean> issuesRaisedBeans = getIssueBeans(user, issuesRaised);
            List<ParticipantBean> participantBeans = getParticipantBeans(participants);
            List<NoteBean> noteBeans = getNoteBeans(session.getAssignee(), notes);
            Duration estimatedTimeSpent = sessionController.calculateEstimatedTimeSpentOnSession(session);
            return new FullSessionBean(session, excaliburWebUtil, isActive, relatedIssuesBeans, issuesRaisedBeans, participantBeans,
                    activeParticipantCount, noteBeans, permissions, estimatedTimeSpent);
        } else {
            Integer noteCount = lightSessionUtils.getNoteCount(session);
            Integer issuesRaisedCount = lightSessionUtils.getIssuesRaisedCount(session, user);
            return new ExtensionSpecificSessionBean(session, excaliburWebUtil, isActive, permissions, noteCount, issuesRaisedCount,
                    activeParticipantCount, relatedIssuesBeans);
        }
    }

    private List<IssueBean> getIssueBeans(ApplicationUser user, List<Issue> issues) {
        List<IssueBean> toReturn = Lists.newArrayList();
        for (Issue i : issues) {
            toReturn.add(new IssueBean(i, excaliburWebUtil));
        }
        return toReturn;
    }

    private List<ParticipantBean> getParticipantBeans(List<Participant> participants) {
        List<ParticipantBean> toReturn = Lists.newArrayList();
        for (Participant p : participants) {
            toReturn.add(new ParticipantBean(p));
        }
        return toReturn;
    }

    private List<NoteBean> getNoteBeans(ApplicationUser assignee, Collection<Note> notes) {
        List<NoteBean> toReturn = Lists.newArrayList();
        for (Note n : notes) {
            boolean canEdit = bonfirePermissionService.canEditNote(getLoggedInUser(), assignee, n);
            toReturn.add(new NoteBean(n, excaliburWebUtil, canEdit));
        }
        return toReturn;
    }

    private List<Status> translateStatuses(List<String> statusStrings) {
        List<Status> toReturn = Lists.newArrayList();
        if (statusStrings != null) {
            for (String s : statusStrings) {
                if (BonfireConstants.INCOMPLETE_STATUS.equals(s)) {
                    insertOnce(toReturn, Status.CREATED);
                    insertOnce(toReturn, Status.STARTED);
                    insertOnce(toReturn, Status.PAUSED);
                } else {
                    insertOnce(toReturn, Status.valueOf(s));
                }
            }
        }
        return toReturn;
    }

    private void insertOnce(List list, Object object) {
        if (!list.contains(object)) {
            list.add(object);
        }
    }

    private GenericEntity<SessionsBean> toGenericEntity(final SessionsBean sessionBeans) {
        return new GenericEntity<SessionsBean>(sessionBeans) {
        };
    }
}
