package com.thed.zephyr.capture.service.gdpr.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Masud on 4/2/19.
 */
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 2149141530228032194L;

    private String userKey;
    private String userName;
    private String accountId;

    public UserDTO() {
    }

    public UserDTO(String userKey, String userName, String accountId) {
        this.userKey = userKey;
        this.userName = userName;
        this.accountId = accountId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDTO)) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(getUserKey(), userDTO.getUserKey()) &&
                Objects.equals(getUserName(), userDTO.getUserName()) &&
                Objects.equals(getAccountId(), userDTO.getAccountId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserKey(), getUserName(), getAccountId());
    }
}
