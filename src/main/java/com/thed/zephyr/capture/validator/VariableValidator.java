package com.thed.zephyr.capture.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.thed.zephyr.capture.model.Variable;

/**
 * Validator Class that will be invoked for create and update methods of
 * variable API.
 * 
 * @author Venkatareddy on 08/24/17.
 *
 */
@Component
public class VariableValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Variable.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target instanceof Variable){
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "", "Variable name can't be empty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "value", "", "Variable value can't be empty");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ownerName", "", "Variable Owner name can't be empty");
		}		
	}

}
