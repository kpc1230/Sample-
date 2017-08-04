package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.model.FavouriteTemplate;
import com.atlassian.bonfire.model.Template;
import com.atlassian.bonfire.rest.model.EmptyBean;
import com.atlassian.bonfire.rest.model.GetTemplatesResponse;
import com.atlassian.bonfire.rest.model.TemplateBean;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.BonfireUnmarshalService;
import com.atlassian.bonfire.service.controller.TemplateController;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.Math.abs;

/**
 * TemplateResource - HTTP front end to TemplateController
 *
 * @since v1.7
 */
@Path("/templates")
public class TemplateResource extends BonfireRestResource {
    @Resource(name = TemplateController.SERVICE)
    private TemplateController templateController;

    @Resource(name = BonfireUnmarshalService.SERVICE)
    private BonfireUnmarshalService bonfireUnmarshalService;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @Resource
    private PermissionManager jiraPermissionManager;

    @JIRAResource
    private ProjectManager jiraProjectManager;

    private static final Integer DEFAULT_TEMPLATES_RETURNED = 50;

    public TemplateResource() {
        super(TemplateResource.class);
    }

    /**
     * ******************************************************************************************
     * <p>
     * POST RESOURCES
     * </p>
     * *******************************************************************************************
     */

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createTemplate(final String templateBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                TemplateController.CreateResult result;

                try {
                    result = templateController.validateCreate(getLoggedInUser(), new JSONObject(templateBody));
                } catch (JSONException e) {
                    return badRequest("template.resource.json.incorrect");
                }

                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }

                TemplateController.TemplateResult templateResult = templateController.create(result);

