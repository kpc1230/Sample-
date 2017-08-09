package com.thed.zephyr.capture.service.rest;

import com.thed.zephyr.capture.rest.model.UserOptionBean;
import com.thed.zephyr.capture.service.BonfireUserService;
import com.thed.zephyr.capture.web.util.UserFullNameComparator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

@Service(UserOptionService.SERVICE)
public class UserOptionService {
    public static final String SERVICE = "bonfire-useroptionservice";

    @Resource(name = BonfireUserService.SERVICE)
    private BonfireUserService bonfireUserService;

    @Resource
    private PermissionContextFactory jiraPermissionContextFactory;

    @SuppressWarnings("deprecation")
    public List<UserOptionBean> getAssignableUsers(Project project) {
        ArrayList<UserOptionBean> userBeans = new ArrayList<UserOptionBean>();
        Collection<ApplicationUser> assignableUsers = bonfireUserService.getUsers(Long.valueOf(Permissions.ASSIGNABLE_USER),
                jiraPermissionContextFactory.getPermissionContext(project));
        TreeSet<ApplicationUser> sortedUsers = new TreeSet<ApplicationUser>(new UserFullNameComparator());
        sortedUsers.addAll(assignableUsers);
        for (ApplicationUser user : sortedUsers) {
            userBeans.add(new UserOptionBean(user.getDisplayName(), user.getName()));
        }
        return userBeans;
    }
}
