package com.thed.zephyr.capture.service.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.InviteService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
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

    @Override
    public void sendInviteToSession(Session session, String emailAddress, String message) throws Exception {
        //impl of send email.
        Map<String, Object> params = getContextParams(session,message);
        String subject = getRenderedTemplate("templates/email/invite-session.vm", params);
        String body = getRenderedTemplate("templates/email/invite-body.vm", params);

        log.debug("We are trying to send to email {} {}",emailAddress, message);

        throw new Exception("Email(sendEmail()) need to implement here.");
    }

    private String getRenderedTemplate(String templatePath, Map<String, Object> contextParams) throws Exception{
         throw new Exception("Render template to string");
    }

    private Map<String, Object> getContextParams(Session session, String message) {
        Map<String, Object> context = new HashMap<>();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();

        context.put("remoteUser", host.getUserKey().get());
        context.put("session", session);
        context.put("message", message);
        context.put("i18n", LocaleContextHolder.getLocale());
        context.put("baseurl", env.getProperty(ApplicationConstants.CAPTURE_BASEURL));
        return context;
    }
}
