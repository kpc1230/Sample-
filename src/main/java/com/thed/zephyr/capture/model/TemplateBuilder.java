package com.thed.zephyr.capture.model;

import java.util.Date;
import java.util.Objects;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.util.CaptureUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * Class to create Template Object from TemplateRequest and vice versa.
 * @author Venkatareddy
 * Created on 8/22/2017.
 *
 */
public final class TemplateBuilder {
	/**
	 * Construct a Template object using the TemplateRequest transfer Object.
	 * @param templateRequest - Object with values of Template
	 * @return - Template to be persisted
	 */
	public static Template constructTemplate(String ctId, TemplateRequest templateRequest){
		Template template = new Template();
		Date created = new Date();

		template.setCtId(ctId);
		template.setName(templateRequest.getName());
		template.setProjectId(templateRequest.getProjectId());
		template.setFavourite(templateRequest.getFavourited());
		template.setShared(templateRequest.getShared());
		template.setContent(templateRequest.getSource());
		if(CaptureUtil.isTenantGDPRComplaint()) {
			template.setCreatedByAccountId(templateRequest.getOwnerAccountId());
		} else {
			template.setCreatedBy(templateRequest.getOwnerName());
			template.setCreatedByAccountId(templateRequest.getOwnerAccountId());
		}
		template.setTimeCreated(created);
		//Populate timeUpdated
		if(templateRequest.getFavourited()){
			template.setTimeFavourited(created);
		}
		template.setTimeUpdated(created);
		return template;
	}
	/**
	 * Update Template object with the modified values from templateRequest. 
	 * @param template - Existing template object
	 * @param templateRequest - Values to be taken into Template object.
	 * @return - Modified Template to be persisted.
	 * @throws CaptureValidationException 
	 */
	public static Template updateTemplate(Template template, TemplateRequest templateRequest) throws CaptureValidationException{
		if(templateRequest.getName() != null && !templateRequest.getName().equals(template.getName())){
			template.setName(templateRequest.getName());
		}

		if(templateRequest.getProjectId() != null && templateRequest.getProjectId().longValue() != template.getProjectId().longValue()){
			template.setProjectId(templateRequest.getProjectId());
		}
		if(templateRequest.getFavourited() != null){
			template.setFavourite(templateRequest.getFavourited());
		}
		template.setShared(templateRequest.getShared());
		template.setContent(templateRequest.getSource());
		if(!template.getFavourite() && templateRequest.getFavourited() != null && templateRequest.getFavourited()){
			template.setTimeFavourited(new Date());
		}
		template.setTimeUpdated(new Date());
		return template;
	}
	
	/**
	 * Create a TemplateRequest object using the persisted template object to use on UI.
	 * @param template - Persisted Template Object.
	 * @return - TemplateRequest Transfer Object created out of template.
	 */
	private static TemplateRequest createTemplateRequest(Template template){
		TemplateRequest tr = new TemplateRequest();
		tr.setId(template.getId());
		tr.setName(template.getName());
		tr.setProjectId(template.getProjectId());
		tr.setFavourited(template.getFavourite());
		tr.setShared(template.getShared());
		tr.setSource(template.getContent());
		if(CaptureUtil.isTenantGDPRComplaint()) {
			tr.setOwnerAccountId(template.getCreatedByAccountId());
		} else {
			tr.setOwnerName(template.getCreatedBy());
			tr.setOwnerAccountId(template.getCreatedByAccountId());
		}
		tr.setTimeCreated(template.getTimeCreated());
		tr.setTimeUpdated(template.getTimeUpdated());
		return tr;
	}

	/**
	 * Parse the request body and populate the transfer object TemplateRequest.
	 * @param payload - recived RequestBody as JsonNode
	 * @return - parsed data as TemplateRequest
	 * @throws CaptureValidationException - If some mandatory input is not present.
	 */
	public static TemplateRequest parseJson(JsonNode payload) {
		TemplateRequest templateRequest = new TemplateRequest();
		templateRequest.setName(payload.path("name").asText());
		//Populate project
		JsonNode jsonProject = (JsonNode)payload.get("project");
		if(!jsonProject.isMissingNode()){
			templateRequest.setProjectId(jsonProject.path("value").asLong());			
		}else{
			templateRequest.setProjectId(null); // case handled in controller
		}
		//Populate timeCreated
		templateRequest.setVariablesChanged(false);

		//Populate shared
		templateRequest.setShared(payload.path("shared").isMissingNode() ? false : payload.path("shared").asBoolean());

		//Populate favourited
		templateRequest.setFavourited(payload.path("favourited").isMissingNode() ? true : payload.path("favourited").asBoolean());

		//Populate Source (source as it is from request body)
		templateRequest.setSource(payload);
		return templateRequest;
	}
	
	public static TemplateRequest parseUpdateJson(JsonNode payload) {
		JsonNode updatedJson = (JsonNode)payload.get("source"); //fetching only the updated values;
		TemplateRequest templateRequest = new TemplateRequest();
		templateRequest.setName(updatedJson.path("name").asText());
		templateRequest.setId(payload.path("id").asText());
		//Populate project
		JsonNode jsonProject = updatedJson.path("project").get("value");
		if(Objects.nonNull(jsonProject) && !jsonProject.isMissingNode()){
			templateRequest.setProjectId(Long.valueOf(jsonProject.asText()));				
		}
		//Populate timeCreated
		templateRequest.setVariablesChanged(false);
		
		//settimeupdated
		templateRequest.setTimeUpdated(new Date(payload.path("timeUpdated").asLong()));

		//Populate shared
		templateRequest.setShared(updatedJson.path("shared").isMissingNode() ? false : updatedJson.path("shared").asBoolean());

		//Populate favourited
		if(!updatedJson.path("favourited").isMissingNode()){
			templateRequest.setFavourited(updatedJson.path("favourited").get("value").asBoolean());
		}
		
		//Populate Source (source as it is from request body)
		templateRequest.setSource(updatedJson);
		return templateRequest;
	}

	/**
	 * 
	 * @param template
	 * @param project
	 * @return
	 */
	public static TemplateRequest createTemplateRequest(Template template, Project project, CaptureUser user) {
		TemplateRequest request = createTemplateRequest(template);
		if(project != null){
			request.setProjectKey(project.getKey());		
		}
		if(user != null){
			if(CaptureUtil.isTenantGDPRComplaint()) {
				request.setOwnerAccountId(user.getAccountId());
			} else {
				request.setOwnerName(user.getKey());
				request.setOwnerAccountId(user.getAccountId());
			}
			request.setOwnerDisplayName(user.getDisplayName());
		}
		return request;
	}

    /**
     *
     * @param template
     * @param projectKey
     * @return
     */
    public static TemplateRequest createTemplateRequest(Template template, String projectKey, CaptureUser user) {
        TemplateRequest request = createTemplateRequest(template);
        if(StringUtils.isNotEmpty(projectKey)){
            request.setProjectKey(projectKey);		}
        if(user != null){
        	if(CaptureUtil.isTenantGDPRComplaint()) {
        		request.setOwnerAccountId(user.getAccountId());
        	} else {
        		 request.setOwnerName(user.getKey());
                 request.setOwnerAccountId(user.getAccountId());
        	}
            request.setOwnerDisplayName(user.getDisplayName());
        }

        return request;
    }
}
