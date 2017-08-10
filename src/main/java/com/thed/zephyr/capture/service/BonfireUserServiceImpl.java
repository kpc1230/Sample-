package com.thed.zephyr.capture.service;

import com.thed.zephyr.capture.util.model.BFDummyApplicationUser;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service(BonfireUserService.SERVICE)
public class BonfireUserServiceImpl implements BonfireUserService {

    @Resource
    private UserManager jiraUserManager;

    @JIRAResource
    private PermissionSchemeManager permissionSchemeManager;

    public String getUserKey(final String userName) {
        ApplicationUser user = jiraUserManager.getUserByName(userName);
        return user != null ? user.getKey() : userName;
    }

    public ApplicationUser getUser(String userName) {
        return jiraUserManager.getUserByName(userName);
    }

    public ApplicationUser safeGetUser(final String userName) {
        ApplicationUser user = jiraUserManager.getUserByName(userName);
        if (user == null) {
            // This DummyUser will replace the users that have been deleted
            user = new BFDummyApplicationUser(userName);
        }
        return user;
    }

    public ApplicationUser safeGetUserByKey(final String userKey) {
        ApplicationUser user = jiraUserManager.getUserByKey(userKey);
        if (user == null) {
            // This DummyUser will replace the users that have been deleted
            user = new BFDummyApplicationUser(userKey);
        }
        return user;
    }

    public Collection<ApplicationUser> getUsers(Long permission, PermissionContext context) {
        List<ApplicationUser> users = new ArrayList<ApplicationUser>();
        users.addAll(permissionSchemeManager.getUsers(permission, context));

        return users;
    }

    public Collection<ApplicationUser> getAllUsers() {
        List<ApplicationUser> users = new ArrayList<ApplicationUser>();
        users.addAll(jiraUserManager.getAllApplicationUsers());

        return users;
    }
}
