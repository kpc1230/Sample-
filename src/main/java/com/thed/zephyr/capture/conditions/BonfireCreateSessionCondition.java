package com.thed.zephyr.capture.conditions;

import com.thed.zephyr.capture.service.BonfirePermissionService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;

import javax.annotation.Resource;
import java.util.Map;

/**
 * TODO When we drop 4.4 Move this to implement AbstractJiraCondition or AbstractJiraPermissionCondition
 *
 * @author ezhang
 */
public class BonfireCreateSessionCondition implements Condition {
    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService bonfirePermissionService;

    private static final String KEY_PROJECT = "project";
    private static final String KEY_USER = "user";

    @Override
    public void init(Map<String, String> arg0) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> hashMap) {
        if (hashMap.containsKey(KEY_PROJECT) && hashMap.containsKey(KEY_USER)) {
            Object possibleProject = hashMap.get(KEY_PROJECT);
            Object possibleUser = hashMap.get(KEY_USER);
            if (possibleProject instanceof Project && (possibleUser instanceof User || possibleUser instanceof ApplicationUser)) {
                final ApplicationUser user;
                if (possibleUser instanceof ApplicationUser) {
                    user = (ApplicationUser) possibleUser;
                } else {
                    user = ApplicationUsers.from((User) possibleUser);
                }
                Project project = (Project) possibleProject;
                return bonfirePermissionService.canCreateSession(user, project);
            }
        }
        return false;
    }
}
