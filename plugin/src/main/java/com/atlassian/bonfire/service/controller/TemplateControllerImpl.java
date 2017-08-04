package com.atlassian.bonfire.service.controller;

import com.atlassian.bonfire.model.*;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.bonfire.service.BonfirePermissionService;
import com.atlassian.bonfire.service.dao.TemplateDao;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.excalibur.service.dao.IdDao;
import com.atlassian.excalibur.web.util.JSONKit;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONObject;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Controller for storing and retrieving templates
 *
 * @since v1.7
 */
// TODO I'm an ugly beast that needs a refactor to use common services more
@Service(TemplateController.SERVICE)
public class TemplateControllerImpl implements TemplateController {
    @Resource(name = TemplateDao.SERVICE)
    private TemplateDao templateDao;

    @Resource(name = VariableController.SERVICE)
    private VariableController variableController;

    @Resource(name = BonfirePermissionService.SERVICE)
    private BonfirePermissionService permissionService;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @Resource(name = IdDao.SERVICE)
    private IdDao idDao;

    @Resource
    private PermissionManager jiraPermissionManager;

    @JIRAResource
    private ProjectService jiraProjectService;

    private final Logger log = Logger.getLogger(this.getClass());

    @Override
    public CreateResult validateCreate(ApplicationUser creator, JSONObject source) {
        notNull(creator);
        notNull(source);

        ErrorCollection errorCollection = new ErrorCollection();
        // Extract and find the project
        Long projectId = extractProjectIdFromJSON(source);
        Boolean shared = Boolean.valueOf(JSONKit.getString(source, "shared"));
        String name = JSONKit.getString(source, "name");
        ProjectService.GetProjectResult projectResult = jiraProjectService.getProjectById(creator, projectId);
        Iterable<Variable> variables = variableController.loadVariablesForTemplate(creator, source);

        if (StringUtils.isBlank(name)) {
            errorCollection.addError(i18n.getText("template.validate.create.empty.name"));
        }
        if (!projectResult.isValid()) {
            errorCollection.addError(i18n.getText("template.validate.create.cannot.browse.project"));
            return new CreateResult(errorCollection, Template.EMPTY, creator);
        } else {
            // Need create issue permission in the project
            if (!permissionService.canCreateTemplate(creator, projectResult.getProject())) {
                errorCollection.addError(i18n.getText("template.validate.create.cannot.create.issue"));
            }
            return new CreateResult(errorCollection, new TemplateBuilder(idDao.genNextId()).setOwner(creator)
                    .setProject(projectResult.getProject())
                    .setShared(shared)
                    .setJsonSource(source.toString())
                    .setVariables(variables).build(), creator);
        }

    }

    @Override
    public TemplateResult create(CreateResult result) {
        notNull(result);

        Template template = result.getReturnedValue();
        if (!result.isValid()) {
            return new TemplateResult(result.getErrorCollection(), template);
        }

        templateDao.save(template);
        templateDao.saveFavourite(template.getId(), result.getUser());

        return TemplateResult.ok(template);
    }

    @Override
    public UpdateResult validateUpdate(ApplicationUser updater, Template newTemplate) {
        notNull(updater);
        notNull(newTemplate);

        ErrorCollection errorCollection = new ErrorCollection();
        // First verify that the template already exists
        Template oldTemplate = templateDao.load(newTemplate.getId());
        if (oldTemplate.equals(Template.EMPTY)) {
            errorCollection.addError(i18n.getText("template.validate.update.not.exist"));
        }
        if (!canModifyTemplate(updater, oldTemplate)) {
            errorCollection.addError(i18n.getText("template.validate.update.permission"));
        }
        // Validate that timeCreated hasn't changed - sanity check
        if (!oldTemplate.getTimeCreated().equals(newTemplate.getTimeCreated())) {
            errorCollection.addError(i18n.getText("template.validate.update.time.created.mismatch"));
        }
        // Validate that timeUpdated hasn't changed
        // This means that noone else has updated the template in between us / the template isn't out of date
        if (!oldTemplate.getTimeUpdated().equals(newTemplate.getTimeUpdated())) {
            errorCollection.addError(i18n.getText("template.validate.update.time.updated.incorrect"));
        }

        // Update the time stamps
        // TODO Reduce code duplication between this and create
        Long projectId = extractProjectIdFromJSON(newTemplate.getJsonObject());
        Boolean shared = Boolean.valueOf(JSONKit.getString(newTemplate.getJsonObject(), "shared"));
        String name = JSONKit.getString(newTemplate.getJsonObject(), "name");
        ProjectService.GetProjectResult projectResult = jiraProjectService.getProjectById(updater, projectId);
        Iterable<Variable> variables = variableController.loadVariablesForTemplate(updater, newTemplate.getJsonObject());

        if (StringUtils.isBlank(name)) {
            errorCollection.addError(i18n.getText("template.validate.create.empty.name"));
        }
        if (!projectResult.isValid()) {
            errorCollection.addError(i18n.getText("template.validate.create.cannot.browse.project"));
            return new UpdateResult(errorCollection, Template.EMPTY);
        } else {
            // Need create issue permission in the project
            if (!permissionService.canEditTemplate(updater, projectResult.getProject())) {
                errorCollection.addError(i18n.getText("template.validate.create.cannot.create.issue"));
            }
            return new UpdateResult(errorCollection, new TemplateBuilder(newTemplate).setProject(projectResult.getProject())
                    .setShared(shared)
                    .setVariables(variables)
                    .build());
        }
    }

