package com.thed.zephyr.capture.service.email.impl;

import com.thed.zephyr.capture.model.FeedbackRequest;
import com.thed.zephyr.capture.model.Mail;
import com.thed.zephyr.capture.service.email.AmazonSEService;
import com.thed.zephyr.capture.service.email.CaptureEmailService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaptureEmailServiceImpl implements CaptureEmailService {

    @Autowired
    private Logger log;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private AmazonSEService amazonSEService;

    @Autowired
    private VelocityEngine engine;

    @Autowired
    private CaptureI18NMessageSource i18n;

    @Override
    public void sendFeedBackEmail(FeedbackRequest feedbackRequest) throws Exception {
        log.info("sendFeedBackEmail started");
        Map<String, Object> model = new HashMap<>();
        model.put("name", feedbackRequest.getName() != null ? feedbackRequest.getName() : "");
        model.put("description", feedbackRequest.getDescription() != null ? feedbackRequest.getDescription() : "");
        model.put("email", feedbackRequest.getEmail() != null ? feedbackRequest.getEmail() : "");

        String BODY = VelocityEngineUtils.mergeTemplateIntoString(this.engine, "email/feedback.vm", "UTF-8", model);

        log.debug("Email subject: {}", feedbackRequest.getSummary() != null ? feedbackRequest.getSummary() : "");
        log.debug("Email body: {}", BODY);

        Mail mail = new Mail();
        String toEmail = dynamicProperty.getStringProp(ApplicationConstants.FEEDBACK_SEND_EMAIL, "atlassian.dev@getzephyr.com").get();

        mail.setTo(toEmail);
        mail.setSubject(feedbackRequest.getSummary() != null ? feedbackRequest.getSummary() : "");
        mail.setText(BODY);

        if (amazonSEService.sendMail(mail)) {
            log.info("Successfully sent email to : {}", toEmail);
        }
        log.info("sendFeedBackEmail complted");
    }
}
