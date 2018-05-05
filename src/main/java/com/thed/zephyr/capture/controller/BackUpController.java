package com.thed.zephyr.capture.controller;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.data.BackUpService;
import com.thed.zephyr.capture.util.UniqueIdGenerator;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/backup")
public class BackUpController {
    @Autowired
    private Logger log;
    @Autowired
    private BackUpService backUpService;

    @GetMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> createBackUp(@RequestParam("fileName") String fileName, @AuthenticationPrincipal AtlassianHostUser hostUser) {
        try {
            log.info("createBackUp request processes started with file nane : {} ", fileName);
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            String jobProgressId = new UniqueIdGenerator().getStringId();
            backUpService.createBackUp(acHostModel, fileName, jobProgressId, getUser());
            Map<String, String> response = new HashMap<>();
            response.put("jobProgressId", jobProgressId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Erro in createBackUp ", ex);
            throw new CaptureRuntimeException(ex);
        }
    }

    @GetMapping(value = "/restore", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> restoreFromBackUp(@RequestParam("fileName") String fileName, @AuthenticationPrincipal AtlassianHostUser hostUser) {
        try {
            log.info("restoreFromBackUp request invoked with file name : {} ", fileName);
            AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
            String jobProgressId = new UniqueIdGenerator().getStringId();
            backUpService.restoreData(acHostModel, fileName, null, null, jobProgressId, getUser());
            Map<String, String> response = new HashMap<>();
            response.put("jobProgressId", jobProgressId);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Erro in restoreFromBackUp ", ex);
            throw new CaptureRuntimeException(ex);
        }
    }

    @GetMapping(value = "/runDailyBackupJob", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> runDailyBackupJob() {
        log.info("****************** Start daily backup job ****************");
        backUpService.runDailyBackupJob();
        Map<String, String> response = new HashMap<>();
        response.put("success", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getBackupsList(@AuthenticationPrincipal AtlassianHostUser hostUser) {
        log.info("getBackupsList request invoked ");
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        List<S3ObjectSummary> s3ObjectSummaryList = backUpService.getBackUpsList(acHostModel);
        return ResponseEntity.ok(s3ObjectSummaryList);
    }

    @GetMapping(value = "/download")
    public void downloadBackupFile(@RequestParam("fileName") String fileName, @AuthenticationPrincipal AtlassianHostUser hostUser, HttpServletResponse response) throws IOException {
        log.info("downloadBackupFile request invoked with file name : {} ", fileName);
        AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
        try {
            File backupFile = backUpService.getFullBackupFile(acHostModel, fileName);
            if (backupFile != null) {
                if (!backupFile.exists()) {
                    log.warn("Download file requested not exist {} ", fileName);
                    response.setStatus(404);
                    return;
                }
                String mimeType = URLConnection.guessContentTypeFromName(fileName);
                if (mimeType == null) {
                    log.debug("mimetype is not detectable, will take default");
                    mimeType = "application/octet-stream";
                }
                response.setContentType(mimeType);
                response.setHeader("Content-Disposition", String.format("inline; filename=\"" + fileName + "\""));
                response.setContentLength((int) backupFile.length());
                InputStream inputStream = new BufferedInputStream(new FileInputStream(backupFile));
                FileCopyUtils.copy(inputStream, response.getOutputStream());
                if (backupFile.exists()) {
                    backupFile.delete();
                }
            }

        } catch (Exception exception) {
            log.error("Exception occurred while file download : {} ", fileName, exception);
            throw new CaptureRuntimeException(exception);
        }
        response.setStatus(404);
        return;
    }

    @PostMapping(value = "/restore", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> restoreBackupFile(@RequestParam("backup") MultipartFile mfile, @AuthenticationPrincipal AtlassianHostUser hostUser) {
        try {

            if (!mfile.isEmpty()) {
                log.info("restoreBackupFile request invoked with file name : {} ", mfile.getName());
                AcHostModel acHostModel = (AcHostModel) hostUser.getHost();
                String jobProgressId = new UniqueIdGenerator().getStringId();
                backUpService.restoreMultiPartDataFile(acHostModel, mfile.getName(), mfile, false, jobProgressId, getUser());
                Map<String, String> response = new HashMap<>();
                response.put("jobProgressId", jobProgressId);
                return ResponseEntity.ok(response);
            }

        } catch (Exception ex) {
            log.error("Error in restoreFromBackUp ", ex);

            throw new CaptureRuntimeException(ex);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    protected String getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AtlassianHostUser host = (AtlassianHostUser) auth.getPrincipal();
        String userKey = host.getUserKey().get();
        return userKey;
    }

}
