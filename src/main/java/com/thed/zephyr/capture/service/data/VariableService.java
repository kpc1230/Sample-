package com.thed.zephyr.capture.service.data;

import java.util.List;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.Variable;

/**
 * Service layer class for Varaibles.
 * @author Venkatareddy on 08/24/2017.
 */
public interface VariableService {
	public void createVariable(Variable input) throws CaptureValidationException;
	public List<Variable> getVariables(String userName);
	public void updateVariable(Variable input) throws CaptureValidationException;
	public void deleteVariable(Variable input) throws CaptureValidationException;
}