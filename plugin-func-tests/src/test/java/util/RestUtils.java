package util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RestUtils {
    private static final Logger log = LoggerFactory.getLogger(RestUtils.class);
    private static HttpClient httpClient = new HttpClient();

    /**
     * @param method to run
     * @return response of call
     */
    public static HttpResponse doCall(HttpMethod method) {
        return doCall(httpClient, method);
    }

    /**
     * @param httpClient to run method
     * @param httpMethod to run
     * @return response
     */
    public static HttpResponse doCall(HttpClient httpClient, HttpMethod httpMethod) {
        try {
            final int returnCode = httpClient.executeMethod(httpMethod);
            log.debug(httpMethod.getName() + " " + httpMethod.getURI().getURI() + "\nHttp Response code: " + returnCode);
            final String content = httpMethod.getResponseBodyAsString();
            return new HttpResponse(returnCode, httpMethod.getResponseHeaders(), content);
        } catch (final IOException e) {
            log.error("Error executing HTTP call.", e);
            return new HttpResponse(-1, null);
        } finally {
            httpMethod.releaseConnection();
        }
    }

    public static HttpResponse doPost(final String url) {
        return doCall(new PostMethod(url));
    }

    public static HttpResponse doPut(final String url) {
        return doCall(new PutMethod(url));
    }

    public static HttpResponse doGet(final String url) {
        return doCall(new GetMethod(url));
    }

    public static HttpResponse doDelete(final String url) {
        return doCall(new DeleteMethod(url));
    }
}
