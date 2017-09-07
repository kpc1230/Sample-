package com.thed.zephyr.capture.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.domain.Project;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.jira.CaptureUser;

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
	public static Template constructTemplate(String ctId, TemplateRequest templateRequest, Set<String> variables){
		Template template = new Template();
		DateTime created = new DateTime();

		template.setCtId(ctId);
		template.setName(templateRequest.getName());
		template.setProjectId(templateRequest.getProjectId());
		template.setFavourite(templateRequest.getFavourited());
		template.setShared(templateRequest.getShared());
		template.setContent(templateRequest.getSource());
		template.setCreatedBy(templateRequest.getOwnerName());
//		template.setVariables(getVariables());
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
	public static Template updateTemplate(Template template, TemplateRequest templateRequest, Set<String> variables) throws CaptureValidationException{
		TemplateRequest newTR = parseJson(templateRequest.getSource());
		if(newTR.getName() != null && !newTR.getName().equals(template.getName())){
			template.setName(newTR.getName());
		}

		if(newTR.getProjectId() != null && newTR.getProjectId().longValue() != template.getProjectId().longValue()){
			template.setProjectId(newTR.getProjectId());
		}

		template.setFavourite(newTR.getFavourited());
		template.setShared(newTR.getShared());
		template.setContent(templateRequest.getSource());
		if(!template.getFavourite() && newTR.getFavourited()){
			templateRequest.setTimeFavourited(new DateTime());
		}
		template.setTimeUpdated(new DateTime());
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
		tr.setOwnerName(template.getCreatedBy());
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
	public static TemplateRequest parseJson(JsonNode payload) throws CaptureValidationException{
		TemplateRequest templateRequest = new TemplateRequest();
		templateRequest.setName(payload.path("name").asText());
		//Populate project
		JsonNode jsonProject = (JsonNode)payload.get("project");
		if(!jsonProject.isMissingNode()){
			templateRequest.setProjectId(jsonProject.path("value").asLong());			
		}else{
			throw new CaptureValidationException("ProjectId is not present");
		}
		//Populate timeCreated
		templateRequest.setVariablesChanged(false);

		//Populate shared
		templateRequest.setShared(payload.path("shared").isMissingNode() ? false : payload.path("shared").asBoolean());

		//Populate favourited
		templateRequest.setFavourited(payload.path("favourited").isMissingNode() ? false : payload.path("favourited").asBoolean());

		//Populate Source (source as it is from request body)
		templateRequest.setSource(payload);
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
			//TODO, include the attributes projectIconUrl
			//request.setProjectIconUrl(project.getSelf().toString());
		}
		if(user != null){
			request.setOwnerName(project.getLead().getName());
			request.setOwnerDisplayName(project.getLead().getDisplayName());
		}

		return request;
	}
}
