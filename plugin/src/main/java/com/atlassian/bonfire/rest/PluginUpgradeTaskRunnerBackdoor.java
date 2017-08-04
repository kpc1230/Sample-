package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.BonfireBuildCheckService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.sal.api.upgrade.PluginUpgradeManager;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

/**
 * Backdoor for running plugin upgrade tasks
 *
 * @since v1.8
 */
@Path("/runUpgrade")
public class PluginUpgradeTaskRunnerBackdoor extends BonfireRestResource {
    private final PluginUpgradeManager pluginUpgradeManager;

    @Resource(name = BonfireBuildCheckService.SERVICE)
    private BonfireBuildCheckService buildCheckService;

    @Resource
    private PermissionManager jiraPermissionManager;

    private final Logger log = Logger.getLogger(this.getClass());

    public PluginUpgradeTaskRunnerBackdoor(PluginUpgradeManager pluginUpgradeManager) {
        super(PluginUpgradeTaskRunnerBackdoor.class);
        this.pluginUpgradeManager = pluginUpgradeManager;
    }

    @POST
    public Response runUpgradeTasks() {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                if (!jiraPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, getLoggedInUser())) {
                    return forbiddenRequest();
                }

                if (buildCheckService.checkTheStateOfTheNation().hasErrors()) {
                    log.warn("Bonfire running upgrade tasks via the backdoor.");
                    log.warn("!! The back of one door is the face of another. !!");
                    pluginUpgradeManager.upgrade();
                    log.warn("Bonfire finished running upgrade tasks.");
                }
                return Response.ok().build();
            }
        });
    }
}
