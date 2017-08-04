package com.atlassian.bonfire.rest.util;

import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

/**
 * JIRA by default does not log REST call 500 failures in atlassian-jira.log.  So we now have a wrapper invoker class
 * to make the rest call and log any errors.  Thjis will ensure that they go to the JIRA log and the
 * Bonfire log (if you use a com.atlassian.bonfire logger say) and this will help with support as well.
 *
 * @since 1.7
 */
public class RestCall {
    private final Logger logger;

    public RestCall(Logger logger) {
        this.logger = logger;
    }

    /**
     * This will invoke the Callable and return the response to the callee.  If an exception occurs it will
     * log that into the appropriate logger.
     *
     * @param callable the REST call code that will respond to the rest call
     * @return a REST {@link Response}
     */
    public Response response(Callable<Response> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            if (e instanceof WebApplicationException) {
                WebApplicationException webApplicationException = (WebApplicationException) e;
                if (webApplicationException.getResponse() != null) {
                    //
                    // BON-687 the code has build a response, full of error collections not doubt
                    // and hence we should send it on.  We info log it for support reasons
                    //
                    logger.info("Unable to complete Bonfire REST method " + e.getMessage());

                    return webApplicationException.getResponse();
                }
            }

            logger.error("Unable to complete Bonfire REST method ", e);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
