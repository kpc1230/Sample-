package com.thed.zephyr.capture.rest.util;

import com.thed.zephyr.capture.rest.model.RequestErrorsBean;
import com.thed.zephyr.capture.service.BonfireLicenseService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.util.concurrent.Callable;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;

/**
 * A base class to make REST resources more structured and supportable
 *
 * @since 1.7
 */
public class BonfireRestResource {

    protected final Logger log;

    @JIRAResource
    protected JiraAuthenticationContext jiraAuthenticationContext;

    @Resource(name = BonfireLicenseService.SERVICE)
    protected BonfireLicenseService bonfireLicenseService;


    protected BonfireRestResource(Class className) {
        this.log = Logger.getLogger(className);
    }

    /**
     * Invokes the callable and returns the response but with extra logging support.
     *
     * @param responseCallable the callable to invoke
     * @return the Response
     */
    protected Response response(Callable<Response> responseCallable) {
        return new RestCall(log).response(responseCallable);
    }

    /**
     * Returns some I18n text based on the key and message paramereters
     *
     * @param key    the I18n key to retrive
     * @param params the variable parameters
     * @return the I18n text
     */
    protected String getText(String key, Object... params) {
        return jiraAuthenticationContext.getI18nHelper().getText(key, params);
    }

    /**
     * Turns a set of  errors in a {@link ErrorCollection} into a {@link com.atlassian.bonfire.rest.model.RequestErrorsBean}
     *
     * @param errorCollection the input ErrorCollection
     * @return the RequestErrorsBean that was created
     */
    private RequestErrorsBean toRequestErrors(ErrorCollection errorCollection) {
        RequestErrorsBean requestErrorsBean = new RequestErrorsBean();
        for (ErrorCollection.ErrorItem err : errorCollection.getErrors()) {
            requestErrorsBean.addError(err.getMessageKeyEscaped(), err.getField());
        }
        return requestErrorsBean;
    }

    /**
     * @return true if the current user is anonymous
     */
    protected boolean isAnonymousUser() {
        return jiraAuthenticationContext.getUser() == null;
    }

    /**
     * @return the currently logged in user or null if they are anonymous
     */
    protected ApplicationUser getLoggedInUser() {
        return jiraAuthenticationContext.getUser();
    }

    /**
     * @return a newly constructed JiraServiceContext
     */
    protected JiraServiceContext buildJiraServiceContext() {
        com.atlassian.jira.util.ErrorCollection jiraErrorCollection = new SimpleErrorCollection();
        return new JiraServiceContextImpl(getLoggedInUser(), jiraErrorCollection);
    }


    /**
     * @return an error collection of licence status error messages
     */
    protected ErrorCollection getLicenseStatusErrors() {
        return bonfireLicenseService.getLicenseStatusErrors();
    }

    /**
     * @return a No Content response
     */
    protected Response noContent() {
        return Response.noContent().build();
    }

    /**
     * Sends a 200 OK response
     *
     * @param result the result object to send back
     * @return a 200 OK response
     */
    protected Response ok(Object result) {
        return Response.ok(result).cacheControl(never()).build();
    }

    /**
     * @return a CacheControl object that represents NEVER CACHE / NEVER STORE
     */
    protected CacheControl never() {
        CacheControl cacheNever = new CacheControl();
        cacheNever.setNoStore(true);
        cacheNever.setNoCache(true);
        return cacheNever;
    }

    /**
     * Returns a FORBIDDEN response with the no errors
     *
     * @return the FORBIDDEN response
     */
    protected Response forbiddenRequest() {
        return Response.status(Response.Status.FORBIDDEN).cacheControl(NO_CACHE).build();
    }

    /**
     * Returns a FORBIDDEN response with the specified errors
     *
     * @param errorCollection the colleciton of errors to respond with
     * @return the FORBIDDEN response
     */
    protected Response forbiddenRequest(ErrorCollection errorCollection) {
        return Response.status(Response.Status.FORBIDDEN).entity(toRequestErrors(errorCollection)).cacheControl(NO_CACHE).build();
    }

    /**
     * Sends a  standard FORBIDDEN response with the specified error message
     *
     * @param errKey    the error message key
     * @param msgParams the error message parameters
     * @return the FORBIDDEN response with error message
     */
    protected Response forbiddenRequest(final String errKey, final Object... msgParams) {
        RequestErrorsBean requestErrorsBean = new RequestErrorsBean();
        requestErrorsBean.addError(getText(errKey, msgParams), "data");
        return Response.status(Response.Status.FORBIDDEN).cacheControl(NO_CACHE).entity(requestErrorsBean).build();
    }


    /**
     * Sends a standard UNAUTHORIZED response when the user is anonymous
     *
     * @return the UNAUTHORIZED response with error message
     */
    protected Response unauthorizedRequest() {
        RequestErrorsBean requestErrorsBean = new RequestErrorsBean();
        requestErrorsBean.addError(getText("rest.resource.user.not.authenticated"), "user");
        return Response.status(Response.Status.UNAUTHORIZED).cacheControl(NO_CACHE).entity(requestErrorsBean).build();
    }

    /**
     * Sends a standard BAD_REQUEST response with the specified errors
     *
     * @param errorCollection the collection of errors
     * @return the BAD_REQUEST response with error messages
     */
    protected Response badRequest(final ErrorCollection errorCollection) {
        return Response.status(Response.Status.BAD_REQUEST).cacheControl(NO_CACHE).entity(toRequestErrors(errorCollection)).build();
    }

    /**
     * Sends a standard BAD_REQUEST response with the specified errors
     *
     * @param jiraErrorCollection the collection of errors
     * @return the BAD_REQUEST response with error messages
     */
    protected Response badRequest(final com.atlassian.jira.util.ErrorCollection jiraErrorCollection) {
        ErrorCollection errorCollection = new ErrorCollection();
        errorCollection.addAllJiraErrors(jiraErrorCollection);
        return badRequest(errorCollection);
    }

    /**
     * Sends a  standard BAD_REQUEST response when the JSON is malformed
     *
     * @param errKey    the error message key
     * @param msgParams the error message parameters
     * @return the BAD_REQUEST response with error message
     */
    protected Response badRequest(final String errKey, final Object... msgParams) {
        RequestErrorsBean requestErrorsBean = new RequestErrorsBean();
        requestErrorsBean.addError(getText(errKey, msgParams), "data");
        return Response.status(Response.Status.BAD_REQUEST).entity(requestErrorsBean).build();
    }


    /**
     * Virtually ALL of our rest resources want to check licence status and anonymous user
     * and return standard response if they fail.  This method allows.
     *
     * @return a non null Response if the checks fail or NULL if its ok to proceed
     */
    protected Response validateRestCall() {
        ErrorCollection licenseStatusErrors = getLicenseStatusErrors();
        if (licenseStatusErrors.hasErrors()) {
            return forbiddenRequest(licenseStatusErrors);
        }
        if (isAnonymousUser()) {
            return unauthorizedRequest();
        }
        return null;
    }

}
