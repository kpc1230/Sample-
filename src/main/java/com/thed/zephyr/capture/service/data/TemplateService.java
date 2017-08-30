package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.model.Template;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.model.util.TemplateSearchList;

/**
 * Service layer class for Template.
 * Created by Venkatareddy on 08/18/2017.
 */
public interface TemplateService {
	/**
	 * Creates a new Template.
	 * @param template - this is a TemplateRequest object from which Template is created.
	 * @return - Created Template object.
	 */
	Template createTemplate(TemplateRequest template);
	/**
	 * Update an existing Template.
	 * @param template - Template Object data to be updated.
	 * @return - updated Template if an existing Template was found in persistence, otherwise null.
	 */
	Template updateTemplate(TemplateRequest template);
	/**
	 * Delete an existing template using templateId.
	 * @param templateId
	 */
	void deleteTemplate(String templateId);
    /**
     * Get template using the templateId.
     * @param templateId
     * @return
     */
	Template getTemplate(String templateId);
	
	/**
	 * Get all the templates.
	 * @return
	 */
	TemplateSearchList getTemplates(String userName, Integer offset, Integer limit);
    
    /**
     * Get all the templates for the project projectId.
     * @param projectId - Project Id to be used for searching templates.
     * @param offset
     * @param limit
     * @return - List of Template objects found for the project.
     */
	TemplateSearchList getTemplatesByProject(Long projectId, Integer offset, Integer limit);
    /**
     * Get all the shared templates for the user with projectId.
     * @param owner
     * @param offset
     * @param limit
     * @return
     */
	TemplateSearchList getSharedTemplates(String owner, Integer offset, Integer limit);

	TemplateSearchList getFavouriteTemplates(String owner, Integer offset, Integer limit);
	TemplateSearchList getUserTemplates(String userName, Integer offset, Integer limit);
}
