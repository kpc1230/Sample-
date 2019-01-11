package com.thed.zephyr.capture.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.VariableRequest;
import com.thed.zephyr.capture.model.util.VariableSearchList;
import com.thed.zephyr.capture.service.data.VariableService;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.validator.VariableValidator;

/**
 * Controller class for implementing variables. 
 * @author Venkatareddy on 8/24/2017.
 */
@RestController
@RequestMapping("/variables")
@Validated
public class VariableController extends CaptureAbstractController{

	@Autowired
	private Logger log;

	@Autowired
	private VariableValidator validator;

	@InitBinder("variableRequest")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@Autowired
	private VariableService variableService;

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addVariable(@Valid @RequestBody VariableRequest variableRequest)
			throws CaptureValidationException {
		log.info("addVariableRequest start for the name:" + variableRequest.getName() + variableRequest.getValue());
		try {
			if(CaptureUtil.isTenantGDPRComplaint()) {
				variableRequest.setOwnerAccountId(getUserAccountId());
			} else {
				variableRequest.setOwnerName(getUser());
				variableRequest.setOwnerAccountId(getUserAccountId());
			}
			variableService.createVariable(variableRequest);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during addVariable.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("addVariable end for " + variableRequest.getName() + variableRequest.getValue());
		return noContent();
	}

	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateVariable(@Valid @RequestBody VariableRequest variableRequest)
			throws CaptureValidationException {
		log.info("updateVariable start for the id:{}", variableRequest.getId());
		try {
			if(CaptureUtil.isTenantGDPRComplaint()) {
				variableRequest.setOwnerAccountId(getUserAccountId());
			} else {
				variableRequest.setOwnerName(getUser());
				variableRequest.setOwnerAccountId(getUserAccountId());
			}
			variableService.updateVariable(variableRequest);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during updateVariable.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("updateVariable end for the id:{}", variableRequest.getId());
		return noContent();
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getVariables(@RequestParam(required = false) Integer offset , @RequestParam(required = false) Integer limit)
			throws CaptureValidationException {
		String ownerName = getUser();
		String ownerAccountId = getUserAccountId();
		log.info("getVariables start for ownerName:" + ownerName + ",ownerAccountId:"+ownerAccountId);
		VariableSearchList response = null;
		try {
			response = variableService.getVariables(ownerName, ownerAccountId, offset, limit);
		} catch (Exception ex) {
			log.error("Error during getVariables.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("getVariables end for the ownerName:{}", ownerName);
		return ok(response);
	}

	@DeleteMapping(consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteVariable(@Valid @RequestBody VariableRequest variableRequest)
			throws CaptureValidationException {
		log.info("deleteVariable start for the id:{}", variableRequest.getId());
		try {
			variableService.deleteVariable(variableRequest);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during deleteVariable.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("deleteVariable end for the id:{}", variableRequest.getId());
		return noContent();
	}
	private ResponseEntity<?> noContent(){
		return ResponseEntity.noContent().build();
	}

	private ResponseEntity<?> ok(VariableSearchList response) {
		return ResponseEntity.ok(response);
	}
}
