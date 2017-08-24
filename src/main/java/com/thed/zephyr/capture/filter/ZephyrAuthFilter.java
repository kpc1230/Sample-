package com.thed.zephyr.capture.filter;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthenticationFilter;
import com.atlassian.connect.spring.internal.jwt.Jwt;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.AcHostModelRepository;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import com.thed.zephyr.capture.util.security.AESEncryptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ZephyrAuthFilter(AuthenticationManager authenticationManager, ServerProperties serverProperties) {
        super(authenticationManager, serverProperties);

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<String> optionalAccessKey = getAccessKeyFromHeader(request);
        if (optionalAccessKey.isPresent()) {
            boolean status = validateAccessKey(optionalAccessKey.get(), request);
            log.debug("validation on access key : "+status);
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
                String[] keyParts = decodedKey.split("_");
                log.debug("Decoded access key : " + decodedKey);
                String useragent = request.getHeader(ApplicationConstants.USER_AGENT);
                log.debug("User-Agent from request received : " + useragent);
                if (StringUtils.endsWith(decodedKey, "_" + useragent)) {
                    String clientKey = keyParts[0];
                    String userKey = keyParts[1];
                    AcHostModel acHostModel = acHostModelRepository.findOne(clientKey);
                    JwtAuthentication jwtAuthentication = new JwtAuthentication(new AtlassianHostUser(acHostModel, Optional.ofNullable(userKey)), new Jwt("", "", ""));
                    //Put JwtAuthentication into SecurityContext to mock JwtAuthenticationFilter behavior and allow RequireAuthenticationHandlerInterceptor pass request
                    SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                    return true;
                }
            }

        }
        return false;
    }


}
