package com.thed.zephyr.capture.service.email.impl;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicStringProperty;
import com.thed.zephyr.capture.model.Mail;
import com.thed.zephyr.capture.service.email.AmazonSEService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Class to send email.
 *
 * Created by Masud on 9/10/17.
 */
@Service
public class AmazonSEServiceImpl implements AmazonSEService{

    @Autowired
    private Logger log;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private Environment env;

    @Override
    public boolean sendMail(Mail mail) throws MessagingException {

        // Replace sender@example.com with your "From" address.
        // This address must be verified.
        DynamicStringProperty dpF = dynamicProperty.getStringProp(ApplicationConstants.FROM_EMAIL,
                ApplicationConstants.DEFAULT_EMAIL_FROM);
        String FROM = dpF.get().equals("")?dpF.getDefaultValue():dpF.getValue();

        //This from must come from current logged in user
        DynamicStringProperty dpU = dynamicProperty.getStringProp(ApplicationConstants.AWS_SMTP_USERNAME,
                ApplicationConstants.DEFAULT_SMTP_USERNAME);
        String SMTP_USERNAME = dpU.get().equals("")?dpU.getDefaultValue():dpU.getValue();

        // Replace smtp_password with your Amazon SES SMTP password.
        DynamicStringProperty dpP = dynamicProperty.getStringProp(ApplicationConstants.AWS_SMTP_PASSWORD,
                ApplicationConstants.DEFAULT_SMTP_PASSWORD);
        String SMTP_PASSWORD = dpP.get().equals("")?dpP.getDefaultValue():dpP.getValue();

        // The name of the Configuration Set to use for this message.
        // If you comment out or remove this variable, you will also need to
        // comment out or remove the header on line 65.
        // String CONFIGSET = "ConfigSet";

        // Amazon SES SMTP host name. This example uses the US West (Oregon) Region.
        DynamicStringProperty dpR = dynamicProperty.getStringProp(ApplicationConstants.AWS_SES_REGION,
                ApplicationConstants.DEFAULT_SES_REGION);
        String region = dpR.get().equals("")?dpR.getDefaultValue():dpR.getValue();
        String HOST = "email-smtp."+region+".amazonaws.com";

        // The port you will connect to on the Amazon SES SMTP endpoint.
        DynamicIntProperty dpPo = dynamicProperty.getIntProp(ApplicationConstants.AWS_SMTP_PORT,
                ApplicationConstants.DEFAULT_SMTP_PORT);
        int PORT = dpPo.get()==0?dpPo.getDefaultValue():dpPo.getValue();

        // Create a Properties object to contain connection configuration information.
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        // Create a Session object to represent a mail session with the specified properties.
        Session session = Session.getDefaultInstance(props);
        Transport transport = null;

        try
        {

        // Create a transport.
        transport = session.getTransport();

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM,mail.getFrom()));
        String TO = mail.getTo() != null ? mail.getTo(): StringUtils.join(mail.getToList(), ',');
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(TO));
        msg.setSubject(mail.getSubject());
        msg.setContent(mail.getText(),"text/html");

        if(StringUtils.isNotEmpty(mail.getCc())){
            msg.setRecipient(Message.RecipientType.CC, new InternetAddress(mail.getCc()));
        }

        if(mail.getCcList() != null && mail.getCcList().size()>0){
            msg.setRecipient(Message.RecipientType.CC, new InternetAddress(StringUtils.join(mail.getCcList(), ',')));
        }

        if(StringUtils.isNotEmpty(mail.getBcc())){
            msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(mail.getBcc()));
        }

            if(mail.getBccList() != null && mail.getBccList().size()>0){
                msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(StringUtils.join(mail.getBccList(), ',')));
            }
        // Add a configuration set header. Comment or delete the
        // next line if you are not using a configuration set
        // msg.setHeader("X-SES-CONFIGURATION-SET", CONFIGSET);


            // Send the message.
            log.debug("Sending...");

            // Connect to Amazon SES using the SMTP username and password you specified above.
            transport.connect(HOST, SMTP_USERNAME, SMTP_PASSWORD);

            // Send the email.
            transport.sendMessage(msg, msg.getAllRecipients());
            log.debug("Email sent!");
            return true;
        }
        catch (Exception ex) {
            log.debug("The email was not sent.");
            log.debug("Error message: " + ex.getMessage());
            return false;
        }
        finally
        {
            if(transport != null) {
                // Close and terminate the connection.
                transport.close();
            }
        }

    }
}
