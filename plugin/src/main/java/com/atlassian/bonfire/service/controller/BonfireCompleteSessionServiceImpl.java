package com.atlassian.bonfire.service.controller;

import com.atlassian.bonfire.rest.model.request.CompleteSessionRequest;
import com.atlassian.bonfire.rest.model.request.CompleteSessionRequest.CompleteSessionIssueLinkRequest;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.controller.BonfireCompleteSessionService.CompleteSessionResult.CompleteSessionIssueLink;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.excalibur.model.Session;
import com.atlassian.excalibur.service.controller.SessionController;
import com.atlassian.excalibur.service.controller.SessionController.UpdateResult;
import com.atlassian.excalibur.web.util.ReflectionKit;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.JiraDurationUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Service(BonfireCompleteSessionServiceImpl.SERVICE)
public class BonfireCompleteSessionServiceImpl implements BonfireCompleteSessionService {

    @JIRAResource
    private IssueLinkManager jiraIssueLinkManager;

    @JIRAResource
    private IssueLinkTypeManager jiraIssueLinkTypeManager;

    @JIRAResource
    private JiraDurationUtils jiraDurationUtils;

    @JIRAResource
    private JiraAuthenticationContext jiraAuthenticationContext;

    @JIRAResource
    private WorklogService jiraWorklogService;

    @JIRAResource
    private IssueService issueService;

    @JIRAResource
    private IssueLinkManager issueLinkManager;

    @Resource(name = SessionController.SERVICE)
    private SessionController sessionController;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Override
    public CompleteSessionResult validateComplete(ApplicationUser user, String sessionId, CompleteSessionRequest request) {
        ErrorCollection errorCollection = new ErrorCollection();
        String timeSpentRaw = request.getTimeSpent();
        Long millisecondsSpent = getAndValidateTimeSpent(errorCollection, timeSpentRaw);
        Issue logTimeIssue = getAndValidateIssue(errorCollection, user, request.getLogTimeIssueId());
        List<CompleteSessionIssueLink> issuesToLink = Lists.newArrayList();
        for (CompleteSessionIssueLinkRequest linkReq : request.getIssueLinks()) {
            Issue related = getAndValidateIssue(errorCollection, user, linkReq.getRelatedId());
            Issue raised = getAndValidateIssue(errorCollection, user, linkReq.getRaisedId());
            if (related != null && raised != null) {
                issuesToLink.add(new CompleteSessionIssueLink(related, raised));
            }
        }
        UpdateResult updateResult = sessionController.validateCompleteSession(user, sessionId, new Duration(millisecondsSpent));
        errorCollection.addAllErrors(updateResult.getErrorCollection());

        return new CompleteSessionResult(user, errorCollection, updateResult, millisecondsSpent, timeSpentRaw, issuesToLink, logTimeIssue);
    }

    @Override
    public void complete(CompleteSessionResult result) {
        if (!result.isValid()) {
            return;
        }
        sessionController.update(result.getSessionUpdateResult());
        Issue logTimeIssue = result.getLogTimeIssue();
        if (logTimeIssue != null) {
            logTimeSpentOnIssue(result.getUser(), result.getSessionUpdateResult().getSession(), result.getTimeSpent(), logTimeIssue);
        }
        List<CompleteSessionIssueLink> thingsToLink = result.getIssuesToLink();
        for (CompleteSessionIssueLink entry : thingsToLink) {
            linkIssue(result.getUser(), entry.getRelated(), entry.getRaised());
        }
    }

    private Long getAndValidateTimeSpent(ErrorCollection errorCollection, String timeSpent) {
        Long millisecondsDuration = 0L;
        if (StringUtils.isNotBlank(timeSpent)) {
            try {
                // Need to multiply by 1000 as jiraDurationUtils returns duration in seconds
                millisecondsDuration = 1000 * parseDuration(timeSpent);
            } catch (InvalidDurationException e) {
                errorCollection.addError(i18n.getText("session.complete.invalid.duration", timeSpent));
            }
        }

        return millisecondsDuration;
    }

