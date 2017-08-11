package com.thed.zephyr.capture.connect;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.request.jwt.JwtBuilder;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by aliakseimatsarski on 10/20/16.
 */

@Component
public class ConnectRequestInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private Logger log;

    @Autowired
    private DynamicProperty dynamicProperty;

    @Autowired
    private Environment env;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null){
            log.warn("Warning during intercept outgoing request. Can't sign request without authenticated user.");
            return execution.execute(request, body);
        }
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.debug("Triggering interceptor........ tenantKey:{}", host.getHost().getClientKey());
        HttpRequest interceptedRequest = wrapRequest(request, host);
        return execution.execute(interceptedRequest, body);
    }

    private HttpRequest wrapRequest(HttpRequest request, AtlassianHostUser hostUser) {
        String jwt = createJwt(request.getMethod(), request.getURI(), hostUser);
        URI uri = wrapUri(request, hostUser);
        return new JwtSignedHttpRequestWrapper(request, jwt, uri);
    }

    protected String createJwt(HttpMethod method, URI uri, AtlassianHostUser hostUser) {
        String connectBaseUrl = dynamicProperty.getStringProp(ApplicationConstants.CAPTURE_CONNECT_BASE_URL, env.getProperty(ApplicationConstants.CAPTURE_CONNECT_BASE_URL)).getValue();
        try {
            final URI baseUrl = new URI(connectBaseUrl);
            final String productContext = baseUrl.getPath();
            String pathWithoutProductContext = uri.getPath().substring(productContext.length());
            URI uriWithoutProductContext = new URI(pathWithoutProductContext);
            return createJwtToken(uriWithoutProductContext, method, hostUser);
        } catch (URISyntaxException exception) {
            log.error("Error during sign request with jwt.", exception);
        }

        return null;
    }

    public String createJwtToken(URI uri, HttpMethod httpMethod, AtlassianHostUser hostUser) {
//        ZfjCloudAcHostModel zfjCloudAcHostModel = riakAcHostRepository.getZfjCloudAcHostModel(hostUser.getHost().getClientKey());
//        JwtBuilder jwtBuilder = new JwtBuilder()
//                .issuer(hostUser.getHost().getClientKey())
//                        // .audience(host.getClientKey()) -- TODO Figure out whether we can / should set this?
//                .queryHash(httpMethod, uri, hostUser.getHost().getBaseUrl())
//                .signature(zfjCloudAcHostModel.getSharedSecret());
        //maybeIncludeJwtSubjectClaim(jwtBuilder, hostUser);
        //return jwtBuilder.build();
        return "";
    }

    private JwtBuilder maybeIncludeJwtSubjectClaim(JwtBuilder jwtBuilder, AtlassianHostUser hostUser) {
        if (includeJwtSubjectClaim() && hostUser.getUserKey().isPresent()) {
            jwtBuilder.subject(hostUser.getUserKey().get());
        }
        return jwtBuilder;
    }

    private boolean includeJwtSubjectClaim() {
        // TODO Check a property
        return false;
    }

    private URI wrapUri(HttpRequest request, AtlassianHostUser hostUser) {
        URI uri = request.getURI();
        if (!uri.isAbsolute()) {
            URI baseUri = URI.create(hostUser.getHost().getBaseUrl());
            uri = baseUri.resolve(getUriToResolve(baseUri, uri));
        }
        return uri;
    }

    private URI getUriToResolve(URI baseUri, URI uri) {
        String pathToResolve = "";
        String baseUriPath = baseUri.getPath();
        if (baseUriPath != null) {
            pathToResolve += baseUriPath;
        }
        String path = uri.getPath();
        if (path != null) {
            String pathToAppend = (pathToResolve.endsWith("/") && path.startsWith("/")) ? path.substring(1) : path;
            pathToResolve += pathToAppend;
        }

        try {
            uri = new URI(null, null, pathToResolve, uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        return uri;
    }

    private class JwtSignedHttpRequestWrapper extends HttpRequestWrapper {

        private final String jwt;
        private final URI uri;

        public JwtSignedHttpRequestWrapper(HttpRequest request, String jwt, URI uri) {
            super(request);
            this.jwt = jwt;
            this.uri = uri;

            setJwtHeaders();
        }

        @Override
        public URI getURI() {
            return uri;
        }

        private void setJwtHeaders() {
            HttpHeaders headers = super.getHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, String.format("JWT %s", jwt));
         //   headers.add(HttpHeaders.USER_AGENT, String.format("%s/%s", USER_AGENT_PRODUCT, atlassianConnectClientVersion));
        }
    }
}
