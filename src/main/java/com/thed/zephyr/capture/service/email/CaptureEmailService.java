package com.thed.zephyr.capture.service.email;

import com.thed.zephyr.capture.model.FeedbackRequest;
import com.thed.zephyr.capture.model.InviteSessionRequest;
import com.thed.zephyr.capture.model.Session;


public interface CaptureEmailService {
    void sendFeedBackEmail(FeedbackRequest feedbackRequest) throws Exception;
}
