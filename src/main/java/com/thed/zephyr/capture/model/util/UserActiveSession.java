package com.thed.zephyr.capture.model.util;

import com.thed.zephyr.capture.model.Participant;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import org.apache.commons.lang3.StringUtils;

public class UserActiveSession {

    private CaptureUser user;

    private Session session;

    private UserType userType;

    public UserActiveSession(CaptureUser user, Session session) {
        this.user = user;
        this.session = session;
        if (this.session == null){
            this.userType = UserType.NONE;
        } else if(isUserParticipant(user, session)){
            this.userType = UserType.PARTICIPANT;
        } else if(StringUtils.equals(session.getAssignee(), user.getKey())){
            this.userType = UserType.ASSIGNEE;
        }
    }

    public CaptureUser getUser() {
        return user;
    }

    public void setUser(CaptureUser user) {
        this.user = user;
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId(){
        return session != null?session.getId():null;
    }

    public UserType getUserType() {
        return userType;
    }

    public enum UserType{
        PARTICIPANT, ASSIGNEE, NONE
    }

    public Boolean isUserHasActiveSession(){
        return session != null?true:false;
    }

    private Boolean isUserParticipant(CaptureUser user, Session session){
        if (session.getParticipants() != null) {
            for (Participant participant:session.getParticipants()){
                if(StringUtils.equals(participant.getUser(), user.getKey())
                || StringUtils.equals(participant.getUserAccountId(), user.getAccountId())
                ){
                    return true;
                }
            }
        }

        return false;
    }
}
