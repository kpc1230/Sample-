package com.thed.zephyr.capture.controller;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.FeedbackRequest;
import com.thed.zephyr.capture.service.email.CaptureEmailService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/feedback")
public class FeedbackController {

    @Autowired
    private Logger log;

    @Autowired
    CaptureEmailService captureEmailService;


    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> sendFeedbackEmail(@RequestBody FeedbackRequest feedbackRequest) throws CaptureValidationException {
        log.info("Start of sendFeedbackEmail() ");
        try {
            captureEmailService.sendFeedBackEmail(feedbackRequest);
            return ResponseEntity.ok(feedbackRequest);
        } catch (Exception ex) {
            log.error("Error in sendFeedbackEmail() -> ", ex);
            throw new CaptureRuntimeException(ex.getMessage(), ex);
        }
    }
}
