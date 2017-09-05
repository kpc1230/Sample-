package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.model.VariableRequest;
import com.thed.zephyr.capture.model.util.VariableSearchList;

/**
 * Service layer class for Variables.
 * @author Venkatareddy on 08/24/2017.
 */
public interface VariableService {
	public void createVariable(VariableRequest input) throws CaptureValidationException;
	public VariableSearchList getVariables(String userName, Integer offset, Integer limit);
	public void updateVariable(VariableRequest input) throws CaptureValidationException;
	public void deleteVariable(VariableRequest input) throws CaptureValidationException;
}