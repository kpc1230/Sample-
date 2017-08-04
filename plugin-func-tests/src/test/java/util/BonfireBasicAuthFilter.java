package util;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class BonfireBasicAuthFilter extends ClientFilter {
    private static final String AUTHORIZATION = "Authorization";
    private final String authentication;

    /**
     * Creates a new HTTP Basic Authentication filter using provided username and password credentials
     *
     * @param username
     * @param password
     */
    public BonfireBasicAuthFilter(final String username, final String password) {
        try {
            String userpass = username + ":" + password;
            authentication = "Basic " + new String(Base64.encodeBase64(userpass.getBytes()), "ASCII");
        } catch (UnsupportedEncodingException ex) {
            // This should never occur
            throw new RuntimeException(ex);
        }
    }

    public String getAuthenticationString() {
        return authentication;
    }

    @Override
    public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {

        if (!cr.getMetadata().containsKey(AUTHORIZATION)) {
            cr.getMetadata().add(AUTHORIZATION, authentication);
        }
        return getNext().handle(cr);
    }
}
