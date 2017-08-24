package com.thed.zephyr.capture.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
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
import com.thed.zephyr.capture.model.Variable;
import com.thed.zephyr.capture.service.data.VariableService;
import com.thed.zephyr.capture.validator.VariableValidator;

/**
 * Controller class for implementing variables. 
 * @author Venkatareddy on 8/24/2017.
 */
@RestController
@RequestMapping("/variables")
@Validated
public class VariableController {

	@Autowired
	private Logger log;

	@Autowired
	private VariableValidator validator;

	@InitBinder("variables")
	protected void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	@Autowired
	private VariableService variableService;

	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> addVariable(@Valid @RequestBody Variable input, Errors errors)
			throws CaptureValidationException {
		log.info("addVariableRequest start for the name:" + input.getName() + input.getValue());
		try {
			variableService.createVariable(input);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during addVariable.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("addVariable end for " + input.getName() + input.getValue());
		return created();
	}

	@PutMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> updateVariable(@Valid @RequestBody Variable input, Errors errors)
			throws CaptureValidationException {
		log.info("updateVariable start for the id:{}", input.getId());
		try {
			variableService.updateVariable(input);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during updateVariable.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("updateVariable end for the id:{}", input.getId());
		return created();
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getVariables(@RequestParam String ownerName) throws CaptureValidationException {
		log.info("getVariables start for ownerName:{}", ownerName);
		if (StringUtils.isEmpty(ownerName)) {
			throw new CaptureValidationException("ownerName cannot be null/empty");
		}
		List<Variable> list = null;
		try {
			list = variableService.getVariables(ownerName);
		} catch (Exception ex) {
			log.error("Error during getVariables.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("getVariables end for the ownerName:{}", ownerName);
		return ok(list);
	}

	@DeleteMapping(consumes = APPLICATION_JSON_VALUE)
	public ResponseEntity<?> deleteVariable(@Valid @RequestBody Variable input, Errors errors)
			throws CaptureValidationException {
		log.info("deleteVariable start for the id:{}", input.getId());
		try {
			variableService.deleteVariable(input);
		} catch (CaptureValidationException e) {
			throw e;
		} catch (Exception ex) {
			log.error("Error during deleteVariable.", ex);
			throw new CaptureRuntimeException(ex.getMessage());
		}
		log.info("deleteVariable end for the id:{}", input.getId());
		return ok();
	}

	private ResponseEntity<?> ok() {
		return ResponseEntity.ok().build();
	}

	private ResponseEntity<?> ok(List<Variable> variables) {
		Map<String, List<Variable>> map = new HashMap<>();
		map.put("variables", variables);
		return ResponseEntity.ok(map);
	}

	private ResponseEntity<?> created() {
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
