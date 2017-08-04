package com.atlassian.bonfire.rest;

import com.atlassian.bonfire.rest.model.AttachmentResponse;
import com.atlassian.bonfire.rest.util.BonfireRestResource;
import com.atlassian.bonfire.service.BonfireI18nService;
import com.atlassian.borrowed.greenhopper.jira.JIRAResource;
import com.atlassian.borrowed.greenhopper.web.ErrorCollection;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.excalibur.web.util.ExcaliburWebUtil;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.attachment.CreateAttachmentParamsBean;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.util.AttachmentException;
import com.atlassian.jira.web.util.FileNameCharacterCheckerUtil;
import com.atlassian.json.JSONArray;
import com.atlassian.json.JSONException;
import com.atlassian.json.JSONObject;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartConfig;
import com.atlassian.plugins.rest.common.multipart.MultipartConfigClass;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import webwork.config.Configuration;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * REST resource for attaching Base64 encoded PNG images to issues
 */
@Path("issue-attach")
public class IssueImageAttachmentResource extends BonfireRestResource {
    public static final String GENERIC_CONTENT_TYPE = "application/octet-stream";

    // Need an AttachmentService to check that we can create attachments.
    // And this requires a ServiceContext in order to check. Really horrid.
    @Resource
    private AttachmentService jiraAttachmentService;

    @Resource(name = ExcaliburWebUtil.SERVICE)
    private ExcaliburWebUtil excaliburWebUtil;

    @Resource(name = BonfireI18nService.SERVICE)
    private BonfireI18nService i18n;

    @JIRAResource
    private AttachmentManager jiraAttachmentManager;

    @JIRAResource
    private IssueUpdater issueUpdater;

    @JIRAResource
    private IssueService issueService;

    public IssueImageAttachmentResource() {
        super(IssueImageAttachmentResource.class);
    }

