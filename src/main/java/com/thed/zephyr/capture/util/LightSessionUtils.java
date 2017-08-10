package com.thed.zephyr.capture.util;

import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.BonfireSessionCustomFieldService;
import com.thed.zephyr.capture.model.LightSession;
import com.thed.zephyr.capture.service.BonfireUnmarshalService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.model.Note;
import com.atlassian.excalibur.model.Participant;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.controller.NoteController;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.json.JSONObject;
import com.atlassian.query.Query;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.Validate.isTrue;

/**
 * This class contains handy methods to get additional details about a session from a LightSession object. This class should always aim to do it in a
 * performant way. Getting issues are done with JQL - which gives us issue permission checks for free so we can reduce the number of permission checks
 * we do as well
 *
 * @author ezhang
 */
@Service(LightSessionUtils.SERVICE)
public class LightSessionUtils {
    public static final String SERVICE = "bonfire-lightsessionutils";

    private static final String SESSION_RELATED_ISSUES = "relatedIssue";
    private static final String SESSION_NOTE_IDS = "sessionNoteIds";
    private static final String SESSION_PARTICIPANTS = "participants";
    private static final String SESSION_STARTED = "start";
    private static final String SESSION_LOGGED_TIME = "duration";
    private static final String SESSION_STATUS_HISTORY = "sessionStatusHistory";

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = BonfireUnmarshalService.SERVICE)
    private BonfireUnmarshalService bonfireUnmarshalService;

    @Resource(name = NoteController.SERVICE)
    private NoteController noteController;

    @JIRAResource
    private IssueManager jiraIssueManager;

    @JIRAResource
    private SearchService searchService;

    public Integer getNoteCount(LightSession lightSession) {
        String sessionNoteIdsRaw = ((String) lightSession.getRawData().get(SESSION_NOTE_IDS));
        String subRawString = sessionNoteIdsRaw.substring(1, sessionNoteIdsRaw.length() - 1);
        if (StringUtils.isNotBlank(subRawString)) {
            String[] sessionNoteIdArray = subRawString.split(",");
            return sessionNoteIdArray.length;
        }
        return 0;
    }

    public List<Participant> getParticipants(LightSession lightSession) {
        List<String> participantsJSON = (List<String>) lightSession.getRawData().get(SESSION_PARTICIPANTS);
        List<Participant> participants = new ArrayList<Participant>();
        if (participantsJSON != null) {
            for (String json : participantsJSON) {
                participants.add(bonfireUnmarshalService.getParticipantFromJSON(new JSONObject(json)));
            }
        }
        return participants;
    }

    public List<Participant> getActiveParticipants(LightSession lightSession) {
        List<String> participantsJSON = (List<String>) lightSession.getRawData().get(SESSION_PARTICIPANTS);
        List<Participant> participants = new ArrayList<Participant>();
        if (participantsJSON != null) {
            for (String json : participantsJSON) {
                Participant participant = bonfireUnmarshalService.getParticipantFromJSON(new JSONObject(json));
                if (!participant.hasLeft()) {
                    participants.add(participant);
                }
            }
        }
        return participants;
    }

    public List<Note> getNotes(LightSession lightSession) {
        String sessionNoteIdsRaw = ((String) lightSession.getRawData().get(SESSION_NOTE_IDS));
        if (StringUtils.isNotBlank(sessionNoteIdsRaw)) {
            String[] sessionNoteIdArray = sessionNoteIdsRaw.substring(1, sessionNoteIdsRaw.length() - 1).split(",");
            List<Long> sessionNoteIds = new ArrayList<Long>(sessionNoteIdArray.length);
            // Possible NumberFormatException here, but the marshaller doesn't mind either... so fix when it breaks for someone
            for (String sessionNoteId : sessionNoteIdArray) {
                if (!StringUtils.isEmpty(sessionNoteId)) {
                    sessionNoteIds.add(Long.valueOf(sessionNoteId.trim()));
                }
            }
            return Lists.newArrayList(noteController.getNoteIterable(sessionNoteIds));
        }
        return Lists.newArrayList();
    }

    public DateTime getTimeCreated(LightSession lightSession) {
        // Should do a null check and all that, but the marshaller doesn't seem to so for now we won't.
        return ISODateTimeFormat.dateTime().parseDateTime((String) lightSession.getRawData().get(SESSION_STARTED));
    }

    public Duration getTimeLogged(LightSession lightSession) {
        if (lightSession.getRawData().containsKey(SESSION_LOGGED_TIME)) {
            return new Duration(new Long((String) lightSession.getRawData().get(SESSION_LOGGED_TIME)));
        }
        return null;
    }

    public List<String> getRelatedIssueKeys(LightSession lightSession) {
        List<String> toReturn = Lists.newArrayList();
        if (lightSession.getRawData().containsKey(SESSION_RELATED_ISSUES)) {
            String allRelatedIssues = (String) lightSession.getRawData().get(SESSION_RELATED_ISSUES);
            String[] relatedIssueIds = allRelatedIssues.split(",");
            for (String s : relatedIssueIds) {
                if (StringUtils.isNotBlank(s)) {
                    Issue relatedIssue = jiraIssueManager.getIssueObject(Long.valueOf(s));
                    if (relatedIssue != null) {
                        toReturn.add(relatedIssue.getKey());
                    }
                }
            }
        }
        return toReturn;
    }

    public Map<DateTime, Session.Status> getSessionStatusHistory(LightSession lightSession) {
        String sessionStatusHistorySerialised = (String) lightSession.getRawData().get(SESSION_STATUS_HISTORY);

        Map<DateTime, Session.Status> sessionStatusHistory = new HashMap<DateTime, Session.Status>();

        if (!StringUtils.isEmpty(sessionStatusHistorySerialised)) {
            String[] sessionStatusHistoryArray = sessionStatusHistorySerialised.split(",");
            // Should have pairs
            isTrue(sessionStatusHistoryArray.length % 2 == 0);
            // TODO De-uglify
            for (int i = 0; i < sessionStatusHistoryArray.length; i += 2) {
                try {
                    sessionStatusHistory.put(ISODateTimeFormat.dateTime().parseDateTime(sessionStatusHistoryArray[i]),
                            Session.Status.valueOf(sessionStatusHistoryArray[i + 1]));
                } catch (IllegalArgumentException e) {
                    // Status is bad, so lets just not add this one
                    // TODO log it
                }
            }
        }
        return sessionStatusHistory;
    }

    /******************
     * JQL STUFF BELOW
     ******************/

    public Integer getIssuesRaisedCount(LightSession lightSession, ApplicationUser user) {
        Query query = getRaisedInSessionQuery(lightSession);
        return searchForIssues(query, user).size();
    }

    public List<Issue> getIssuesRaised(LightSession lightSession, ApplicationUser user) {
        Query query = getRaisedInSessionQuery(lightSession);
        return searchForIssues(query, user);
    }

    public List<Issue> getRelatedToIssues(LightSession lightSession, ApplicationUser user) {
        Query query = getRelatedToSessionQuery(lightSession);
        return searchForIssues(query, user);
    }

    private List<Issue> searchForIssues(Query query, ApplicationUser user) {
        try {
            SearchResults result = searchService.search(user, query, PagerFilter.getUnlimitedFilter());
            return result.getIssues();
        } catch (SearchException se) {
            // Something went wrong with the search ...
            return Lists.newArrayList();
        }
    }

    private Query getRaisedInSessionQuery(LightSession lightSession) {
        CustomField raisedInField = bonfireSessionCustomFieldService.getRaisedInSessionCustomField();
        Query query = JqlQueryBuilder.newClauseBuilder().customField(raisedInField.getIdAsLong()).eq(lightSession.getId().toString()).buildQuery();
        return query;
    }

    private Query getRelatedToSessionQuery(LightSession lightSession) {
        CustomField relatedToField = bonfireMultiSessionCustomFieldService.getRelatedToSessionCustomField();
        Query query = JqlQueryBuilder.newClauseBuilder().customField(relatedToField.getIdAsLong()).eq(lightSession.getId().toString()).buildQuery();
        return query;
    }
}
