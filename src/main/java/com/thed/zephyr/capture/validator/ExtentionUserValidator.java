package com.thed.zephyr.capture.validator;

import com.thed.zephyr.capture.model.ExtentionUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ExtentionUserValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {

        return ExtentionUser.class.equals(aClass);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        if (obj instanceof ExtentionUser) {
            ExtentionUser extentionUser = (ExtentionUser) obj;
            if (extentionUser != null) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "", "username can't be empty");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "", "password can't be empty");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "baseUrl", "", "baseUrl can't be empty");
                String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";

                if (!(StringUtils.isNotEmpty(extentionUser.getBaseUrl()) && extentionUser.getBaseUrl().matches(urlPattern))) {
                    errors.rejectValue("baseUrl", "Base URL is not valid");
                }
            } else {
                errors.reject("The request is not valid");
            }


        }
    }
}
