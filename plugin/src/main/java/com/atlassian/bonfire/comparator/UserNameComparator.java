package com.atlassian.bonfire.comparator;

import com.atlassian.jira.user.ApplicationUser;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

/**
 * UserNameComparator that is different to the JIRA one. Doesn't require locale, doesn't throw runtime exceptions and sorts based on display names.
 *
 * @author ezhang
 */
public class UserNameComparator implements Comparator<ApplicationUser> {
    public int compare(final ApplicationUser user1, final ApplicationUser user2) {
        if (user1 == user2) {
            return 0;
        } else if (user2 == null) {
            return -1;
        } else if (user1 == null) {
            return 1;
        }

        String name1 = user1.getDisplayName();
        String name2 = user2.getDisplayName();
        if (StringUtils.isBlank(name1) && StringUtils.isBlank(name2)) {
            return 0;
        } else if (StringUtils.isBlank(name2)) {
            return -1;
        } else if (StringUtils.isBlank(name1)) {
            return 1;
        }

        return name1.compareTo(name2);
    }
}
