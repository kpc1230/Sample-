package com.thed.zephyr.capture.validator;

import com.thed.zephyr.capture.model.ExtensionUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ExtensionUserValidator implements Validator {
    @Override
    public boolean supports(Class<?> aClass) {

        return ExtensionUser.class.equals(aClass);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        if (obj instanceof ExtensionUser) {
            ExtensionUser extensionUser = (ExtensionUser) obj;
            if (extensionUser != null) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "", "username can't be empty");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "", "password can't be empty");
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "baseUrl", "", "baseUrl can't be empty");
                String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";

                if (!(StringUtils.isNotEmpty(extensionUser.getBaseUrl()) && extensionUser.getBaseUrl().matches(urlPattern))) {
                    errors.rejectValue("baseUrl","", "Base URL is not valid");
                }
            } else {
                errors.reject("The request is not valid");
            }


        }
    }
}
