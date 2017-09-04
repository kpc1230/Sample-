package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.IgnoreJwt;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.ExtentionUser;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.Global.*;
import com.thed.zephyr.capture.util.security.AESEncryptionUtils;
import com.thed.zephyr.capture.validator.ExtentionUserValidator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by snurulla on 8/17/2017.
 */
@RestController
@Validated
public class ExtensionAuthController {
    @Autowired
    private Logger log;
    @Autowired
    private JiraAuthService jiraAuthService;

    @Autowired
    ExtentionUserValidator extentionUserValidator;

    @Autowired
    DynamicProperty dynamicProperty;

    @Autowired
    private TokenHolder tokenHolder;

    @InitBinder("extentionUser")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(extentionUserValidator);
    }

    @IgnoreJwt
    @RequestMapping(value = "/rest/authenticate/be", method = RequestMethod.POST)
    ResponseEntity validateCredentials(@Valid @RequestBody ExtentionUser extentionUser, @RequestHeader(value = "User-Agent") String userAgent, Errors errors) {
        log.debug("Validating JIRA user credentials : userAgent : " + userAgent);
        boolean success = jiraAuthService.authenticateWithJira(extentionUser.getUsername(), extentionUser.getPassword(), extentionUser.getBaseUrl());
        if (success) {
            AcHostModel host = jiraAuthService.getAcHostModelbyBaseUrl(extentionUser.getBaseUrl());
            if (host.getStatus() == AcHostModel.TenantStatus.ACTIVE) {
                StringBuffer buffer = new StringBuffer(host.getCtId()).append("__")
                        .append(extentionUser.getUsername())
                        .append("__").append(System.currentTimeMillis())
                        .append("__").append(tokenHolder.getToken())
                                .append("__").append(userAgent);
                HttpHeaders headers = new HttpHeaders();
                String encry = AESEncryptionUtils.encrypt(buffer.toString(), dynamicProperty.getStringProp(ApplicationConstants.AES_ENCRYPTION_SECRET_KEY, "password").getValue());
                log.debug("Encrypted string....... : " + encry);
                headers.add(ApplicationConstants.HEADER_PARAM_PACCESS_KEY, encry);
                log.debug("Validating JIRA user credentials END");
                return new ResponseEntity(headers, HttpStatus.OK);
            }

        }
        log.debug("Validating JIRA user credentials END");
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }


}
