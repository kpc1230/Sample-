package com.thed.zephyr.capture.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.user.ApplicationUser;

import java.util.Collection;

/**
 * UserService for Bonfire, which wraps the JIRA UserManager, who only has deprecations and such in 5.0+
 */
public interface BonfireUserService {

    public static final String SERVICE = "bonfire-BonfireUserService";

    /**
     * Returns the userkey from a username
     */
    String getUserKey(final String userName);

    /**
     * Returns a {@link User} based on user name.
     *
     * @param userName the user name of the user
     * @return the User object, or null if the user cannot be found including null userName.
     */
    ApplicationUser getUser(final String userName);

    /**
     * Returns a {@link User} based on user name.
     *
     * @param userName the user name of the user
     * @return the User object, or a dummy user if the user cannot be found.
     */
    ApplicationUser safeGetUser(final String userName);

    /**
     * Returns a {@link User} based on user name.
     *
     * @param userKey the unique key of the user. This is used when loading the user from storage
     * @return the User object, or a dummy user if the user cannot be found.
     */
    ApplicationUser safeGetUserByKey(final String userKey);

    /**
     * Returns a collection of {@link User} based on permissions and permission context.
     *
     * @param permission long representing the permission. Found in com.atlassian.jira.security.Permissions
     * @param context    Permission context for the list of users
     * @return the collection of User objects. Collection will be empty if there are none.
     */
    Collection<ApplicationUser> getUsers(Long permission, PermissionContext context);

    /**
     * @return all users in this instance of JIRA
     */
    Collection<ApplicationUser> getAllUsers();
}
