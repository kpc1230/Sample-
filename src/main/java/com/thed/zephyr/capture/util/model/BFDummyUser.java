package com.thed.zephyr.capture.util.model;

import com.atlassian.crowd.embedded.api.User;

public class BFDummyUser implements User {
    private String name;

    public BFDummyUser(String name) {
        this.name = name;
    }

    public String getEmail() {
        return "";
    }

    public String getFullName() {
        return "Deleted user with username (" + getName() + ")";
    }

    public String getKey() {
        return name;
    }

    public String getUsername() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getDirectoryId() {
        return -1l;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public String getEmailAddress() {
        return "";
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public int compareTo(User user) {
        return user.getName().compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof User) && this.compareTo((User) o) == 0;
    }

    public boolean isLocalServiceDeskUser() {
        return true;
    }
}
