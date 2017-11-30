package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.IgnoreJwt;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.ExtensionUser;
import com.thed.zephyr.capture.model.jira.CaptureUser;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.Global.TokenHolder;
import com.thed.zephyr.capture.util.security.AESEncryptionUtils;
import com.thed.zephyr.capture.validator.ExtensionUserValidator;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

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
    ExtensionUserValidator extensionUserValidator;
    @Autowired
    DynamicProperty dynamicProperty;
    @Autowired
    private TokenHolder tokenHolder;
    @Autowired
    private AtlassianHostRepository atlassianHostRepository;

    @InitBinder("extensionUser")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(extensionUserValidator);
    }

    @IgnoreJwt
    @RequestMapping(value = "/rest/authenticate/be", method = RequestMethod.POST)
    ResponseEntity<?> validateCredentials(@Valid @RequestBody ExtensionUser extensionUser, HttpServletRequest request, Errors errors) {
        String userAgent = CaptureUtil.getUserAgent(request);
        log.debug("Validating JIRA user credentials : userAgent : " + userAgent);
        Map<String,String> respMap = new HashedMap();
        boolean success = jiraAuthService.authenticateWithJira(extensionUser.getUsername(), extensionUser.getPassword(), extensionUser.getBaseUrl());
        if (success) {
            AcHostModel host = (AcHostModel)atlassianHostRepository.findFirstByBaseUrl(extensionUser.getBaseUrl()).get();
            CaptureUser captureUser = jiraAuthService.getUserDetails(extensionUser.getUsername(), extensionUser.getPassword(), extensionUser.getBaseUrl());
            if (host.getStatus() == AcHostModel.TenantStatus.ACTIVE && captureUser != null) {
                StringBuffer buffer = new StringBuffer(host.getCtId()).append("__")
                        .append(captureUser.getKey())
                        .append("__").append(System.currentTimeMillis())
                        .append("__").append(tokenHolder.getToken())
                        .append("__").append(userAgent);
                HttpHeaders headers = new HttpHeaders();
                String encry = AESEncryptionUtils.encrypt(buffer.toString(), dynamicProperty.getStringProp(ApplicationConstants.AES_ENCRYPTION_SECRET_KEY, "password").getValue());
                log.debug("Encrypted string....... : " + encry);
                headers.add(ApplicationConstants.HEADER_PARAM_PACCESS_KEY, encry);
                respMap.put("userKey",captureUser.getKey());
                log.debug("Validating JIRA user credentials END");
                return new ResponseEntity(respMap, headers, HttpStatus.OK);
            }

        }
        log.debug("Validating JIRA user credentials END");
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }


}
