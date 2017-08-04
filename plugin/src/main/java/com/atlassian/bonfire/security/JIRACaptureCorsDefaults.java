package com.atlassian.bonfire.security;

import com.atlassian.plugins.rest.common.security.CorsHeaders;
import com.atlassian.plugins.rest.common.security.descriptor.CorsDefaults;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * CORS descriptor to whitelist Chrome and Safari browser extension referrers.
 *
 * @since v2.9.4
 */
public class JIRACaptureCorsDefaults implements CorsDefaults {
    private static final String[] ALLOWED_ORIGINS = {"chrome-extension://", "safari-extension://"};

    @Override
    public boolean allowsCredentials(String origin) throws IllegalArgumentException {
        return allowsOrigin(origin);
    }

    @Override
    public boolean allowsOrigin(String origin) throws IllegalArgumentException {
        if (origin == null) {
            return false;
        }
        for (String allowedOrigin : ALLOWED_ORIGINS) {
            if (origin.startsWith(allowedOrigin)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getAllowedRequestHeaders(String origin) throws IllegalArgumentException {
        return ImmutableSet.of(CorsHeaders.ORIGIN.value());
    }

    @Override
    public Set<String> getAllowedResponseHeaders(String origin) throws IllegalArgumentException {
        return ImmutableSet.of(CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN.value());
    }
}