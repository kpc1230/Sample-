package com.atlassian.bonfire.util.model;

import java.util.Optional;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

/**
 * This DummyUser will replace the users that have been deleted.
 */
public class BFDummyApplicationUser implements ApplicationUser {
    private String name;

    public BFDummyApplicationUser(String name) {
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

    public Optional<String> getAccountId() {
        return Optional.empty();
    }

    public String getUsername() {
        return name;
    }

    public String getName() {
        return name;
    }

    public long getDirectoryId() {
        return -1l;
    }

    public boolean isActive() {
        return false;
    }

    public String getEmailAddress() {
        return "";
    }

    public String getDisplayName() {
        return name;
    }

    public User getDirectoryUser() {
        return new BFDummyUser(name);
    }

    public int compareTo(User user) {
        return user.getName().compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof User) && this.compareTo((User) o) == 0;
    }

    /**
     * JIRA 7.0 implemented method
     *
     * @return ID
     */
    public Long getId() {
        return -1l;
    }
}
