package com.thed.zephyr.capture.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.service.jira.IssueService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.WikiParser;
import emoji4j.EmojiUtils;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Class converts the session activity which has issue id to map in order
 * to have issue related information.
 * 
 * @author manjunath
 * @see java.util.function.Function
 *
 */
public class SessionActivityFunction implements Function<SessionActivity, Object> {
	
	private IssueService issueService;
	private WikiParser wikiParser;
	
	public SessionActivityFunction(IssueService issueService, WikiParser wikiParser) {
		this.issueService = issueService;
		this.wikiParser = wikiParser;
	}

	@Override
	public Object apply(SessionActivity sessionActivity) {
		CaptureIssue captureIssue = null;
		Map<String, Object> finalSescionActivityMap = new HashMap<>();
		if(sessionActivity instanceof IssueRaisedSessionActivity) {
			IssueRaisedSessionActivity issueRaisedSessionActivity = (IssueRaisedSessionActivity)sessionActivity;
			captureIssue = issueService.getCaptureIssue(String.valueOf(issueRaisedSessionActivity.getIssueId()));
			addSessionActivityInToMap(finalSescionActivityMap, issueRaisedSessionActivity);
			addIssueInToMap(finalSescionActivityMap, captureIssue, issueRaisedSessionActivity.getIssueId());
			return finalSescionActivityMap;
		} else if(sessionActivity instanceof IssueUnraisedSessionActivity) {
			IssueUnraisedSessionActivity issueUnraisedSessionActivity = (IssueUnraisedSessionActivity)sessionActivity;
			captureIssue = issueService.getCaptureIssue(String.valueOf(issueUnraisedSessionActivity.getIssueId()));
			addSessionActivityInToMap(finalSescionActivityMap, issueUnraisedSessionActivity);
			addIssueInToMap(finalSescionActivityMap, captureIssue, issueUnraisedSessionActivity.getIssueId());
			return finalSescionActivityMap;
		} else if(sessionActivity instanceof IssueAttachmentSessionActivity) {
			IssueAttachmentSessionActivity issueAttachmentSessionActivity = (IssueAttachmentSessionActivity)sessionActivity;
			captureIssue = issueService.getCaptureIssue(String.valueOf(issueAttachmentSessionActivity.getIssueId()));
			addSessionActivityInToMap(finalSescionActivityMap, issueAttachmentSessionActivity);
			addIssueInToMap(finalSescionActivityMap, captureIssue, issueAttachmentSessionActivity.getIssueId());
			finalSescionActivityMap.put("attachment", issueAttachmentSessionActivity.getAttachment());
			return finalSescionActivityMap;
		} else if(sessionActivity instanceof NoteSessionActivity){
			NoteSessionActivity noteSessionActivity = (NoteSessionActivity) sessionActivity;
			String noteData = noteSessionActivity.getNoteData();
			if(noteData != null){
				String wikify = wikiParser.parseWiki(noteData, ApplicationConstants.HTML);
				String noteHtmlData = EmojiUtils.emojify(StringEscapeUtils.unescapeHtml(wikify));
				noteSessionActivity.setNoteData(noteHtmlData);
			}
			addNoteActivityInToMap(finalSescionActivityMap, noteSessionActivity);
			return finalSescionActivityMap;
		}

		return sessionActivity;
	}

	@SuppressWarnings("unchecked")
	private void addNoteActivityInToMap(Map<String, Object> finalSessionActivityMap, NoteSessionActivity noteSessionActivity) {
		ObjectMapper m = new ObjectMapper();
		finalSessionActivityMap.putAll(m.convertValue(noteSessionActivity, Map.class));
		String rawNoteData = noteSessionActivity.getNoteData();
		finalSessionActivityMap.put("rawNoteData", rawNoteData);
		finalSessionActivityMap.put("noteData", CaptureUtil.createNoteData( rawNoteData));
	}

	private void addSessionActivityInToMap(Map<String, Object> finalSescionActivityMap, SessionActivity sessionActivity) {
		finalSescionActivityMap.put("id", sessionActivity.getId());
		finalSescionActivityMap.put("sessionId", sessionActivity.getSessionId());
		finalSescionActivityMap.put("ctId", sessionActivity.getCtId());
		finalSescionActivityMap.put("timestamp", sessionActivity.getTimestamp());
		finalSescionActivityMap.put("user", sessionActivity.getUser());
		finalSescionActivityMap.put("clazz", sessionActivity.getClazz());
		finalSescionActivityMap.put("projectId", sessionActivity.getProjectId());
		finalSescionActivityMap.put("displayName", sessionActivity.getDisplayName());
	}
	
	private void addIssueInToMap(Map<String, Object> finalSescionActivityMap, CaptureIssue issue, Long issuedId) {
		if(Objects.nonNull(issue)) {
			finalSescionActivityMap.put("issue", issue);
			return;
		}
		finalSescionActivityMap.put("issuedId", issuedId);
	}

}
