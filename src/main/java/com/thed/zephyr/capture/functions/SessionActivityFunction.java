package com.thed.zephyr.capture.functions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.thed.zephyr.capture.model.IssueAttachmentSessionActivity;
import com.thed.zephyr.capture.model.IssueRaisedSessionActivity;
import com.thed.zephyr.capture.model.IssueUnraisedSessionActivity;
import com.thed.zephyr.capture.model.SessionActivity;
import com.thed.zephyr.capture.model.jira.CaptureIssue;
import com.thed.zephyr.capture.service.jira.IssueService;

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
	
	public SessionActivityFunction(IssueService issueService) {
		this.issueService = issueService;
	}

	@Override
	public Object apply(SessionActivity sessionActivity) {
		CaptureIssue captureIssue = null;
		Map<String, Object> finalSescionActivityMap = new HashMap<>();
		if(sessionActivity instanceof IssueRaisedSessionActivity) {
			IssueRaisedSessionActivity issueRaisedSessionActivity = (IssueRaisedSessionActivity)sessionActivity;
			captureIssue = issueService.getCaptureIssue(issueRaisedSessionActivity.getIssueId());
			addSessionActivityInToMap(finalSescionActivityMap, issueRaisedSessionActivity);
			addIssueInToMap(finalSescionActivityMap, captureIssue, issueRaisedSessionActivity.getIssueId());
			return finalSescionActivityMap;
		} else if(sessionActivity instanceof IssueUnraisedSessionActivity) {
			IssueUnraisedSessionActivity issueUnraisedSessionActivity = (IssueUnraisedSessionActivity)sessionActivity;
			captureIssue = issueService.getCaptureIssue(issueUnraisedSessionActivity.getIssueId());
			addSessionActivityInToMap(finalSescionActivityMap, issueUnraisedSessionActivity);
			addIssueInToMap(finalSescionActivityMap, captureIssue, issueUnraisedSessionActivity.getIssueId());
			return finalSescionActivityMap;
		} else if(sessionActivity instanceof IssueAttachmentSessionActivity) {
			IssueAttachmentSessionActivity issueAttachmentSessionActivity = (IssueAttachmentSessionActivity)sessionActivity;
			captureIssue = issueService.getCaptureIssue(issueAttachmentSessionActivity.getIssueId());
			addSessionActivityInToMap(finalSescionActivityMap, issueAttachmentSessionActivity);
			addIssueInToMap(finalSescionActivityMap, captureIssue, issueAttachmentSessionActivity.getIssueId());
			finalSescionActivityMap.put("finalSescionActivityMap", issueAttachmentSessionActivity.getAttachment());
			return finalSescionActivityMap;
		}
		return sessionActivity;
	}
	
	private void addSessionActivityInToMap(Map<String, Object> finalSescionActivityMap, SessionActivity sessionActivity) {
		finalSescionActivityMap.put("id", sessionActivity.getId());
		finalSescionActivityMap.put("sessionId", sessionActivity.getSessionId());
		finalSescionActivityMap.put("ctId", sessionActivity.getCtId());
		finalSescionActivityMap.put("timestamp", sessionActivity.getTimestamp());
		finalSescionActivityMap.put("user", sessionActivity.getUser());
		finalSescionActivityMap.put("clazz", sessionActivity.getClazz());
		finalSescionActivityMap.put("projectId", sessionActivity.getProjectId());
	}
	
	private void addIssueInToMap(Map<String, Object> finalSescionActivityMap, CaptureIssue issue, Long issuedId) {
		if(Objects.nonNull(issue)) {
			finalSescionActivityMap.put("issue", issue);
			return;
		}
		finalSescionActivityMap.put("issuedId", issuedId);
	}

}
