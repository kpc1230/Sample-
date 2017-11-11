package com.thed.zephyr.capture.filter;


import com.atlassian.connect.spring.AtlassianHostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;


@Component
public class MDCFilter extends OncePerRequestFilter {

    @Autowired
    private AtlassianHostRepository atlassianHostRepository;

    private static final String AUTHORIZATION_HEADER_SCHEME_PREFIX = "JWT ";
    private static final String QUERY_PARAMETER_NAME = "jwt";

    private static final Logger log = LoggerFactory.getLogger(MDCFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Optional<String> optionalJwt = getJwtFromRequest(request);
        if (optionalJwt.isPresent()) {
            String tenantTokenObject  = optionalJwt.get().split("\\.")[1];
            String tenantJsonObj = CaptureUtil.decodeBase64(tenantTokenObject);
            JsonNode obj = new ObjectMapper().readTree(tenantJsonObj);
            String tenantKey = obj.has("clientKey") ? obj.get("clientKey").textValue() : obj.get("iss").textValue();
            if(tenantKey == null){
                log.warn("The tenantKey is null from jwt:{}", optionalJwt.get());
                MDC.put(ApplicationConstants.MDC_TENANTKEY, ApplicationConstants.SYSTEM_KEY);
            } else {
                log.debug("Stored tenantKey into logger {}",tenantKey);
                MDC.put(ApplicationConstants.MDC_TENANTKEY, tenantKey);
            }
        }
        else{
            MDC.put(ApplicationConstants.MDC_TENANTKEY,ApplicationConstants.SYSTEM_KEY);
        }

        filterChain.doFilter(request,response);
    }


    private static Optional<String> getJwtFromRequest(HttpServletRequest request) {
        Optional<String> optionalJwt = getJwtFromHeader(request);
        if (!optionalJwt.isPresent()) {
            optionalJwt = getJwtFromParameter(request);
        }
        return optionalJwt;
    }

    private static Optional<String> getJwtFromHeader(HttpServletRequest request) {
        Optional<String> optionalJwt = Optional.empty();
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.isEmpty(authHeader) && authHeader.startsWith(AUTHORIZATION_HEADER_SCHEME_PREFIX)) {
            String jwt = authHeader.substring(AUTHORIZATION_HEADER_SCHEME_PREFIX.length());
            optionalJwt = Optional.of(jwt);
        }

        return optionalJwt;
    }

    private static Optional<String> getJwtFromParameter(HttpServletRequest request) {
        Optional<String> optionalJwt = Optional.empty();
        String jwt = request.getParameter(QUERY_PARAMETER_NAME);
        if (!StringUtils.isEmpty(jwt)) {
            optionalJwt = Optional.of(jwt);
        }
        return optionalJwt;
    }


}
