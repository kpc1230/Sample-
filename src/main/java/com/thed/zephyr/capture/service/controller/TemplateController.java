package com.thed.zephyr.capture.service.controller;

import com.thed.zephyr.capture.model.FavouriteTemplate;
import com.thed.zephyr.capture.model.Template;
import com.thed.zephyr.capture.model.Variable;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.json.JSONObject;

import java.util.List;

/**
 * Controller for storing and retrieving templates
 *
 * @since v1.7
 */
public interface TemplateController {
    public static final String SERVICE = "bonfire-templateController";

    public CreateResult validateCreate(ApplicationUser creator, JSONObject source);

    public TemplateResult create(CreateResult result);

    public UpdateResult validateUpdate(ApplicationUser updater, Template newTemplate);

    public TemplateResult update(UpdateResult result);

    public DeleteResult validateDelete(ApplicationUser deleter, Template template);

    public TemplateResult delete(DeleteResult result);

    public SaveFavouriteResult validateSaveFavourite(ApplicationUser favouriter, Long id);

    public TemplateResult saveFavourite(SaveFavouriteResult result);

    public RemoveFavouriteResult validateRemoveFavourite(ApplicationUser favouriter, Long id);

    public TemplateResult removeFavourite(RemoveFavouriteResult result);

    public FavouriteTemplate loadFavouriteData(ApplicationUser user, Template template);

    public List<Template> loadFavouriteTemplates(ApplicationUser user);

    public List<Template> loadSharedTemplatesForProject(Project project);

    public List<Template> loadSharedTemplates(ApplicationUser user, Integer startIndex, Integer size);

    public List<Template> loadUserTemplates(ApplicationUser user, Integer startIndex, Integer size);

    public List<Template> loadTemplatesForAdmin(final ApplicationUser user, Integer startIndex, Integer size);

    /**
     * Will return a template is shared in a valid project the user has permissions for, or owned by the user. null otherwise
     */
    public Template loadTemplate(ApplicationUser user, Long templateId);

    public void variableUpdated(Variable var, ApplicationUser user);

    public void variableDeleted(Variable var, ApplicationUser user);


    public static class CreateResult extends TemplateValidationResultWithUser {
        public CreateResult(ErrorCollection errorCollection, Template value, ApplicationUser user) {
            super(errorCollection, value, user);
        }
    }

    public static class UpdateResult extends TemplateValidationResult {
        public UpdateResult(ErrorCollection errorCollection, Template value) {
            super(errorCollection, value);
        }
    }

    public static class DeleteResult extends TemplateValidationResult {
        public DeleteResult(ErrorCollection errorCollection, Template value) {
            super(errorCollection, value);
        }

    }

    public static class SaveFavouriteResult extends TemplateValidationResultWithUser {
        public SaveFavouriteResult(ErrorCollection errorCollection, Template value, ApplicationUser user) {
            super(errorCollection, value, user);
        }

    }

    public static class RemoveFavouriteResult extends TemplateValidationResultWithUser {
        public RemoveFavouriteResult(ErrorCollection errorCollection, Template value, ApplicationUser user) {
            super(errorCollection, value, user);
        }

    }

    public static class TemplateValidationResultWithUser extends TemplateValidationResult {

        private final ApplicationUser user;

        public TemplateValidationResultWithUser(ErrorCollection errorCollection, Template value, ApplicationUser user) {
            super(errorCollection, value);
            this.user = user;
        }

        public ApplicationUser getUser() {
            return user;
        }

    }

    public static class TemplateValidationResult extends TemplateResult {
        public TemplateValidationResult(ErrorCollection errorCollection, Template value) {
            super(errorCollection, value);
        }
    }

    public static class TemplateResult extends ServiceOutcomeImpl<Template> {
        public TemplateResult(ErrorCollection errorCollection, Template template) {
            super(errorCollection, template);
        }

        /**
         * Convenience method that returns a new ServiceOutcomeImpl instance containing no errors, and with the provided
         * returned value.
         *
         * @param returnedValue the returned value
         * @return a new ServiceOutcomeImpl
         */
        public static TemplateResult ok(Template returnedValue) {
            return new TemplateResult(new ErrorCollection(), returnedValue);
        }
    }
}
