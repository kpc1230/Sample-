package com.thed.zephyr.capture.service;

import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.model.Session;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.SMTPMailServer;
import com.google.common.collect.Maps;
import org.apache.velocity.exception.VelocityException;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service(BonfireEmailService.SERVICE)
public class BonfireEmailServiceImpl implements BonfireEmailService {
    @JIRAResource
    private JiraAuthenticationContext jiraAuthenticationContext;

    @JIRAResource
    private ApplicationProperties applicationProperties;

    public void sendInviteToSession(ApplicationUser sender, Session session, String emailAddress, String message) {
        String fromEmail = sender.getEmailAddress();
        Map<String, Object> params = getContextParams(sender, session, message);
        String subject = getRenderedTemplate("templates/bonfire/email/subject/invite-session.vm", params);
        String body = getRenderedTemplate("templates/bonfire/email/text/invite-body.vm", params);
        Email email = new Email(emailAddress).setFrom(fromEmail).setReplyTo(fromEmail).setSubject(subject).setBody(body);
        sendEmail(email);
    }

    private void sendEmail(Email email) {
        try {
            SMTPMailServer smtpMailServer = MailFactory.getServerManager().getDefaultSMTPMailServer();
            if (smtpMailServer != null) {
                smtpMailServer.send(email);
            }
        } catch (MailException e) {
            // Log me
        }
    }

    private String getRenderedTemplate(String templatePath, Map<String, Object> contextParams) {
        String mailEncoding = ComponentAccessor.getApplicationProperties().getMailEncoding();
        try {
            String rendered = ComponentAccessor.getVelocityManager().getEncodedBody("", templatePath, mailEncoding, contextParams);
            return rendered;
        } catch (VelocityException e) {
            // Gulp TODO - add logging
            return "";
        }
    }

    private Map<String, Object> getContextParams(ApplicationUser user, Session session, String message) {
        Map<String, Object> context = Maps.newHashMap();
        context.put("remoteUser", user);
        context.put("session", session);
        context.put("message", message);
        context.put("i18n", jiraAuthenticationContext.getI18nHelper());
        context.put("baseurl", applicationProperties.getString(APKeys.JIRA_BASEURL));
        return context;
    }
}
