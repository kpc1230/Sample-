package com.thed.zephyr.capture.filter;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.AtlassianConnectProperties;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthenticationFilter;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.Global.TokenHolder;
import com.thed.zephyr.capture.util.security.AESEncryptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by snurulla on 8/21/2017.
 */
@Component
public class ZephyrAuthFilter extends JwtAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(ZephyrAuthFilter.class);

    @Autowired
    DynamicProperty dynamicProperty;
    @Autowired
    private AcHostModelRepository acHostModelRepository;
    @Autowired
    private TokenHolder tokenHolder;

    public ZephyrAuthFilter(AuthenticationManager authenticationManager,
                                   AddonDescriptorLoader addonDescriptorLoader,
                                   AtlassianConnectProperties atlassianConnectProperties,
                                   ServerProperties serverProperties) {
        super(authenticationManager,addonDescriptorLoader,atlassianConnectProperties,serverProperties);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> optionalAccessKey = getAccessKeyFromHeader(request);
        if (optionalAccessKey.isPresent()) {
            boolean status = validateAccessKey(optionalAccessKey.get(), request);
            log.debug("validation on access key : " + status);
            filterChain.doFilter(request, response);

        } else {
            super.doFilterInternal(request, response, filterChain);
        }
    }

    private static Optional<String> getAccessKeyFromHeader(HttpServletRequest request) {
        Optional<String> optionalAccessKey = Optional.empty();
        String authHeader = request.getHeader(ApplicationConstants.HEADER_PARAM_PACCESS_KEY);
        if (!StringUtils.isEmpty(authHeader)) {
            optionalAccessKey = Optional.of(authHeader);
        }

        return optionalAccessKey;
    }

    private boolean validateAccessKey(String accessKey, HttpServletRequest request) {
        if (!StringUtils.isEmpty(accessKey)) {
            String decodedKey = AESEncryptionUtils.decrypt(accessKey, dynamicProperty.getStringProp(ApplicationConstants.AES_ENCRYPTION_SECRET_KEY, "password").getValue());
            if (StringUtils.isNotBlank(decodedKey)) {
                String[] keyParts = decodedKey.split("__");
                log.debug("Decoded access key : " + decodedKey);
                String useragent = CaptureUtil.getUserAgent(request);
                log.debug("User-Agent from request received : " + useragent);
                if (StringUtils.endsWith(decodedKey, "__" + useragent)) {
                    String clientKey = keyParts[0];
                    String userKey = keyParts[1];
                    String timeInMilliSec = keyParts[2];
                    String jiraToken = keyParts[3];
                    tokenHolder.setTokenKey(jiraToken);
                    if (!validateKeyExpiry(timeInMilliSec)) {
                        return false;
                    }
                    AcHostModel acHostModel = acHostModelRepository.findOne(clientKey);
                    if(null != acHostModel) {
                        JwtAuthentication jwtAuthentication = new JwtAuthentication(new AtlassianHostUser(acHostModel, Optional.ofNullable(userKey)), new JWTClaimsSet());
                        //Put JwtAuthentication into SecurityContext to mock JwtAuthenticationFilter behavior and allow RequireAuthenticationHandlerInterceptor pass request
                       SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                        MDC.put(ApplicationConstants.MDC_TENANTKEY, acHostModel.getClientKey());
                        return true;
                    }else {
                        return false;
                    }
                }
            }

        }
        return false;
    }

    private boolean validateKeyExpiry(String timeInMilliSec) {
        long keyGeneratedTime = Long.valueOf(timeInMilliSec);
        long expiredIn = dynamicProperty.getLongProp(ApplicationConstants.BE_ACCESS_KEY_EXPIRATION_TIME, (15 * 24 * 60 * 60 * 1000)).get();
        if (keyGeneratedTime + expiredIn >= System.currentTimeMillis()) {
            return true;
        }
        return false;
    }


}
