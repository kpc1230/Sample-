package com.thed.zephyr.capture.filter;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.AtlassianConnectProperties;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthenticationFilter;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.thed.zephyr.capture.model.be.BEAuthToken;
import com.thed.zephyr.capture.model.be.BEContextAuthentication;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepositoryImpl;
import com.thed.zephyr.capture.service.extension.JiraAuthService;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import com.thed.zephyr.capture.util.DynamicProperty;
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
    private DynamicProperty dynamicProperty;
    @Autowired
    private AtlassianHostRepository atlassianHostRepository;
    @Autowired
    private JiraAuthService jiraAuthService;

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
                BEAuthToken beAuthToken = jiraAuthService.createBEAuthTokenFromString(decodedKey);
                String userAgent = CaptureUtil.getUserAgent(request);
                if (beAuthToken != null && StringUtils.equals(userAgent, beAuthToken.getUserAgent())) {
                    if (!validateKeyExpiry(beAuthToken.getTimestamp())) {
                        return false;
                    }
                    AtlassianHost atlassianHost = ((DynamoDBAcHostRepositoryImpl)atlassianHostRepository).findByCtId(beAuthToken.getCtId());
                    if(null != atlassianHost) {
                        AtlassianHostUser atlassianHostUser = AtlassianHostUser.builder(atlassianHost).
                        		withUserKey(beAuthToken.getUserKey()).withUserAccountId(beAuthToken.getUserAccountId()).build();
                        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().build();
                        BEContextAuthentication beContextAuthentication = new BEContextAuthentication(atlassianHostUser, jwtClaimsSet, beAuthToken);
                        //Put JwtAuthentication into SecurityContext to mock JwtAuthenticationFilter behavior and allow RequireAuthenticationHandlerInterceptor pass request
                       SecurityContextHolder.getContext().setAuthentication(beContextAuthentication);
                        MDC.put(ApplicationConstants.MDC_TENANTKEY, atlassianHost.getClientKey());
                        return true;
                    }else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private boolean validateKeyExpiry(long timeInMilliSec) {
        long expiredIn = dynamicProperty.getLongProp(ApplicationConstants.BE_ACCESS_KEY_EXPIRATION_TIME, (15 * 24 * 60 * 60 * 1000)).get();

        return timeInMilliSec + expiredIn >= System.currentTimeMillis();
    }
}
