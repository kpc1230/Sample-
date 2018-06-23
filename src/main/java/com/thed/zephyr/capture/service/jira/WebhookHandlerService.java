package com.thed.zephyr.capture.service.jira;

import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.BasicIssue;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.repositories.elasticsearch.SessionESRepository;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class WebhookHandlerService {

    private Logger log;
    private SessionService sessionService;
    private SessionESRepository sessionESRepository;

    public WebhookHandlerService(@Autowired Logger log,
                                 @Autowired SessionService sessionService,
                                 @Autowired SessionESRepository sessionESRepository) {
        this.log = log;
        this.sessionService = sessionService;
        this.sessionESRepository = sessionESRepository;
    }

    public void issueCreateEventHandler(AcHostModel acHostModel, BasicIssue basicIssue, CaptureUser user){
        log.trace("Triggered issueCreateEventHandler...");
        try {
            String recipientSessionId = sessionService.getActiveSessionIdByUser(user.getKey(), acHostModel);
            recipientSessionId = StringUtils.isNotEmpty(recipientSessionId)?recipientSessionId:findSessionByParticipateUser(acHostModel, user);
            if(StringUtils.isEmpty(recipientSessionId)){
                log.debug("User doesn't have any active sessions and doesn't participate in any.");
                return;
            }
            sessionService.addRaisedIssueToSession(acHostModel, recipientSessionId, basicIssue, user);
        } catch (Exception exception) {
            log.error("Error during performing issue create event.", exception);
        }
    }

    private String findSessionByParticipateUser(AcHostModel acHostModel, CaptureUser user){
        Page<Session> userParticipatedSessionPage = sessionESRepository.findByCtIdAndStatusAndParticipantsUser(acHostModel.getCtId(), Session.Status.STARTED.toString(), user.getKey(), CaptureUtil.getPageRequest(0, 1000));
        for(Session session:userParticipatedSessionPage.getContent()){
            for (Participant participant:session.getParticipants()){
                if(StringUtils.equals(participant.getUser(), user.getKey()) && participant.getTimeLeft() == null){
                    return session.getId();
                }
            }
        }

        return null;
    }
}
