package com.thed.zephyr.capture.service.data.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.thed.zephyr.capture.model.InviteSessionRequest;
import com.thed.zephyr.capture.model.Mail;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.service.data.InviteService;
import com.thed.zephyr.capture.service.email.AmazonSEService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.CaptureUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.ui.velocity.VelocityEngineUtils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private VelocityEngine engine;

    @Autowired
    private CaptureI18NMessageSource i18n;

    @Autowired
    private AddonDescriptorLoader ad;

    @Override
    public void sendInviteToSession(Session session, InviteSessionRequest inviteSessionRequest) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String userKey = host.getUserKey().get();
        CaptureUser loggedUser = userService.findUserByKey(userKey);

        List<String> toEmails = inviteSessionRequest.getEmails() != null ?
                inviteSessionRequest.getEmails(): new ArrayList<>();

        if(inviteSessionRequest.getUsernames() != null){
            inviteSessionRequest.getUsernames()
                    .forEach(username -> {
                        CaptureUser captureUser =
                                userService.findUserByKey(username);
                        toEmails.add(captureUser.getEmailAddress());
                    });
        }

        //session link
        String SESSION_LINK = host.getHost().getBaseUrl()+ CaptureUtil.createSessionLink(session.getId(),ad.getDescriptor().getKey());

        //subject
        String subject = "[JIRA] "+i18n.getMessage("capture.session.invite.subject",new Object[]{loggedUser.getDisplayName(),session.getName()});

        //note
        String NOTE = inviteSessionRequest.getMessage() != null ?
                 inviteSessionRequest.getMessage(): "Please come to join.";

        Map<String, Object> model = new HashMap<>();
        model.put("fullname",loggedUser.getDisplayName());
        model.put("sessionlink",SESSION_LINK);
        model.put("sessionid",session.getId());
        model.put("firstline",i18n.getMessage("capture.session.invite.body.firstline", new Object[]{loggedUser.getDisplayName()}));
        model.put("secondline",i18n.getMessage("capture.session.invite.body.link",new Object[]{SESSION_LINK,SESSION_LINK}));
        model.put("note",NOTE);

        String BODY = VelocityEngineUtils.mergeTemplateIntoString(this.engine, "email/body.vm", "UTF-8", model);

        log.debug("Email subject: {}", subject);
        log.debug("Email body: {}", BODY);

        Mail mail = new Mail();

        mail.setToList(toEmails);
        mail.setSubject(subject);
        mail.setText(BODY);

        if(amazonSEService.sendMail(mail)){
         log.info("Successfully sent email to : {}", StringUtils.join(toEmails,","));
        }

    }

}
