package com.thed.zephyr.capture.controller;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.IgnoreJwt;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.ExtensionUser;
import com.thed.zephyr.capture.model.be.BEAuthToken;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
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
    private AtlassianHostRepository atlassianHostRepository;

    @InitBinder("extensionUser")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(extensionUserValidator);
    }

    @IgnoreJwt
    @RequestMapping(value = "/rest/authenticate/be", method = RequestMethod.POST)
    ResponseEntity<?> validateCredentials(@Valid @RequestBody ExtensionUser extensionUser, HttpServletRequest request, Errors errors) {
        AcHostModel host = (AcHostModel)atlassianHostRepository.findFirstByBaseUrl(extensionUser.getBaseUrl()).get();
        String userAgent = CaptureUtil.getUserAgent(request);
        String password = CaptureUtil.decodeBase64(extensionUser.getPassword());
        Map<String,String> respMap = new HashedMap();
        BEAuthToken beAuthToken = jiraAuthService.authenticateWithJira(extensionUser.getUsername(), password, extensionUser.getBaseUrl(), userAgent);
        if (beAuthToken != null && host.getStatus() == AcHostModel.TenantStatus.ACTIVE) {
            String beKey = jiraAuthService.createStringTokenFromBEAuthToken(beAuthToken);
            HttpHeaders headers = new HttpHeaders();
            String encryptedKey = AESEncryptionUtils.encrypt(beKey, dynamicProperty.getStringProp(ApplicationConstants.AES_ENCRYPTION_SECRET_KEY, "password").getValue());
            headers.add(ApplicationConstants.HEADER_PARAM_PACCESS_KEY, encryptedKey);
            if(!CaptureUtil.isTenantGDPRComplaint()) {
                respMap.put("userKey", beAuthToken.getUserKey());
            }
            respMap.put("userAccountId",beAuthToken.getUserAccountId());
            return new ResponseEntity(respMap, headers, HttpStatus.OK);
        }

        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }


}
