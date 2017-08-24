package com.thed.zephyr.capture.controller;

import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.security.AESEncryptionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by snurulla on 8/17/2017.
 */
@RestController
public class ExtensionAuthController {
    @Autowired
    private Logger log;
    @Autowired
    private JiraAuthService jiraAuthService;

    @Autowired
    DynamicProperty dynamicProperty;

    @RequestMapping(value = "/rest/authenticate/be", method = RequestMethod.POST)
    ResponseEntity validateCredentials(@RequestParam String username, @RequestParam String password, @RequestParam String baseUrl, @RequestHeader(value = "User-Agent") String userAgent) {
        log.debug("Validating JIRA user credentials : userAgent : " + userAgent);
        boolean success = jiraAuthService.authenticateWithJira(username, password, baseUrl);
        if (success) {
            AcHostModel host = jiraAuthService.getAcHostModelbyBaseUrl(baseUrl);
            if (host.getStatus() == AcHostModel.TenantStatus.ACTIVE) {
                StringBuffer buffer = new StringBuffer(host.getCtId()).append("_").append(username).append("_").append(userAgent);

                HttpHeaders headers = new HttpHeaders();
                String encry = AESEncryptionUtils.encrypt(buffer.toString(), dynamicProperty.getStringProp(ApplicationConstants.AES_ENCRYPTION_SECRET_KEY, "password").getValue());
                headers.add(ApplicationConstants.HEADER_PARAM_PACCESS_KEY, encry);
                log.debug("Encrypted string....... : " + encry);

                headers.add("accessKey", CaptureUtil.base64(buffer.toString()));
                log.debug("Validating JIRA user credentials END");
                return new ResponseEntity(headers, HttpStatus.OK);
            }

        }
        log.debug("Validating JIRA user credentials END");
        return new ResponseEntity(HttpStatus.UNAUTHORIZED);
    }


}
