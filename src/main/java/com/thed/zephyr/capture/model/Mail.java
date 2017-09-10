package com.thed.zephyr.capture.model;

import java.util.List;

/**
 * Created by Masud on 9/10/17.
 */
public class Mail {
    private String to;
    private List<String> toList;
    private String cc;
    private List<String> ccList;
    private String bcc;
    private List<String> bccList;
    private String from;
    private String replyto;
    private String sentDate;
    private String subject;
    private String text;

    public Mail() {
    }

    public Mail(String to, String text) {
        this.to = to;
        this.text = text;
    }

    public Mail(String to, String subject, String text) {
        this.to = to;
        this.subject = subject;
        this.text = text;
    }

    public Mail(String to, List<String> toList, String cc, List<String> ccList, String bcc, List<String> bccList, String from, String replyto, String sentDate, String subject, String text) {
        this.to = to;
        this.toList = toList;
        this.cc = cc;
        this.ccList = ccList;
        this.bcc = bcc;
        this.bccList = bccList;
        this.from = from;
        this.replyto = replyto;
        this.sentDate = sentDate;
        this.subject = subject;
        this.text = text;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getToList() {
        return toList;
    }

    public void setToList(List<String> toList) {
        this.toList = toList;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public List<String> getCcList() {
        return ccList;
    }

    public void setCcList(List<String> ccList) {
        this.ccList = ccList;
    }

    public String getBcc() {
        return bcc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public List<String> getBccList() {
        return bccList;
    }

    public void setBccList(List<String> bccList) {
        this.bccList = bccList;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReplyto() {
        return replyto;
    }

    public void setReplyto(String replyto) {
        this.replyto = replyto;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
