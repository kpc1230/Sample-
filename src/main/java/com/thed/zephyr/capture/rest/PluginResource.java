package com.thed.zephyr.capture.rest;

import com.thed.zephyr.capture.rest.model.PluginDetailsBean;
import com.thed.zephyr.capture.rest.util.BonfireRestResource;
import com.thed.zephyr.capture.service.BonfireLicenseService;
import com.thed.zephyr.capture.service.BuildPropertiesService;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

@Path("/plugin")
public class PluginResource extends BonfireRestResource {
    // BONDEV-123: JIRA Analytics framework has it's own kill switches for analytics
    // So no kill kill switches in capture anymore
    private static final boolean JIRA_ANALYTICS_SETTINGS = true;

    @Resource(name = BuildPropertiesService.SERVICE)
    private BuildPropertiesService buildPropertiesService;

    @Resource(name = BonfireLicenseService.SERVICE)
    private BonfireLicenseService bonfireLicenseService;

    @Resource
    private BuildUtilsInfo jiraBuildUtilsInfo;

    public PluginResource() {
        super(PluginResource.class);
    }

    /**
     * This method is anonymous allowed because there is no reason to restrict this data from anonymous users. It may also be possible to check the
     * version before getting a valid cookie
     *
     * @return bean containing the details of the plugin
     */
    @GET
    @AnonymousAllowed
    @Path("/details")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getPluginDetails() {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                PluginDetailsBean details = new PluginDetailsBean.Builder()
                        .setVersion(buildPropertiesService.getVersion())
                        .setBuildNumber(buildPropertiesService.getBuildNumber())
                        .setBuildDate(buildPropertiesService.getBuildDate())
                        .setLicenseStatus(bonfireLicenseService.getLicenseStatus())
                        .setJiraVersion(jiraBuildUtilsInfo.getVersion())
                        .setJiraBuildInfo(jiraBuildUtilsInfo.getBuildInformation())
                        .setSendAnalytics(JIRA_ANALYTICS_SETTINGS)
                        .build();
                return ok(details);
            }
        });
    }
}
