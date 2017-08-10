package com.thed.zephyr.capture.listeners;

import com.thed.zephyr.capture.customfield.BonfireMultiSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.BonfireSessionCustomFieldService;
import com.thed.zephyr.capture.customfield.TestingStatusCustomFieldService;
import com.thed.zephyr.capture.events.*;
import com.thed.zephyr.capture.service.BonfireServiceSupport;
import com.atlassian.event.api.EventListener;
import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.issue.Issue;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service(SessionCFListener.SERVICE)
public class SessionCFListener extends BonfireServiceSupport {
    public static final String SERVICE = "bonfire-SessionCFListener";

    @Resource(name = BonfireSessionCustomFieldService.SERVICE)
    private BonfireSessionCustomFieldService bonfireSessionCustomFieldService;

    @Resource(name = BonfireMultiSessionCustomFieldService.SERVICE)
    private BonfireMultiSessionCustomFieldService bonfireMultiSessionCustomFieldService;

    @Resource(name = TestingStatusCustomFieldService.SERVICE)
    private TestingStatusCustomFieldService testingStatusCustomFieldService;

    @Override
    protected void onPluginStart() {
    }

    @Override
    protected void onPluginStop() {
    }

    @Override
    protected void onClearCache() {
    }

    @EventListener
    public void raisedIssueInSession(IssueRaisedInSessionEvent event) {
        bonfireSessionCustomFieldService.addRaisedInValue(event.getIssueRaised(), event.getSession());
        testingStatusCustomFieldService.updateTestingStatus(event.getIssueRaised());
    }

    @EventListener
    public void unraisedIssueInSession(IssueUnraisedInSessionEvent event) {
        bonfireSessionCustomFieldService.deleteRaisedInValue(event.getIssueUnraised());
        testingStatusCustomFieldService.updateTestingStatus(event.getIssueUnraised());
    }

    @EventListener
    public void linkSessionToIssueOnCreate(CreateSessionEvent event) {
        Session session = event.getSession();
        for (Issue i : session.getRelatedIssues()) {
            bonfireMultiSessionCustomFieldService.addRelatedToValue(i, session);
            testingStatusCustomFieldService.updateTestingStatus(i);
        }
    }

    @EventListener
    public void updateTestingStatusForRelatedIssues(SessionStatusChangedEvent event) {
        Session session = event.getSession();
        for (Issue i : session.getRelatedIssues()) {
            testingStatusCustomFieldService.updateTestingStatus(i);
        }
    }

    @EventListener
    public void updateSessionToIssueLinks(UpdateSessionEvent event) {
        List<Issue> before = event.getBefore().getRelatedIssues();
        List<Issue> after = event.getAfter().getRelatedIssues();
        if (!before.equals(after)) {
            for (Issue i : before) {
                if (!after.contains(i)) {
                    bonfireMultiSessionCustomFieldService.deleteRelatedToValue(i, event.getAfter());
                    testingStatusCustomFieldService.updateTestingStatus(i);
                }
            }
            for (Issue i : after) {
                if (!before.contains(i)) {
                    bonfireMultiSessionCustomFieldService.addRelatedToValue(i, event.getAfter());
                    testingStatusCustomFieldService.updateTestingStatus(i);
                }
            }
        }
    }

    @EventListener
    public void removeSessionFromIssue(DeleteSessionEvent event) {
        Session deletedSession = event.getSession();
        for (Issue i : deletedSession.getIssuesRaised()) {
            bonfireSessionCustomFieldService.deleteRaisedInValue(i);
            testingStatusCustomFieldService.updateTestingStatus(i);
        }
        for (Issue i : deletedSession.getRelatedIssues()) {
            bonfireMultiSessionCustomFieldService.deleteRelatedToValue(i, deletedSession);
            testingStatusCustomFieldService.updateTestingStatus(i);
        }
    }
}
