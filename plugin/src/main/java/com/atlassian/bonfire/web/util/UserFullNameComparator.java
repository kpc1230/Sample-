package com.atlassian.bonfire.web.util;

import com.atlassian.jira.user.ApplicationUser;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Borrowed from GH code, currently sorts names into alphabetical order based on Display Name
 */
public class UserFullNameComparator implements Comparator<ApplicationUser>, Serializable {
    public int compare(ApplicationUser user1, ApplicationUser user2) {
        if (user1 == null && user2 == null)
            return 0;

        if (user1 == null || user1.getDisplayName() == null)
            return -1;

        if (user2 == null || user2.getDisplayName() == null)
            return 1;

        if (user1.getDisplayName().compareToIgnoreCase(user2.getDisplayName()) == 0)
            return user1.getName().compareToIgnoreCase(user2.getName());

        return user1.getDisplayName().compareToIgnoreCase(user2.getDisplayName());
    }
}