    private boolean canModifyTemplate(ApplicationUser user, Template oldTemplate) {
        return isUserOwnerOfTemplate(user, oldTemplate) || isAdmin(user) || isSysAdmin(user);
    }

    private boolean isSysAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user);
    }

    private boolean isAdmin(ApplicationUser user) {
        return jiraPermissionManager.hasPermission(Permissions.ADMINISTER, user);
    }

    private static boolean isUserOwnerOfTemplate(ApplicationUser user, Template template) {
        return user.getKey().equals(template.getOwnerName());
    }

    @Override
    public TemplateResult update(UpdateResult result) {
        notNull(result);

        Template template = result.getReturnedValue();
        if (!result.isValid()) {
            return new TemplateResult(result.getErrorCollection(), template);
        }
        templateDao.save(template);
        return TemplateResult.ok(template);
    }

    @Override
    public DeleteResult validateDelete(ApplicationUser deleter, Template template) {
        notNull(deleter);
        notNull(template);

        ErrorCollection errorCollection = new ErrorCollection();
        // Validate that the template exists
        Template oldTemplate = templateDao.load(template.getId());
        if (oldTemplate.equals(Template.EMPTY)) {
            errorCollection.addError(i18n.getText("template.validate.delete.not.exist"));
        }

        if (!oldTemplate.getTimeUpdated().equals(template.getTimeUpdated())) {
            errorCollection.addError(i18n.getText("template.validate.delete.outdated"));
        }

        ProjectService.GetProjectResult projectResult = jiraProjectService.getProjectById(deleter, oldTemplate.getProjectId());

        if (!projectResult.isValid()) {
            errorCollection.addError(i18n.getText("template.validate.delete.cannot.browse.project"));
        } else {
            if (!canModifyTemplate(deleter, oldTemplate)) {
                errorCollection.addError(i18n.getText("template.validate.delete.permission.fail"));
            }
        }

        return new DeleteResult(errorCollection, oldTemplate);
    }

    @Override
    public TemplateResult delete(DeleteResult result) {
        notNull(result);

        Template template = result.getReturnedValue();
        if (!result.isValid()) {
            return new TemplateResult(result.getErrorCollection(), template);
        }
        templateDao.delete(template);
        return TemplateResult.ok(template);
    }

    @Override
    public SaveFavouriteResult validateSaveFavourite(ApplicationUser favouriter, Long id) {
        notNull(favouriter);
        notNull(id);

        ErrorCollection errorCollection = new ErrorCollection();
        Template template = templateDao.load(id);
        if (!template.getId().equals(id)) {
            errorCollection.addError(i18n.getText("template.validate.save.favourite.not.exist"));
        }

        if (!template.getOwnerName().equals(favouriter.getName()) &&
                !template.isShared()) {
            // Template isn't yours, and it isn't shared.
            errorCollection.addError(i18n.getText("template.validate.save.favourite.permission.fail"));
        }

        return new SaveFavouriteResult(errorCollection, template, favouriter);
    }

    @Override
    public TemplateResult saveFavourite(SaveFavouriteResult result) {
        notNull(result);

        Template template = result.getReturnedValue();
        if (!result.isValid()) {
            return new TemplateResult(result.getErrorCollection(), template);
        }
        templateDao.saveFavourite(template.getId(), result.getUser());

        return TemplateResult.ok(template);
    }

    @Override
    public RemoveFavouriteResult validateRemoveFavourite(ApplicationUser favouriter, Long id) {
        notNull(favouriter);
        notNull(id);

        ErrorCollection errorCollection = new ErrorCollection();
        // Verify that the template exists
        Template template = templateDao.load(id);
        if (!template.getId().equals(id)) {
            errorCollection.addError(i18n.getText("template.validate.save.favourite.not.exist"));
        }

        return new RemoveFavouriteResult(errorCollection, template, favouriter);
    }

    @Override
    public TemplateResult removeFavourite(RemoveFavouriteResult result) {
        notNull(result);

        Template template = result.getReturnedValue();
        if (!result.isValid()) {
            return new TemplateResult(result.getErrorCollection(), template);
        }
        templateDao.removeFavourite(template.getId(), result.getUser());

        return TemplateResult.ok(template);
    }

    @Override
    public FavouriteTemplate loadFavouriteData(ApplicationUser user, Template template) {
        // TODO This is going to get hammered, might be nice to have a small, bounded cache in front of it at some point
        return templateDao.loadFavouriteData(user, template);
    }

    @Override
    public List<Template> loadSharedTemplatesForProject(final Project project) {
        Predicate<IndexedTemplate> sharedTemplateForProjectPredicate = new Predicate<IndexedTemplate>() {
            @Override
            public boolean apply(@Nullable IndexedTemplate input) {
                return project.getId().equals(input.getProjectId());
            }
        };
        return templateDao.loadAllRelevantSharedTemplates(sharedTemplateForProjectPredicate);
    }

    @Override
    public List<Template> loadFavouriteTemplates(final ApplicationUser user) {
        notNull(user);

        Predicate<Template> sharedTemplateWithPermissionsPredicate = new Predicate<Template>() {
            @Override
            public boolean apply(@Nullable Template input) {
                // If it's not mine, and it's not shared, then I shouldn't be able to see it.
                if (!input.isShared() && !input.getOwnerName().equals(user.getName())) {
                    return false;
                }
                // If I've favourited it, but the template has changed + uses a project I can't use anymore, shouldn't be able to use it
                // It'd be nice if we didn't have to do this on each GET - perhaps if the predicate fails it should be removed from the favourite list?
                return permissionService.canUseTemplate(user, input.getProjectId());
            }
        };

        return templateDao.loadFavouriteTemplates(user, sharedTemplateWithPermissionsPredicate);
    }

    @Override
    public List<Template> loadSharedTemplates(final ApplicationUser user, Integer startIndex, Integer size) {
        notNull(user);
        notNull(startIndex);
        notNull(size);

        Predicate<IndexedTemplate> createIssuePermissionPredicate = new Predicate<IndexedTemplate>() {
            @Override
            public boolean apply(@Nullable IndexedTemplate input) {
                return permissionService.canUseTemplate(user, input.getProjectId());
            }
        };

        return templateDao.loadSharedTemplates(createIssuePermissionPredicate, startIndex, size);
    }

    /**
     * Loads all templates for admin user
     *
     * @param user       user, must be admin or sysadmin
     * @param startIndex paging start
     * @param size       batch size
     * @return list of templates for admin user or empty list
     */
    @NotNull
    @Override
    public List<Template> loadTemplatesForAdmin(final ApplicationUser user, Integer startIndex, Integer size) {
        notNull(user);
        notNull(startIndex);
        notNull(size);

        final List<Template> templates;
        if (!isAdmin(user) && !isSysAdmin(user)) {
            templates = Collections.emptyList();
        } else {

            final Predicate<Template> predicate = new Predicate<Template>() {
                @Override
                public boolean apply(@Nullable Template template) {
                    // Load all templates
                    return true;
                }
            };
            templates = templateDao.loadTemplatesForAdmin(user, predicate, startIndex, size);
        }
        return templates;
    }

    @Override
    public List<Template> loadUserTemplates(final ApplicationUser user, Integer startIndex, Integer size) {
        notNull(user);
        notNull(startIndex);
        notNull(size);

        Predicate<Template> createIssuePermissionPredicate = new Predicate<Template>() {
            @Override
            public boolean apply(@Nullable Template input) {
                // If I have created a template and then the permissions have changed, its possible for it to not be usable by me anymore
                return permissionService.canUseTemplate(user, input.getProjectId());
            }
        };

        return templateDao.loadUserTemplates(user, createIssuePermissionPredicate, startIndex, size);
    }

    @Override
    public void variableUpdated(final Variable var, ApplicationUser user) {
        Iterable<Template> userTemplates = templateDao.userTemplatesIterable(user, Predicates.<Template>alwaysTrue());
        log.debug(String.format("Updating templates - created/updated variable %s, user %s", var.getName(), var.getOwnerName()));
        for (Template template : userTemplates) {
            log.debug(String.format("Looking at template %d", template.getId()));

            Predicate<Variable> varIdPredicate = new Predicate<Variable>() {
                @Override
                public boolean apply(@Nullable Variable variable) {
                    return variable.getId().equals(var.getId());
                }
            };

            // If the template contained the variable in the past, and the name has changed, we need to update the name in the template
            if (Iterables.any(template.getVariables(), varIdPredicate)) {
                log.debug(String.format("Updating template %d - variable rename", template.getId()));
                String oldVariableName = Iterables.find(template.getVariables(), varIdPredicate).getName();
                Pattern oldVariablePattern = Pattern.compile("\\{" + oldVariableName + "\\}");
                JSONObject templateJSON = template.getJsonObject();
                Iterator<String> keys = templateJSON.keys();
                String newVariableName = "{" + var.getName() + "}";
                while (keys.hasNext()) {
                    String currentKey = keys.next();
                    JSONObject currentItem = JSONKit.get(templateJSON, currentKey);
                    // only if a value exists and only if that value is not null
                    if (currentItem.has("value") && !currentItem.isNull("value")) {
                        Object value = currentItem.opt("value");
                        // we do not want to replace values in jsonarrays
                        if (!(value instanceof JSONArray)) {
                            String itemValue = JSONKit.getString(currentItem, "value");
                            Matcher currentMatcher = oldVariablePattern.matcher(itemValue);
                            // Replace the value in the item
                            currentItem.put("value", currentMatcher.replaceAll(newVariableName));
                            // Replace the item in the template
                            templateJSON.put(currentKey, currentItem);
                        }
                    }
                }
                update(new UpdateResult(new ErrorCollection(), new TemplateBuilder(template)
                        .setJsonSource(templateJSON.toString())
                        .setVariables(variableController.loadVariablesForTemplate(user, templateJSON))
                        .build()));
            } else {
                Template updatedTemplate = new TemplateBuilder(template)
                        .setVariables(variableController.loadVariablesForTemplate(user, template.getJsonObject()))
                        .build();

                // If this template now contains the updated variable, update it.
                if (Iterables.any(updatedTemplate.getVariables(), varIdPredicate)) {
                    log.debug(String.format("Updating template %d - variables changed", template.getId()));
                    update(new UpdateResult(new ErrorCollection(), updatedTemplate));
                }
            }
        }
        log.debug(String.format("Finished updating templates - created/updated variable %s, user %s", var.getName(), var.getOwnerName()));
    }

    @Override
    public Template loadTemplate(ApplicationUser user, Long templateId) {
        if (user.isActive() && templateId != null) {
            Template template = templateDao.load(templateId);
            boolean hasPermission = permissionService.canUseTemplate(user, template.getProjectId());
            // Check to ensure the user can use the template. Template.EMPTY will fail this check so all good.
            if (hasPermission) {
                // If owner or shared
                // Because of BON-1972, admins & sysadmin are given permissions to browse all templates on the instance
                if (canModifyTemplate(user, template) || template.isShared()) {
                    return template;
                }
            }
        }
        return null;
    }

    @Override
    public void variableDeleted(Variable var, ApplicationUser user) {
        Iterable<Template> userTemplates = templateDao.userTemplatesIterable(user, Predicates.<Template>alwaysTrue());
        log.debug(String.format("Updating templates - deleted variable %s, user %s", var.getName(), var.getOwnerName()));
        for (Template template : userTemplates) {
            log.debug(String.format("Looking at template %d", template.getId()));
            // If this template had the variable that was deleted, update it.
            if (template.getVariables().contains(var)) {
                log.debug(String.format("Updating template %d", template.getId()));
                Template updatedTemplate = new TemplateBuilder(template)
                        .setVariables(variableController.loadVariablesForTemplate(user, template.getJsonObject()))
                        .build();

                update(new UpdateResult(new ErrorCollection(), updatedTemplate));
            }
        }
        log.debug(String.format("Finished updating templates - deleted variable %s, user %s", var.getName(), var.getOwnerName()));
    }

    private Long extractProjectIdFromJSON(JSONObject source) {
        JSONObject project = JSONKit.get(source, "project");
        Long projectId = Long.valueOf(JSONKit.getString(project, "value", "-1"));
        return projectId;
    }
}