    /**
     * Multipart Form Data Implementation
     */
    @POST
    @Path("/multipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @MultipartConfigClass(BonfireAttachmentMultipartConfig.class)
    public Response uploadMultiPartAttachment(final @QueryParam("issueKey") String issueKey,
                                              final @MultipartFormParam("file") Collection<FilePart> fileParts) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                IssueService.IssueResult issueResult = issueService.getIssue(getLoggedInUser(), issueKey);

                if (!issueResult.isValid()) {
                    return badRequest("file.error.issue.key.invalid", issueKey);
                }

                Issue issue = issueResult.getIssue();

                JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(getLoggedInUser());
                if (!jiraAttachmentService.canCreateAttachments(jiraServiceContext, issue)) {
                    return badRequest("file.error.attachment.permission", issueKey);
                }

                // Now try and attach these files
                ErrorCollection requestErrors = new ErrorCollection();
                for (FilePart filePart : fileParts) {
                    try {
                        final CreateAttachmentParamsBean paramsBean = new CreateAttachmentParamsBean.Builder(getFileFromFilePart(filePart),
                                filePart.getName(), filePart.getContentType(), getLoggedInUser(), issue).build();
                        jiraAttachmentManager.createAttachment(paramsBean);
                    } catch (AttachmentException e) {
                        i18n.addError(requestErrors, "data", "file.error.attach.file.error", filePart.getName());
                    }
                }
                if (requestErrors.hasErrors()) {
                    return badRequest(requestErrors);
                }
                return noContent();
            }
        });
    }

    private File getFileFromFilePart(FilePart filePart) throws IOException {
        File file = File.createTempFile("attachment-", ".tmp");
        file.deleteOnExit();
        filePart.write(file);
        return file;
    }

    public static class BonfireAttachmentMultipartConfig implements MultipartConfig {
        public long getMaxFileSize() {
            return getMaxAttachmentSize();
        }

        public long getMaxSize() {
            return getMaxAttachmentSize() * 10;
        }
    }

    private static Integer getMaxAttachmentSize() {
        Integer maxSize;
        try {
            String maxSizeStr = Configuration.getString(APKeys.JIRA_ATTACHMENT_SIZE);
            if (maxSizeStr != null) {
                try {
                    maxSize = new Integer(maxSizeStr);
                } catch (NumberFormatException e) {
                    maxSize = Integer.MAX_VALUE;
                }
            } else {
                maxSize = Integer.MAX_VALUE;
            }
        } catch (IllegalArgumentException e1) {
            maxSize = Integer.MAX_VALUE;
        }
        return maxSize;
    }

    /**
     * Base64 Implementation
     */

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    public Response uploadAttachment(final @QueryParam("issueKey") String issueKey, final String requestBody) {
        return response(new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                Response invalidCallResponse = validateRestCall();
                if (invalidCallResponse != null) {
                    return invalidCallResponse;
                }

                IssueService.IssueResult issueResult = issueService.getIssue(getLoggedInUser(), issueKey);

                if (!issueResult.isValid()) {
                    return badRequest("file.error.issue.key.invalid", issueKey);
                }

                Issue issue = issueResult.getIssue();

                JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(getLoggedInUser());
                if (!jiraAttachmentService.canCreateAttachments(jiraServiceContext, issue)) {
                    return badRequest("file.error.attachment.permission", issueKey);
                }

                // Validate the JSON object
                final JSONArray json;
                try {
                    json = new JSONArray(requestBody);
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");

                }

                // Attach issues from JSON
                ErrorCollection requestErrors = new ErrorCollection();
                try {
                    attachFilesFromJSON(issueKey, getLoggedInUser(), issue, json, requestErrors);
                } catch (JSONException e) {
                    return badRequest("rest.resource.malformed.json");

                }

                if (requestErrors.hasErrors()) {
                    return badRequest(requestErrors);
                }

                String iconUrl = excaliburWebUtil.getFullIconUrl(issue);
                return ok(new AttachmentResponse(iconUrl));
            }
        });
    }

    private void attachFilesFromJSON(String issueKey, ApplicationUser currentUser, Issue issue, JSONArray jsonArray, ErrorCollection errorCollection) throws JSONException {
        List<ChangeItemBean> changeItemBeans = new ArrayList<ChangeItemBean>();
        JSONObject json = null;

        for (int uploadIndex = 0; uploadIndex < jsonArray.length(); uploadIndex++) {
            json = jsonArray.getJSONObject(uploadIndex);

            String filename = json.getString("fileName");
            String imageData = json.getString("fileData");
            // Only do something if the filename and imagedata exists
            if (StringUtils.isNotBlank(filename) && StringUtils.isNotBlank(imageData)) {
                String invalidChar = new FileNameCharacterCheckerUtil().assertFileNameDoesNotContainInvalidChars(filename);
                if (invalidChar != null) {
                    // TODO move this into a proper validation step
                    errorCollection.addError(i18n.getText("attachfile.error.invalidcharacter", filename, invalidChar));
                    return;
                }
                String mimeType;

                if (json.has("mimeType")) {
                    mimeType = json.getString("mimeType");
                } else {
                    mimeType = GENERIC_CONTENT_TYPE;
                }
                log.debug("Using mimetype " + mimeType);

                byte[] decodedImageData = Base64.decodeBase64(imageData);
                File imageDataTempFile;

                try {
                    imageDataTempFile = byteArrayToTempFile(decodedImageData);
                } catch (IOException e) {
                    i18n.addError(errorCollection, "data", "file.error.temp.file.create", filename);
                    log.debug("Error creating temp file for attachment: " + e);
                    return;
                }

                try {
                    final CreateAttachmentParamsBean paramsBean = new CreateAttachmentParamsBean.Builder(imageDataTempFile, filename, mimeType, currentUser,
                            issue).build();
                    changeItemBeans.add(jiraAttachmentManager.createAttachment(paramsBean));
                } catch (AttachmentException e) {
                    i18n.addError(errorCollection, "data", "file.error.attach.file.error", filename);
                    log.debug("Error creating attachment: " + e);
                    return;
                }
            }
        }

        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue, issue, EventType.ISSUE_UPDATED_ID, currentUser);
        issueUpdateBean.setChangeItems(changeItemBeans);
        issueUpdateBean.setDispatchEvent(true);
        issueUpdateBean.setParams(EasyMap.build("eventsource", IssueEventSource.ACTION));
        issueUpdater.doUpdate(issueUpdateBean, true);
    }

    /**
     * Turns a byte[] into a temporary File.
     * Stolen from the JIRA RPC plugin, and tweaked to throw checked instead of unchecked exceptions.
     *
     * @param buffer the incoming byte array
     * @return a File of those bytes
     * @throws IOException because you know how disks are
     */
    private File byteArrayToTempFile(byte buffer[]) throws IOException {
        if (buffer.length > 0) {
            FileOutputStream destFile = null;
            // Open the file and write all bytes to it.
            try {
                File tempFile = File.createTempFile("bonfire", "tmp");

                destFile = new FileOutputStream(tempFile);
                DataOutputStream fstream = new DataOutputStream(destFile);

                // Write the bytes.
                fstream.write(buffer, 0, buffer.length);
                fstream.flush();

                return tempFile;
            } finally {
                IOUtils.closeQuietly(destFile);
            }
        } else {
            return null;
        }
    }
}
