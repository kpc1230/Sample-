package com.thed.zephyr.capture.service.jira.impl;

import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Attachment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.internal.json.AttachmentJsonParser;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.Session;
import com.thed.zephyr.capture.service.PermissionService;
import com.thed.zephyr.capture.service.data.SessionActivityService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.jira.AttachmentService;
import com.thed.zephyr.capture.service.jira.UserService;
import com.thed.zephyr.capture.util.CaptureUtil;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    @Autowired
    private AtlassianHostRestClients atlassianHostRestClients;


    @Override
    public String addAttachments(MultipartFile[] multipartFiles, String issueKey) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.info("Attachment Upload request for Issue : {}", issueKey);
        final Issue issue = getJiraRestClient.getIssueClient().getIssue(issueKey).claim();
        if (issue == null) {
            throw new CaptureRuntimeException("file.error.issue.key.invalid", issueKey);
        }
        if (!permissionService.hasCreateAttachmentPermission(issue.getKey())) {
            throw new CaptureRuntimeException("file.error.attachment.permission", issueKey);
        }
        // Validate the JSON objecta
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                AttachmentInput attachmentInput = new AttachmentInput(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
                postJiraRestClient.getIssueClient().addAttachments(issue.getAttachmentsUri(), attachmentInput).claim();
            }
        } catch(IOException e) {
            throw new CaptureRuntimeException("rest.resource.malformed.json");
        } catch(Exception e) {
            log.error("Error Adding Attachment",e);
            throw new CaptureRuntimeException("Error Adding Attachment");
        }
        return CaptureUtil.getFullIconUrl(issue,host);
    }

    @Override
    public String addAttachments(String issueKey, String testSessionId, JSONArray jsonArray) throws CaptureRuntimeException, JSONException,RestClientException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        log.info("Attachment Upload request for Issue : {}", issueKey);
        Issue issue = getJiraRestClient.getIssueClient().getIssue(issueKey).claim();
        if (issue == null) {
            throw new CaptureRuntimeException("file.error.issue.key.invalid", issueKey);
        }
        if (!permissionService.hasCreateAttachmentPermission(issue.getKey())) {
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
                File imageDataTempFile = null;
                try {
                    imageDataTempFile = byteArrayToTempFile(filename,decodedImageData);
                    postJiraRestClient.getIssueClient().addAttachments(issue.getAttachmentsUri(),imageDataTempFile).claim();
                } catch (CaptureRuntimeException e) {
                    log.debug("Error creating temp file for attachment: " + e);
                    throw e;
                }   catch (Exception e) {
                    log.debug("Error creating temp file for attachment: " + e);
                    throw e;
                } finally {
                    if(imageDataTempFile != null) {
                        imageDataTempFile.delete();
                    }
                }
            }
        }
        if(StringUtils.isNotBlank(testSessionId)) {
            Session session = sessionService.getSession(testSessionId);
            Issue updatedIssue = getJiraRestClient.getIssueClient().getIssue(issueKey).claim();
            Attachment jiraAttachment = getLastUploadedAttachmentByIssue(updatedIssue);
            com.thed.zephyr.capture.model.jira.Attachment attachment = new
                    com.thed.zephyr.capture.model.jira.Attachment(jiraAttachment.getSelf(), jiraAttachment.getFilename(),
                    jiraAttachment.getAuthor().getName(), jiraAttachment.getCreationDate().getMillis(),
                    jiraAttachment.getSize(), jiraAttachment.getMimeType(),
                    jiraAttachment.getContentUri());
            sessionActivityService.addAttachment(session, updatedIssue, attachment, new Date(jiraAttachment.getCreationDate().getMillis()), attachment.getAuthor());
        }
        return CaptureUtil.getFullIconUrl(issue,host);
    }

    @Override
    public String addAttachmentsByThreads(String issueKey, String testSessionId, JSONArray jsonArray) throws CaptureRuntimeException, JSONException,RestClientException,IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        AtlassianHostUser hostUser = (AtlassianHostUser) auth.getPrincipal();
        String user = hostUser.getUserKey().get();
        if(null != user && StringUtils.isNotEmpty(user)){
            hostUser = new AtlassianHostUser(hostUser.getHost(),Optional.of(user));
        }

        log.info("Attachment Upload request for Issue : {}", issueKey);
        Issue issue = getJiraRestClient.getIssueClient().getIssue(issueKey).claim();
        if (issue == null) {
            throw new CaptureRuntimeException("file.error.issue.key.invalid", issueKey);
        }
        if (!permissionService.hasCreateAttachmentPermission(issue.getKey())) {
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
                File imageDataTempFile = null;
                try {
                    imageDataTempFile = byteArrayToTempFile(filename,decodedImageData);
                    addAttachmentToIssue(issue,imageDataTempFile,testSessionId,host.getHost().getBaseUrl(),hostUser);
                } catch (CaptureRuntimeException e) {
                    log.debug("Error creating temp file for attachment: " + e);
                    throw e;
                }   catch (Exception e) {
                    log.debug("Error creating temp file for attachment: " + e);
                    throw e;
                }
            }
        }
        return CaptureUtil.getFullIconUrl(issue,host);
    }
    private Attachment getLastUploadedAttachmentByIssue(Issue issue) throws CaptureRuntimeException {
        if(issue.getAttachments() != null) {
            Comparator<Attachment> attachmentComparator = Comparator.comparing(Attachment::getCreationDate);
            List<Attachment> attachments = new ArrayList<>();
            issue.getAttachments().forEach(attachments::add);
            Collections.sort(attachments, attachmentComparator);
            return attachments != null && attachments.size() > 0 ? attachments.get(0) : null;
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
            File file = null;
            FileOutputStream destFile = null;
            try {
                file = new File(fileName);
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

    public void addAttachmentToIssue(Issue issue, File imageDataTempFile,String testSessionId,String baseUrl,AtlassianHostUser hostUser) throws IOException {
        CompletableFuture.runAsync(() -> {
            log.debug("Thread addAttachmentToIssue started for the issue key : {} , with Attachment : {} , baseUrl : {} ", issue.getKey(), imageDataTempFile.getName(),baseUrl);
            LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            String response;
            HttpStatus httpStatus = HttpStatus.CREATED;
            try {
                map.add("file", new FileSystemResource(imageDataTempFile));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.set("X-Atlassian-Token", "nocheck");

                org.springframework.http.HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(map, headers);
                response = atlassianHostRestClients.authenticatedAs(hostUser).postForObject(issue.getAttachmentsUri(), requestEntity, String.class);
                if(response!=null){
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        JSONObject attachement=(JSONObject)jsonArray.get(0);
                        AttachmentJsonParser attachmentJsonParser = new AttachmentJsonParser();
                        Attachment jiraAttachment = attachmentJsonParser.parse(attachement);
                        if(StringUtils.isNotBlank(testSessionId)) {
                            Session session = sessionService.getSession(testSessionId);
                            try{
                               com.thed.zephyr.capture.model.jira.Attachment attachment = new
                                        com.thed.zephyr.capture.model.jira.Attachment(jiraAttachment.getSelf(), jiraAttachment.getFilename(),
                                        hostUser.getUserKey().get(), jiraAttachment.getCreationDate().getMillis(),
                                        jiraAttachment.getSize(), jiraAttachment.getMimeType(),
                                        jiraAttachment.getContentUri());
                            } catch (Exception exp) {
                                 log.error("Exception while adding the attachment to session activity "+exp.getMessage(),exp);
                            }
                        }
                    }
                }
            } catch (HttpStatusCodeException e) {
                httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
                response = e.getResponseBodyAsString();
                log.error("Exception while Attachment upload to JIRA. the issue key : {} , with Attachment : {} , baseUrl : {} ", issue.getKey(), imageDataTempFile.getName(),baseUrl);
                log.error("httpStatus : {} , response : {} ", httpStatus, response, e);
            } catch (Exception e) {
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                response = e.getMessage();
                log.error("Exception while Attachment upload to JIRA. the issue key : {} , with Attachment : {} , baseUrl : {} ", issue.getKey(), imageDataTempFile.getName(),baseUrl);
                log.error("httpStatus : {} , response : {} ", httpStatus, response, e);
            }finally {
                    if(imageDataTempFile != null) {
                        imageDataTempFile.delete();
                    }
            }
            log.debug("Thread addAttachmentToIssue completed for the issue key : {} , with Attachment : {} ", issue.getKey(), imageDataTempFile.getName());
        });
    }
}