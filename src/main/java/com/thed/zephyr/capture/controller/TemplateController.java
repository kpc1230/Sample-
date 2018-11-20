package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.TemplateBuilder;
import com.thed.zephyr.capture.model.TemplateRequest;
import com.thed.zephyr.capture.model.jira.CaptureProject;
import com.thed.zephyr.capture.model.util.TemplateSearchList;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.TemplateService;
import com.thed.zephyr.capture.service.jira.ProjectService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller class for implementing templates.
 * Created by Venkatareddy on 8/18/2017.
 */
@RestController
@RequestMapping("/templates")
public class TemplateController extends CaptureAbstractController{

	@Autowired
	private Logger log;
	@Autowired
	private TemplateService templateService;
	@Autowired
	PermissionService permissionService;
	@Autowired
	ProjectService projectService;

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> createTemplate(@RequestBody JsonNode json) throws CaptureValidationException {
		log.trace("createTemplate start...");
		TemplateRequest created;
		try {
			TemplateRequest templateRequest = parseAndValidate(json, true);
			created = templateService.createTemplate(templateRequest);
		} catch (CaptureValidationException ex) {
			log.error("Error during createTemplate.", ex);
			throw ex;
		} catch (Exception ex) {
			log.error("Error during createTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("createTemplate end for name:{}:{}", created.getName(), created.getId());
		return ok(created);
	}

	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> updateTemplate(@RequestBody JsonNode json)
			throws CaptureValidationException {
		log.trace("updateTemplate start...");
		TemplateRequest updated = null;
		try {
			TemplateRequest templateRequest = parseAndValidate(json, false);
            updated = templateService.updateTemplate(templateRequest);
		} catch (CaptureValidationException ex) {
			log.error("Error during createTemplate.", ex);
			throw ex;
		} catch (Exception ex) {
			log.error("Error during updateTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("updateTemplate end for the id:{}", updated.getId());
		return ok(updated);
	}

	@GetMapping(value = "/{templateId}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<TemplateRequest> getTemplate(@PathVariable String templateId) throws CaptureValidationException {
		log.trace("getTemplate start for the id:{}", templateId);
		TemplateRequest template = null;
		if(!StringUtils.isEmpty(templateId)) {
			try {
				template = templateService.getTemplate(getUser(),templateId);
			} catch (Exception ex) {
				log.error("Error during getTemplate.", ex);
				throw new CaptureRuntimeException(ex.getMessage());
			}
		}
		if(template == null || !canUse(template) ){
			throw new CaptureValidationException(i18n.getMessage("template.validate.update.not.exist"));
		}
		log.trace("getTemplate end for the id:{}", templateId);
		return ok(template);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteTemplate(@RequestBody JsonNode json) throws CaptureValidationException {
		log.trace("deleteTemplate start ");
		TemplateRequest templateReq = validateDelete(json);
		try {
			templateService.deleteTemplate(templateReq.getId());
		} catch (Exception ex) {
			log.error("Error during deleteTemplate.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("deleteTemplate end for the id:{}", templateReq.getId());
		return ok(templateReq);
	}

	@GetMapping(value = "/shared")
	public ResponseEntity<?> getSharedTemplates(@RequestParam Integer offset, @RequestParam Integer limit){
		log.trace("getSharedTemplates start.");
		TemplateSearchList result;
		try {
			result = templateService.getSharedTemplates(getUser(), offset, limit);
		} catch (Exception ex) {
			log.error("Error during getSharedTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("getSharedTemplates end.");
		return ok(result);
	}

	@GetMapping(value = "/favourites")
	public ResponseEntity<?> getFavouriteTemplates(@RequestParam Integer offset, @RequestParam Integer limit){
		log.trace("getFavouriteTemplates start for the user: {}");
		TemplateSearchList result;
		try {
			result = templateService.getFavouriteTemplates(getUser(), offset, limit);
		} catch (Exception ex) {
			log.error("Error during getFavouriteTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("getFavouriteTemplates end");
		return ok(result);
	}

	@PutMapping(value = "/favourites")
	public ResponseEntity<?> updateFavouriteTemplate(@RequestBody JsonNode json) {
		log.trace("updateFavouriteTemplates start");
		TemplateRequest templateRequest = null;
		try {
			String templateId = json.get("id").asText();
			Boolean flag = json.get("favourite").asBoolean(false);
			if(StringUtils.isNotEmpty(templateId)){
                templateRequest = templateService.getTemplate(getUser(), templateId);
                templateRequest.setFavourited(flag);
                templateRequest = templateService.updateTemplate(templateRequest);
            }
		} catch (Exception ex) {
			log.error("Error during updateFavouriteTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("updateFavouriteTemplates end");
		return ok(templateRequest);
	}


	@GetMapping(value = "/admin")
	public ResponseEntity<?> getAllTemplates(@RequestParam Integer offset, @RequestParam Integer limit){
		log.trace("getAllTemplates start.");
		TemplateSearchList result;
		try {
			result = templateService.getTemplates(getUser(), offset, limit);
		} catch (Exception ex) {
			log.error("Error during getAllTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("getAllTemplates end.");
		return ok(result);
	}

	@GetMapping(value = "/user")
	public ResponseEntity<?> getUserTemplates(@AuthenticationPrincipal AtlassianHostUser hostUser, @RequestParam Integer offset, @RequestParam Integer limit, @RequestParam Optional<Boolean> mine){
		log.trace("getUserTemplates start.");
        AcHostModel acHostModel = (AcHostModel)hostUser.getHost();
		TemplateSearchList result;
		try {
			result = templateService.getUserTemplates(acHostModel, hostUser.getUserKey().get(), offset, limit, mine.isPresent()? mine.get():false);
		} catch (Exception ex) {
			log.error("Error during getUserTemplates.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.trace("getUserTemplates end.");
		return ok(result);
	}

	private ResponseEntity<TemplateRequest> ok(TemplateRequest template) {
		return ResponseEntity.ok(template);
	}

	private ResponseEntity<?> ok(TemplateSearchList templates) {
		return ResponseEntity.ok(templates);
	}

	/**
	 * Parse the json and validate the json for both Create and Update template API.
	 * @param json - input json
	 * @param create - true for create API and false for update API
	 * @return - parsed json as TemplateRequest
	 * @throws CaptureValidationException
	 */
	private TemplateRequest parseAndValidate(JsonNode json, boolean create) throws CaptureValidationException {
		TemplateRequest templateRequest;
		if(create) {
			templateRequest = TemplateBuilder.parseJson(json);
		} else {
			templateRequest = TemplateBuilder.parseUpdateJson(json);
		}
		templateRequest.setOwnerName(getUser());
		templateRequest.setOwnerName(getUserAccountId());
		validateTemplate(templateRequest, create);
		return templateRequest;
	}

	private void validateTemplate(final TemplateRequest templateReqUI, boolean create) throws CaptureValidationException{
		if(StringUtils.isEmpty(templateReqUI.getName())){
			throw new CaptureValidationException(i18n.getMessage("template.validate.create.empty.name"));
		}
		//Check if project exists or not.
		CaptureProject project = (templateReqUI.getProjectId() == null ? null : projectService.getCaptureProject(templateReqUI.getProjectId()));
		if(Objects.isNull(project)) {
			throw new CaptureValidationException(i18n.getMessage("template.validate.create.cannot.browse.project"));
		}
		if(create){
			if (!permissionService.canCreateTemplate(getUser(), project)) {
				throw new CaptureValidationException(i18n.getMessage("template.validate.create.cannot.create.issue"));
			}
		}else {
			TemplateRequest existing = getTemplate(templateReqUI.getOwnerName(), templateReqUI.getId());
			if(existing == null){
				throw new CaptureValidationException(i18n.getMessage("template.validate.update.not.exist"));
			}
			if (!permissionService.canEditTemplate(getUser(), project)) {
				throw new CaptureValidationException(i18n.getMessage("template.validate.update.permission"));
			}
			templateReqUI.setProjectKey(project.getKey());
	        checkUpdateTime(templateReqUI.getTimeUpdated(), existing.getTimeUpdated());
		}
	}

	/**
	 * Validate operation for Delete API call.
	 * @param json
	 * @return
	 * @throws CaptureValidationException
	 */
	private TemplateRequest validateDelete(JsonNode json) throws CaptureValidationException{
		TemplateRequest templateReq = TemplateBuilder.parseUpdateJson(json);
		templateReq.setOwnerName(getUser());
		templateReq.setOwnerAccountId(getUserAccountId());
		validateDelete(templateReq);
		return templateReq;
	}

	private void validateDelete(TemplateRequest templateReqUI) throws CaptureValidationException{
		TemplateRequest existing = getTemplate(templateReqUI.getOwnerName(), templateReqUI.getId());
		if(existing == null){
			throw new CaptureValidationException(i18n.getMessage("template.validate.delete.not.exist"));
		}
		CaptureProject project = projectService.getCaptureProject(existing.getProjectId());
        if (project == null) {
        	throw new CaptureValidationException(i18n.getMessage("template.validate.delete.cannot.browse.project"));
        } else {
            if (!canModifyTemplate(templateReqUI.getOwnerName(), existing, project)) {
            	throw new CaptureValidationException(i18n.getMessage("template.validate.delete.permission.fail"));
            }
        }
        checkUpdateTime(templateReqUI.getTimeUpdated(), existing.getTimeUpdated());
	}

	/**
	 * Common method for Update and Delete operation to check whether the user
	 * is performing the operation on latest template.
	 * @param timeUpdated
	 * @param timeUpdated2
	 * @throws CaptureValidationException
	 */
    private void checkUpdateTime(Date timeUpdated, Date timeUpdated2) throws CaptureValidationException{
		if(timeUpdated.getTime() != timeUpdated2.getTime()){
			throw new CaptureValidationException(i18n.getMessage("template.validate.update.time.created.mismatch"));
		}
	}

	private boolean canModifyTemplate(String user, TemplateRequest existing, CaptureProject project) {
        return user.equals(existing.getOwnerName()) || (permissionService.canEditTemplate(user, project));
    }

	private TemplateRequest getTemplate(String user, String templateId){
		return StringUtils.isEmpty(templateId) ? null :
			templateService.getTemplate(user, templateId);
	}

	private boolean canUse(TemplateRequest templateReq) throws CaptureValidationException{
		return templateReq.getShared() || canModifyTemplate(getUser(), templateReq, projectService.getCaptureProject(templateReq.getProjectId()));
	}
}
