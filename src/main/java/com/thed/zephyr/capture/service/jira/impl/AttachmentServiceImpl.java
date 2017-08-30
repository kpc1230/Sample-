package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.util.concurrent.Promise;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.AttachmentService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.FileNameCharacterCheckerUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by niravshah on 8/25/17.
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {
    @Autowired
    private Logger log;

    @Autowired
    @Qualifier("jiraRestClientPOST")
    private JiraRestClient postJiraRestClient;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JiraRestClient getJiraRestClient;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionActivityService sessionActivityService;

    @Autowired
    private SessionService sessionService;

    @Override
    public String addAttachments(MultipartFile[] multipartFiles, String issueKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.info("Attachment Upload request for Issue : {}", issueKey);
        final Issue issue = getJiraRestClient.getIssueClient().getIssue(issueKey).claim();
        if (issue == null) {
            throw new CaptureRuntimeException("file.error.issue.key.invalid", issueKey);
        }
        if (!permissionService.hasCreateAttachmentPermission(issue)) {
            throw new CaptureRuntimeException("file.error.attachment.permission", issueKey);
        }
        // Validate the JSON objecta
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                AttachmentInput attachmentInput = new AttachmentInput(multipartFile.getOriginalFilename(),multipartFile.getInputStream());
                postJiraRestClient.getIssueClient().addAttachments(issue.getAttachmentsUri(), attachmentInput).claim();
            }
        } catch(IOException e) {
            throw new CaptureRuntimeException("rest.resource.malformed.json");
        } catch(Exception e) {
           log.error("Error Adding Attachment",e);
            throw new CaptureRuntimeException("Error Adding Attachment");
        }
        return getFullIconUrl(issue);
    }

    @Override
    public String addAttachments(String issueKey, String testSessionId, JSONArray jsonArray) throws CaptureRuntimeException, JSONException {
        log.info("Attachment Upload request for Issue : {}", issueKey);
        final Issue issue = getJiraRestClient.getIssueClient().getIssue(issueKey).claim();
        if (issue == null) {
            throw new CaptureRuntimeException("file.error.issue.key.invalid", issueKey);
        }
        if (!permissionService.hasCreateAttachmentPermission(issue)) {
            throw new CaptureRuntimeException("file.error.attachment.permission", issueKey);
        }
        JSONObject json = null;

        for (int uploadIndex = 0; uploadIndex < jsonArray.length(); uploadIndex++) {
            json = jsonArray.getJSONObject(uploadIndex);

            String filename = json.getString("fileName");
            String imageData = json.getString("fileData");
            // Only do something if the filename and imagedata exists
            if (StringUtils.isNotBlank(filename) && StringUtils.isNotBlank(imageData)) {
                String invalidChar = new FileNameCharacterCheckerUtil().assertFileNameDoesNotContainInvalidChars(filename);
                if (invalidChar != null) {
                    throw new CaptureRuntimeException("attachfile.error.invalidcharacter", filename);
                }
                byte[] decodedImageData = Base64.decodeBase64(imageData);
                File imageDataTempFile;
                try {
                    imageDataTempFile = byteArrayToTempFile(filename,decodedImageData);
                    imageDataTempFile.renameTo(new File(filename));
                    postJiraRestClient.getIssueClient().addAttachments(issue.getAttachmentsUri(),imageDataTempFile);
                } catch (CaptureRuntimeException e) {
                    log.debug("Error creating temp file for attachment: " + e);
                    throw e;
                }
            }
        }
//        if(StringUtils.isNotBlank(testSessionId)) {
//            Session session = sessionService.getSession(testSessionId);
//            Promise<Issue> responsePromise = getJiraRestClient.getIssueClient().getIssue(issueKey);
//            if(responsePromise.isDone()) {
//                log.debug("Retrieved Issue:");
//                Attachment jiraAttachment = getLastUploadedAttachmentByIssue(responsePromise.claim());
//                com.thed.zephyr.capture.model.jira.Attachment attachment = new
//                        com.thed.zephyr.capture.model.jira.Attachment(jiraAttachment.getSelf(), jiraAttachment.getFilename(),
//                        jiraAttachment.getAuthor().getName(), jiraAttachment.getCreationDate().getMillis(),
//                        jiraAttachment.getSize(), jiraAttachment.getMimeType(),
//                        jiraAttachment.getContentUri());
//                sessionActivityService.addAttachment(session, responsePromise.claim(), attachment, jiraAttachment.getCreationDate(), attachment.getAuthor());
//            }
//        }
        return getFullIconUrl(issue);
    }


    private Attachment getLastUploadedAttachmentByIssue(Issue issue) throws CaptureRuntimeException {
        if(issue.getAttachments() != null) {
            Comparator<Attachment> attachmentComparator = Comparator.comparing(Attachment::getCreationDate);
            List<Attachment> attachments = new ArrayList<>();
            issue.getAttachments().forEach(attachments::add);
            Collections.sort(attachments, attachmentComparator);
            return attachments != null && attachments.size() > 1 ? attachments.get(0) : null;
        }
        return null;
    }

    /**
     * Turns a byte[] into a temporary File.
     * Stolen from the JIRA RPC plugin, and tweaked to throw checked instead of unchecked exceptions.
     *
     * @param buffer the incoming byte array
     * @return a File of those bytes
     * @throws IOException because you know how disks are
     */
    private File byteArrayToTempFile(String fileName, byte buffer[]) throws CaptureRuntimeException {
        if (buffer.length > 0) {
            FileOutputStream destFile = null;
            try {
                File file = new File(fileName);
                destFile = new FileOutputStream(file);
                IOUtils.write(buffer,destFile);
                return file;
            } catch (Exception e) {
                log.error("Error Creating File:",e);
                throw new CaptureRuntimeException("Error Creating File");
            } finally {
                IOUtils.closeQuietly(destFile);
            }
        } else {
            return null;
        }
    }


    /**
     * There are two cases, remote icon or within jira. The remote icon can be returned as is while the jira one will need the baseURL added to the
     * front
     *
     * @param i issue
     * @return full url to the image
     */
    public String getFullIconUrl(Issue i) {
        return getFullIconUrl(i.getIssueType());
    }

    public String getFullIconUrl(IssueType it) {
        URI iconUrl = it.getIconUri();
        return iconUrl.toString();
    }
}
