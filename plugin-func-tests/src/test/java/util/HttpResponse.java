package util;

import org.apache.commons.httpclient.Header;

import javax.servlet.http.HttpServletResponse;

public class HttpResponse {
    private final int returnCode;
    private final String headers;
    private final String content;

    public HttpResponse(int returnCode, String content) {
        this(returnCode, null, content);
    }

    public HttpResponse(int returnCode, Header[] headers, String content) {
        this.content = content;
        this.headers = headers == null ? "" : String.valueOf(headers);
        this.returnCode = returnCode;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getHeaders() {
        return headers;
    }

    public String getContent() {
        return content;
    }

    public boolean isSuccessful() {
        return HttpServletResponse.SC_OK == returnCode;
    }
}