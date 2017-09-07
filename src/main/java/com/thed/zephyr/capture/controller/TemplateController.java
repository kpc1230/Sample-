package com.thed.zephyr.capture.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Set;

import javax.validation.Valid;

import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Template;
import com.thed.zephyr.capture.model.TemplateBuilder;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.model.util.TemplateSearchList;
import com.thed.zephyr.capture.service.data.TemplateService;
import com.thed.zephyr.capture.service.data.VariableService;
import com.thed.zephyr.capture.validator.TemplateValidator;

/**
 * Controller class for implementing templates.
 * Created by Venkatareddy on 8/18/2017.
 */
@RestController
@RequestMapping("/templates")
@Validated
public class TemplateController {

	@Autowired
	private Logger log;

	@Autowired
	private TemplateValidator templateValidator;

	@InitBinder("templateRequest")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(templateValidator);
	}

	@Autowired
	private TemplateService templateService;

	@Autowired
	private VariableService variableService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	ProjectService projectService;

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> createTemplate(@RequestBody JsonNode json) {
		//log.info("createTemplate start for the name:" + templateRequest.getName() + templateRequest.getProjectId() + templateRequest.getIssueType());
		TemplateRequest created = null;
		try {

			TemplateRequest templateRequest = TemplateBuilder.parseJson(json);
			templateRequest.setOwnerName(getUser());
			if (!permissionService.canCreateTemplate(getUser(), projectService.getCaptureProject(templateRequest.getProjectId()))) {
                throw new CaptureValidationException("template.validate.create.cannot.create.issue");
			}
			created = templateService.createTemplate(templateRequest);
		} catch (Exception ex) {
			log.error("Error during createTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("createTemplate end for name:"+ created.getName() + ":" + created.getId());
		return ok(created);
	}

	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> updateTemplate(@Valid @RequestBody TemplateRequest templateRequest)
			throws CaptureValidationException {
		log.info("updateTemplate start for the id:{}", templateRequest.getId());
		TemplateRequest updated = null;
		try {
            if (!permissionService.canEditTemplate(getUser(), projectService.getCaptureProject(templateRequest.getProjectId()))) {
                throw new CaptureValidationException("template.validate.create.cannot.create.issue");
            }
            updated = templateService.updateTemplate(templateRequest);
		} catch (Exception ex) {
			log.error("Error during updateTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		if(updated == null){
			throw new CaptureValidationException("Template can't find with id " + templateRequest.getId());
		}
		log.info("updateTemplate end for the id:{}", templateRequest.getId());
		return ok(updated);
	}

	@GetMapping(value = "/{templateId}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> getTemplate(@PathVariable String templateId) throws CaptureValidationException {
		log.info("getTemplate start for the id:{}", templateId);
		if(StringUtils.isEmpty(templateId)) {
			throw new CaptureValidationException("TemplateId cannot be null");
		}
		TemplateRequest template = null;
		try {
			template = templateService.getTemplate(getUser(),templateId);
		} catch (Exception ex) {
			log.error("Error during getTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
//			return ok(new TemplateRequest());
		}
		log.info("getTemplate end for the id:{}", templateId);
		return ok(template);
	}

	@SuppressWarnings({ "rawtypes" })
	@DeleteMapping(value = "/{templateId}")
	public ResponseEntity deleteTemplate(@PathVariable String templateId) throws CaptureValidationException {
		log.info("deleteTemplate start for the id:{}", templateId);
		if(StringUtils.isEmpty(templateId)) {
			throw new CaptureValidationException("TemplateId cannot be null");
		}
		try {
			templateService.deleteTemplate(templateId);
		} catch (Exception ex) {
			log.error("Error during deleteTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
//			return badRequest(ex.getMessage());
		}
		log.info("deleteTemplate end for the id:{}", templateId);
		return ok();
	}

	@GetMapping(value = "/shared")
	public ResponseEntity<?> getSharedTemplates(@RequestParam Integer offset, @RequestParam Integer limit){
		log.info("getSharedTemplates start.");
		TemplateSearchList result = null;
		try {
			result = templateService.getSharedTemplates(getUser(), offset, limit);
		} catch (Exception ex) {
			log.error("Error during getSharedTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("getSharedTemplates end.");
		return ok(result);
	}

	@GetMapping(value = "/favourites")
	public ResponseEntity<?> getFavouriteTemplates(
			@RequestParam Integer offset, @RequestParam Integer limit){
		log.info("getFavouriteTemplates start for the user: {}");
		TemplateSearchList result = null;
		try {
			result = templateService.getFavouriteTemplates(getUser(), offset, limit);
		} catch (Exception ex) {
			log.error("Error during getFavouriteTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("getFavouriteTemplates end for the user: {}");
		return ok(result);
	}

	@GetMapping(value = "/admin")
	public ResponseEntity<?> getAllTemplates(@RequestParam Integer offset, @RequestParam Integer limit){
		log.info("getAllTemplates start.");
		TemplateSearchList result = null;
		try {
			result = templateService.getTemplates(getUser(), offset, limit);
		} catch (Exception ex) {
			log.error("Error during getAllTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("getAllTemplates end.");
		return ok(result);
	}

	@GetMapping(value = "/user")
	public ResponseEntity<?> getUserTemplates(@RequestParam Integer offset, @RequestParam Integer limit){
		log.info("getUserTemplates start.");
		TemplateSearchList result = null;
		try {
			result = templateService.getUserTemplates(getUser(), offset, limit);
		} catch (Exception ex) {
			log.error("Error during getUserTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("getUserTemplates end.");
		return ok(result);
	}

	private ResponseEntity<TemplateRequest> ok(TemplateRequest template) {
		return ResponseEntity.ok(template);
	}
	private ResponseEntity<Template> ok() {
		return ResponseEntity.ok().build();
	}
	private ResponseEntity<?> ok(TemplateSearchList templates) {
		return ResponseEntity.ok(templates);
	}
	/**
	 * Fetches the user key from the authentication object.
	 *
	 * @return -- Returns the logged in user key.
	 * @throws CaptureValidationException -- Thrown while fetching the user key.
	 */
	protected String getUser() throws CaptureValidationException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
		String userKey = host.getUserKey().get();
		if(StringUtils.isBlank(userKey)) {
			throw new CaptureValidationException("User is not logged in");
		}
		return userKey;
	}

}