                return ok(new TemplateBean(templateResult.getReturnedValue(), FavouriteTemplate.INVALID, excaliburWebUtil));
            }
        });
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateTemplate(final String templateBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                JSONObject templateJSON;
                try {
                    templateJSON = new JSONObject(templateBody);
                } catch (JSONException e) {
                    return badRequest("template.resource.json.incorrect");
                }

                TemplateController.UpdateResult result = templateController.validateUpdate(getLoggedInUser(),
                        bonfireUnmarshalService.getTemplateFromJSON(templateJSON));

                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }

                TemplateController.TemplateResult templateResult = templateController.update(result);

                return ok(new TemplateBean(templateResult.getReturnedValue(), templateController.loadFavouriteData(getLoggedInUser(),
                        templateResult.getReturnedValue()), excaliburWebUtil));
            }
        });
    }

    @DELETE
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteTemplate(final String templateBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                JSONObject templateJSON;
                try {
                    templateJSON = new JSONObject(templateBody);
                } catch (JSONException e) {
                    return badRequest("template.resource.json.incorrect");
                }

                TemplateController.DeleteResult result = templateController.validateDelete(getLoggedInUser(),
                        bonfireUnmarshalService.getTemplateFromJSON(templateJSON));

                if (!result.isValid()) {
                    return badRequest(result.getErrorCollection());
                }

                TemplateController.TemplateResult templateResult = templateController.delete(result);

                return ok(new TemplateBean(templateResult.getReturnedValue(), FavouriteTemplate.INVALID, excaliburWebUtil));
            }
        });
    }

    @POST
    @Path("/favourites")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response modifyFavourite(final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                Boolean templateFavourite;
                Long id;

                try {
                    JSONObject favouriteJSON = new JSONObject(requestBody);
                    id = favouriteJSON.getLong("id");
                    templateFavourite = favouriteJSON.getBoolean("favourite");
                } catch (JSONException e) {
                    return badRequest("template.resource.json.incorrect");
                }

                if (templateFavourite) {
                    TemplateController.SaveFavouriteResult saveResult = templateController.validateSaveFavourite(getLoggedInUser(), id);
                    if (!saveResult.isValid()) {
                        return badRequest(saveResult.getErrorCollection());
                    }

                    Template template = templateController.saveFavourite(saveResult).getReturnedValue();
                    return Response.ok(
                            new TemplateBean(template, templateController.loadFavouriteData(getLoggedInUser(), template), excaliburWebUtil)).build();
                } else {
                    TemplateController.RemoveFavouriteResult removeResult = templateController.validateRemoveFavourite(getLoggedInUser(), id);
                    if (!removeResult.isValid()) {
                        return badRequest(removeResult.getErrorCollection());
                    }

                    Template template = templateController.removeFavourite(removeResult).getReturnedValue();
                    return Response.ok(
                            new TemplateBean(template, templateController.loadFavouriteData(getLoggedInUser(), template), excaliburWebUtil)).build();
                }

            }
        });
    }

    /**
     * ******************************************************************************************
     * <p>
     * GET RESOURCES
     * </p>
     * *******************************************************************************************
     */

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/user")
    public Response loadUserTemplates(@QueryParam("startAt") final Integer startIndex, @QueryParam("maxResults") final Integer sizeRaw) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                return loadTemplatesAndBuildResponse(sizeRaw, new TemplateLoaderForUserTemplates(), startIndex);
            }
        });
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/shared")
    public Response loadSharedTemplates(@QueryParam("startAt") final Integer startIndex, @QueryParam("maxResults") final Integer sizeRaw) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                return loadTemplatesAndBuildResponse(sizeRaw, new TemplateLoaderForSharedTemplates(), startIndex);
            }
        });
    }

    /**
     * Loads templates for admin user
     *
     * @param startIndex start index
     * @param sizeRaw    size
     * @return templates list
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/admin")
    public Response loadAllTemplates(@QueryParam("startAt") final Integer startIndex, @QueryParam("maxResults") final Integer sizeRaw) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                // Only allow admin & sysadmin users to access this resource
                final ApplicationUser loggedInUser = getLoggedInUser();
                if (loggedInUser != null && !isSysAdmin(loggedInUser) && !isAdmin(loggedInUser)) {
                    return forbiddenRequest();
                }
                return loadTemplatesAndBuildResponse(sizeRaw, new TemplateLoaderForAdmins(), startIndex);
            }
        });
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/favourites")
    public Response loadFavouriteTemplates(@QueryParam("startAt") final Integer startIndex,
                                           @QueryParam("maxResults") final Integer size) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                List<Template> favouriteTemplates = templateController.loadFavouriteTemplates(getLoggedInUser());

                Collection<TemplateBean> favouriteTemplateBeans = Collections2.transform(favouriteTemplates, new Function<Template, TemplateBean>() {

                    public TemplateBean apply(@Nullable Template template) {
                        return new TemplateBean(template, templateController.loadFavouriteData(getLoggedInUser(), template), excaliburWebUtil);
                    }
                });

                return ok(new GetTemplatesResponse(false, favouriteTemplateBeans));
            }
        });
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/{templateId}")
    public Response loadSingleTemplate(final @PathParam("templateId") String templateId) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                try {
                    Template template = templateController.loadTemplate(getLoggedInUser(), Long.parseLong(templateId));
                    if (template != null) {
                        return ok(new TemplateBean(template, templateController.loadFavouriteData(getLoggedInUser(), template), excaliburWebUtil));
                    }
                } catch (NumberFormatException e) {
                    // no, that didn't work
                }
                return ok(new EmptyBean());
            }
        });
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("/defaults/{projectKey}")
    public Response loadPossibleDefaultTemplates(final @PathParam("projectKey") String projectKey) {
        return response(new Callable<Response>() {
            @Override
            public Response call() {
                List<TemplateBean> templateBeans = Lists.newArrayList();
                Project project = jiraProjectManager.getProjectObjByKey(projectKey);
                if (project != null) {
                    List<Template> templates = templateController.loadSharedTemplatesForProject(project);
                    for (Template t : templates) {
                        TemplateBean bean = new TemplateBean(t, templateController.loadFavouriteData(getLoggedInUser(), t), excaliburWebUtil);
                        templateBeans.add(bean);
                    }
                }
                return ok(new GetTemplatesResponse(false, templateBeans));
            }
        });
    }

    /**
     * ******************************************************************************************
     * <p>
     * PRIVATE METHODS
     * </p>
     * *******************************************************************************************
     */

    private Integer processStartIndex(Integer startIndex) {
        return startIndex != null ? startIndex : 0;
    }

    private Response loadTemplatesAndBuildResponse(Integer sizeRaw, TemplateLoader templateLoader, Integer startIndex) {
        final Response invalidCallResponse = validateRestCall();
        if (invalidCallResponse != null) {
            return invalidCallResponse;
        }

        final Integer size = processSize(sizeRaw);
        boolean hasMore = false;
        List<Template> userTemplates = templateLoader.loadTemplates(getLoggedInUser(), startIndex, size);
        if (size < userTemplates.size()) {
            Template overflow = userTemplates.remove(size.intValue());
            if (overflow != null) {
                hasMore = true;
            }
        }

        final Collection<TemplateBean> userTemplateBeans = Collections2.transform(userTemplates, new Function<Template, TemplateBean>() {

            public TemplateBean apply(@Nullable Template template) {
                return new TemplateBean(template, templateController.loadFavouriteData(getLoggedInUser(), template), excaliburWebUtil);
            }
        });

        return ok(new GetTemplatesResponse(hasMore, userTemplateBeans));
    }

    private Integer processSize(Integer size) {
        return size != null ? abs(size) : DEFAULT_TEMPLATES_RETURNED;
    }

    /**
     * Interface to handle different strategy of template loading
     */
    private static interface TemplateLoader {
        /**
         * Loads list of templates
         *
         * @param user       user to load templates for
         * @param startIndex start index
         * @param size       batch size
         * @return list of templates
         */

        public List<Template> loadTemplates(ApplicationUser user, Integer startIndex, Integer size);
    }

    private class TemplateLoaderForUserTemplates implements TemplateLoader {

        @Override
        public List<Template> loadTemplates(ApplicationUser user, Integer startIndex, Integer size) {
            return templateController.loadUserTemplates(user, processStartIndex(startIndex),
                    processSize(size) + 1);
        }
    }

    private class TemplateLoaderForSharedTemplates implements TemplateLoader {

        @Override
        public List<Template> loadTemplates(ApplicationUser user, Integer startIndex, Integer size) {
            return templateController.loadSharedTemplates(getLoggedInUser(), processStartIndex(startIndex),
                    processSize(size) + 1);
        }
    }

    private class TemplateLoaderForAdmins implements TemplateLoader {

        @Override
        public List<Template> loadTemplates(ApplicationUser user, Integer startIndex, Integer size) {
            return templateController.loadTemplatesForAdmin(getLoggedInUser(), processStartIndex(startIndex),
                    processSize(size) + 1);
        }
    }

    private boolean isSysAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    private boolean isAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

}
