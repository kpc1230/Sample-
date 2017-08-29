package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.jira.AttachmentService;
import com.thed.zephyr.capture.service.jira.http.CJiraRestClientFactory;
import com.thed.zephyr.capture.util.FileNameCharacterCheckerUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Created by niravshah on 8/25/17.
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {
    @Autowired
    private Logger log;

    @Autowired
    private CJiraRestClientFactory cJiraRestClientFactory;

    @Autowired
    private PermissionService permissionService;


    @Autowired
    private JiraRestClient jiraRestClient;

    @Override
    public String addAttachments(MultipartFile[] multipartFiles, String issueKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.info("Attachment Upload request for Issue : {}", issueKey);
        final Issue issue = jiraRestClient.getIssueClient().getIssue(issueKey).claim();
        if (issue == null) {
            throw new CaptureRuntimeException("file.error.issue.key.invalid", issueKey);
        }
        if (!permissionService.canCreateAttachments(host.getUserKey(), issue)) {
            throw new CaptureRuntimeException("file.error.attachment.permission", issueKey);
        }
        // Validate the JSON objecta
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                AttachmentInput attachmentInput = new AttachmentInput(multipartFile.getOriginalFilename(),multipartFile.getInputStream());
                cJiraRestClientFactory.createJiraPostRestClient(host,host.getUserKey()).getIssueClient().addAttachments(issue.getAttachmentsUri(), attachmentInput).claim();
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
    public String addAttachments(String issueKey, JSONArray jsonArray) throws CaptureRuntimeException, JSONException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.info("Attachment Upload request for Issue : {}", issueKey);
        final Issue issue = jiraRestClient.getIssueClient().getIssue(issueKey).claim();
        if (issue == null) {
            throw new CaptureRuntimeException("file.error.issue.key.invalid", issueKey);
        }
        if (!permissionService.canCreateAttachments(host.getUserKey(), issue)) {
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
                    cJiraRestClientFactory.createJiraPostRestClient(host,host.getUserKey()).getIssueClient().addAttachments(issue.getAttachmentsUri(),imageDataTempFile);
                } catch (CaptureRuntimeException e) {
                    log.debug("Error creating temp file for attachment: " + e);
                    throw e;
                }
            }
        }
        return getFullIconUrl(issue);
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