    private Issue getAndValidateIssue(ErrorCollection errorCollection, ApplicationUser user, String id) {
        if (StringUtils.isNotBlank(id)) {
            try {
                Long issueId = Long.parseLong(id);
                IssueResult result = issueService.getIssue(user, issueId);
                if (result.isValid()) {
                    return result.getIssue();
                } else {
                    errorCollection.addAllJiraErrors(result.getErrorCollection());
                }
            } catch (NumberFormatException e) {
                errorCollection.addError("session.issue.invalid", id);
            }
        }
        return null;
    }

    private Long parseDuration(String timeSpent) throws InvalidDurationException {
        return jiraDurationUtils.parseDuration(timeSpent, jiraAuthenticationContext.getLocale());
    }

    private void logTimeSpentOnIssue(ApplicationUser user, Session session, String timeSpent, Issue issueToLogAgainst) {
        JiraServiceContext context = new JiraServiceContextImpl(user);
        WorklogInputParameters worklogParams = WorklogInputParametersImpl.builder().issue(issueToLogAgainst).timeSpent(timeSpent)
                .startDate(session.getTimeCreated().toDate()).comment(i18n.getText("issue.service.logwork.comment.prefix", session.getName()))
                .build();
        WorklogResult worklogResult = jiraWorklogService.validateCreate(context, worklogParams);
        jiraWorklogService.createAndAutoAdjustRemainingEstimate(context, worklogResult, false);
    }

    private void linkIssue(ApplicationUser user, Issue relatedIssue, Issue issueToLink) {
        Collection<IssueLinkType> issueLinkTypeIterator = getBonfireIssueLinks();
        try {
            for (IssueLinkType type : issueLinkTypeIterator) {
                Long issueLinkTypeId = type.getId();
                issueLinkManager.createIssueLink(issueToLink.getId(), relatedIssue.getId(), issueLinkTypeId, 0L, user);
            }
        } catch (CreateException e) {
            // TODO in future we want to move to the service because it doesn't throw an exception.
            // This catch should never happen because we do a validation step first. If it does, we want it to die silently
        }
    }

    // Move this issue linking code to a service if it is ever needed anywhere else. ATM complete issue is the only one that needs this so this code
    // will sit here for now
    private Collection<IssueLinkType> getBonfireIssueLinks() {
        Collection<IssueLinkType> links = jiraIssueLinkTypeManager.getIssueLinkTypesByName(BONFIRE_TESTING);
        String outgoingName = i18n.getText("bonfire.issue.link.outgoing");
        String incomingName = i18n.getText("bonfire.issue.link.incoming");
        // If the links don't exist then create them
        if (links == null || links.isEmpty()) {
            jiraIssueLinkTypeManager.createIssueLinkType(BONFIRE_TESTING, outgoingName, incomingName, null);
        } else {
            // BON-541 - do it have the shape we want, no then we update it
            for (IssueLinkType link : links) {
                //
                // JIRA50 has made IssueLinkType an interface and hence we get ClassChangeAssertionErrors if we invoke
                // directly on the IssueLinkType
                //
                if (!outgoingName.equals(getOutward(link)) || !incomingName.equals(getInward(link))) {
                    jiraIssueLinkTypeManager.updateIssueLinkType(link, BONFIRE_TESTING, outgoingName, incomingName);
                }
            }
        }
        // A this point we are pretty sure it's what we want and in the shape we want
        links = jiraIssueLinkTypeManager.getIssueLinkTypesByName(BONFIRE_TESTING);
        return links;
    }

    private String getOutward(IssueLinkType issueLinkType) {
        return ReflectionKit.method(issueLinkType, "getOutward").call();
    }

    private String getInward(IssueLinkType issueLinkType) {
        return ReflectionKit.method(issueLinkType, "getInward").call();
    }
}
