package com.thed.zephyr.capture.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Template;
import com.thed.zephyr.capture.model.TemplateBuilder;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.service.data.TemplateService;
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

	@InitBinder("templates")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(templateValidator);
	}

	@Autowired
	private TemplateService templateService;

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> createTemplate(@Valid @RequestBody TemplateRequest input, Errors errors) {
		log.info("createTemplate start for the name:" + input.getName() + input.getProjectId() + input.getIssueType());
		Template template = null;
		try {
			template = templateService.createTemplate(input);
		} catch (Exception ex) {
			log.error("Error during createTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
//			badRequest(ex.getMessage());
		}
		log.info("createTemplate end for "+ template.getName() + template.getId());
		return ok(TemplateBuilder.createTemplateRequest(template));
	}

	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> updateTemplate(@Valid @RequestBody TemplateRequest input, Errors errors) 
			throws CaptureValidationException {
		log.info("updateTemplate start for the id:{}", input.getId());
		Template updated = null;
		try {
			updated = templateService.updateTemplate(input);
		} catch (Exception ex) {
			log.error("Error during updateTemplate.", ex);
//			badRequest(ex.getMessage());
			throw new CaptureRuntimeException(ex.getMessage());
		}
		if(updated == null){
			throw new CaptureValidationException("Template can't find with id " + input.getId());
		}
		log.info("updateTemplate end for the id:{}", input.getId());
		return ok(TemplateBuilder.createTemplateRequest(updated));
	}

	@GetMapping(value = "/{templateId}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> getTemplate(@PathVariable String templateId) throws CaptureValidationException {
		log.info("getTemplate start for the id:{}", templateId);
		if(StringUtils.isEmpty(templateId)) {
			throw new CaptureValidationException("TemplateId cannot be null");
		}
		Template template = null;
		try {
			template = templateService.getTemplate(templateId);
		} catch (Exception ex) {
			log.error("Error during getTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
//			return ok(new TemplateRequest());
		}
		log.info("getTemplate end for the id:{}", templateId);
		return ok(TemplateBuilder.createTemplateRequest(template));
	}

	@SuppressWarnings({ "rawtypes" })
	@DeleteMapping(value = "/{templateId}")
	public ResponseEntity deleteTemplate(@PathVariable String templateId) throws CaptureValidationException {
		log.info("deleteTemplate for the id:{}", templateId);
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
		return ok();
	}

	private ResponseEntity<TemplateRequest> ok(TemplateRequest template) {
		return ResponseEntity.ok(template);
	}
	private ResponseEntity<Template> ok() {
		return ResponseEntity.ok().build();
	}
}

class ErrorBean {
	private String key;
	private String value;

	public ErrorBean(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public ErrorBean(ObjectError error) {
		this.key = error.getCode();
		this.value = error.getDefaultMessage();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
