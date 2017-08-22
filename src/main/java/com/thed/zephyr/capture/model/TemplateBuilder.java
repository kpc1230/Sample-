package com.thed.zephyr.capture.model;

import java.util.Date;

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
	public static Template constructTemplate(TemplateRequest templateRequest){
		Template template = new Template();
		template.setName(templateRequest.getName());
		template.setProjectId(templateRequest.getProjectId());
		template.setFavourite(templateRequest.getFavourited());
		template.setShared(templateRequest.getShared());
		template.setCreatedBy(templateRequest.getCreatedBy());
		template.setCreatedOn(new Date());
		template.setContent(templateRequest.getSource());
		return template;
	}
	/**
	 * Update Template object with the modified values from templateRequest. 
	 * @param template - Existing template object
	 * @param templateRequest - Values to be taken into Template object.
	 * @return - Modified Template to be persisted.
	 */
	public static Template updateTemplate(Template template, TemplateRequest templateRequest){
		template.setName(templateRequest.getName());
		template.setProjectId(templateRequest.getProjectId());
		template.setFavourite(templateRequest.getFavourited());
		template.setShared(templateRequest.getShared());
		template.setCreatedBy(templateRequest.getCreatedBy());
		template.setCreatedOn(new Date());
		template.setContent(templateRequest.getSource());
		return template;
	}
	
	/**
	 * Create a TemplateRequest object using the persisted template object to use on UI.
	 * @param template - Persisted Template Object.
	 * @return - TemplateRequest Transfer Object created out of template.
	 */
	public static TemplateRequest createTemplateRequest(Template template){
		TemplateRequest tr = new TemplateRequest();
		tr.setId(template.getId());
		tr.setName(template.getName());
		tr.setProjectId(template.getProjectId());
		tr.setFavourited(template.getFavourite());
		tr.setShared(template.getShared());
		tr.setCreatedBy(template.getCreatedBy());
		tr.setTimeCreated(template.getCreatedOn().toString());
		tr.setSource(template.getContent());
		return tr;
	}
}
