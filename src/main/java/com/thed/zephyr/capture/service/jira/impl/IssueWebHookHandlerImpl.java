package com.thed.zephyr.capture.service.jira.impl;

import com.google.common.collect.Lists;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.repositories.dynamodb.SessionRepository;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.jira.IssueWebHookHandler;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

@Service
public class IssueWebHookHandlerImpl implements IssueWebHookHandler {

    @Autowired
    private Logger log;
    @Autowired
    private SessionESRepository sessionESRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private SessionActivityRepository sessionActivityRepository;

    @Override
    public void issueDeleteEventHandler(AcHostModel acHostModel, Long issueId) {
        List<Long> relatedIssueIds = Arrays.<Long>asList(issueId);
        List<Session> sessions = sessionESRepository.findByCtIdAndRelatedIssueIds(acHostModel.getCtId(), issueId.toString());
        for (Session session:sessions){
            session.getRelatedIssueIds().remove(issueId);
            sessionESRepository.save(session);
            sessionRepository.save(session);
            log.debug("Related issueId:{} was deleted from sessionId:{}", issueId, session.getId());
        }
        BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
        booleanQuery.must(termsQuery("issuesRaised.issueId", issueId.toString()));
        Iterable<Session> search = sessionESRepository.search(booleanQuery);
        for (Session session:search) {
            removeRaisedIssie(session, issueId);
            sessionESRepository.save(session);
            sessionRepository.save(session);
            removeIssueRaisedUnraisedSessionActivity(session.getId(), issueId);
            log.debug("Raised issueId:{} was deleted from sessionId:{}", issueId, session.getId());
        }
    }

    private void removeRaisedIssie(Session session, Long issueId){
        IssueRaisedBean issueRaisedBean = null;
        for (IssueRaisedBean issueRaisedBeanCurrent:session.getIssuesRaised()){
            if(issueRaisedBeanCurrent.getIssueId().equals(issueId)){
                issueRaisedBean = issueRaisedBeanCurrent;
                break;
            }
        }
        if(issueRaisedBean != null){
            session.getIssuesRaised().remove(issueRaisedBean);
        }
    }

    private void removeIssueRaisedUnraisedSessionActivity(String sessionId, Long issueId){
        List<SessionActivity> sessionActivities = sessionActivityRepository.findBySessionId(sessionId);
        for (SessionActivity sessionActivity:sessionActivities){
            if (sessionActivity instanceof IssueRaisedSessionActivity && ((IssueRaisedSessionActivity)sessionActivity).getIssueId().equals(issueId)){
                sessionActivityRepository.delete(sessionActivity);
            } else if(sessionActivity instanceof IssueUnraisedSessionActivity && ((IssueUnraisedSessionActivity)sessionActivity).getIssueId().equals(issueId)){
                sessionActivityRepository.delete(sessionActivity);
            }
        }
    }
}
