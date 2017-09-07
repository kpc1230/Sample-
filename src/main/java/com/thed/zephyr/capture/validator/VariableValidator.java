package com.thed.zephyr.capture.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.thed.zephyr.capture.model.VariableRequest;
import com.thed.zephyr.capture.util.CaptureI18NMessageSource;

/**
 * Validator Class that will be invoked for create and update methods of
 * variable API.
 * 
 * @author Venkatareddy on 08/24/17.
 *
 */
@Component
public class VariableValidator implements Validator {

	@Autowired
	private CaptureI18NMessageSource i18n;

	@Override
	public boolean supports(Class<?> clazz) {
		return VariableRequest.class.equals(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		if(target instanceof VariableRequest){
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "", i18n.getMessage("variable.validate.name.empty"));
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "value", "", i18n.getMessage("variable.validate.value.empty"));
		}		
	}

}
