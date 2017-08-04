package util;

import com.atlassian.sal.api.net.ResponseException;

/**
 * An exception that retains the HTTP status code
 */
public class HttpResponseException extends ResponseException {
    private final int statusCode;

    public HttpResponseException(int statusCode) {
        super("HTTP status code: " + statusCode);
        this.statusCode = statusCode;
    }

    public HttpResponseException(String message, int statusCode, Throwable cause) {
        super("HTTP status code: " + statusCode + ". " + message, cause);
        this.statusCode = statusCode;
    }

    public HttpResponseException(String message, int statusCode) {
        super("HTTP status code: " + statusCode + ". " + message);
        this.statusCode = statusCode;
    }

    public HttpResponseException(int statusCode, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
