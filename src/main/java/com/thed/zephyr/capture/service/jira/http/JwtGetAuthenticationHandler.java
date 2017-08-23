package com.thed.zephyr.capture.service.jira.http;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.descriptor.AddonDescriptorLoader;
import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Make GET Http Request to JIRA,
 * Since they are not available in Request.Builder
 *
 * Created by Masud on 8/22/17.
 */
public class JwtGetAuthenticationHandler implements AuthenticationHandler {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private AtlassianHostUser host;

    //Addon Descriptor
    private AddonDescriptorLoader ad;

    public JwtGetAuthenticationHandler(AtlassianHostUser host, AddonDescriptorLoader ad) {
        this.host = host;
        this.ad = ad;
    }

    @Override
    public void configure(Request.Builder builder) {
        builder.setHeader(AUTHORIZATION_HEADER, "JWT " + createJwtToken(builder.build()));
    }

    private String createJwtToken(final Request request)  {

        try {

            long issuedAt = System.currentTimeMillis() / 1000L;
            long expiresAt = issuedAt + 180L;

            String method = request.getMethod() != null ? request.getMethod().name().toUpperCase():RequestMethod.GET.name();
            String path = request.getUri() != null ? request.getUri().getPath():"/";
            final String contextPath = "/";

            final List<NameValuePair> params = URLEncodedUtils.parse(new URI(request.getUri().toString()), "UTF-8");
            final Map<String, String[]> paramMap = new HashMap<String, String[]>();

            CaptureUtil.getParamMap(params, paramMap);

            final JwtJsonBuilder jwtBuilder = new JsonSmartJwtJsonBuilder()
                    .issuedAt(issuedAt)
                    .expirationTime(expiresAt)
                    .issuer(ad.getDescriptor().getKey());
            if(host.getUserKey().isPresent()){
                jwtBuilder.subject(host.getUserKey().get());
            }

            final CanonicalHttpUriRequest canonical = new CanonicalHttpUriRequest(method, path, contextPath, paramMap);

            JwtClaimsBuilder.appendHttpRequestClaims(jwtBuilder, canonical);
            final JwtWriterFactory jwtWriterFactory = new NimbusJwtWriterFactory();
            final String jwtbuilt = jwtBuilder.build();
            return jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256,
                    host.getHost().getSharedSecret()).jsonToJwt(jwtbuilt);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }

}