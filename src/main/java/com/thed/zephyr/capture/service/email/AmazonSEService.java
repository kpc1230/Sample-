
package com.thed.zephyr.capture.service.email;

import com.thed.zephyr.capture.model.Mail;

import javax.mail.MessagingException;

/**
 * Class is to send email by using AWS SES
 *
 * Created by Masud on 9/9/17.
 */
@SuppressWarnings("ALL")
public interface AmazonSEService {
    boolean sendMail(Mail mail) throws MessagingException;
}
