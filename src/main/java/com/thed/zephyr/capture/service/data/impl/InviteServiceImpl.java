package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.model.InviteSessionRequest;
import com.thed.zephyr.capture.model.Mail;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.service.data.InviteService;
import com.thed.zephyr.capture.service.email.AmazonSEService;
import com.thed.zephyr.capture.service.jira.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Masud on 8/17/17.
 */
@Service
public class InviteServiceImpl implements InviteService{

    @Autowired
    private Logger log;

    @Autowired
    private Environment env;

    @Autowired
    private UserService userService;

    @Autowired
    private AmazonSEService amazonSEService;

    @Override
    public void sendInviteToSession(Session session, InviteSessionRequest inviteSessionRequest) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String userKey = host.getUserKey().get();
        CaptureUser loggedUser = userService.findUser(userKey);

        List<String> toEmails = inviteSessionRequest.getEmails() != null ?
                inviteSessionRequest.getEmails(): new ArrayList<>();

        if(inviteSessionRequest.getUsernames() != null){
            inviteSessionRequest.getUsernames()
                    .forEach(username -> {
                        CaptureUser captureUser =
                                userService.findUser(username);
                        toEmails.add(captureUser.getEmailAddress());
                    });
        }

        //session link
        String SESSION_LINK = host.getHost().getBaseUrl()+"/plugins/servlet/ac/capture-cloud/view-session-url?session.id="+session.getId()+"&origin=nav&invite=true";

        //subject
        String subject = "[JIRA] "+loggedUser.getDisplayName()+" just invited you to participate in \""+session.getName()+"\"";

        //note
        String NOTE = inviteSessionRequest.getMessage() != null ?
                "<p>"+ inviteSessionRequest.getMessage()+"</p>": "<p>Please come to join.</p>";

        //body
        String BODY = String.join(
                System.getProperty("line.separator"),
                "<div style=\"border: 1px solid #EEE;\n" +
                        "    padding: 5px;\n" +
                        "    background: #EFEFEF;\n" +
                        "    border-radius: 5px;\""+
                        "<p><strong>"+loggedUser.getDisplayName()+"</strong> just invited you to participate in a test session</p>" +
                        "<p>Click here to visit the session: "+ SESSION_LINK + " .</p>" +
                        NOTE,
                "<p>-This message was sent by <strong>Zephyr</strong>, Capture for Jira Cloud.</p>"+
                        "</div>"

        );

        Mail mail = new Mail();

        mail.setToList(toEmails);
        mail.setSubject(subject);
        mail.setText(BODY);

        if(amazonSEService.sendMail(mail)){
         log.info("Successfully sent email to : {}", StringUtils.join(toEmails,","));
        }

    }

}
