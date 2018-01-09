package com.thed.zephyr.capture.service.data.impl;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.thed.zephyr.capture.annotation.BackupEntity;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CaptureValidationException;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.view.JobProgress;
import com.thed.zephyr.capture.repositories.dynamodb.SessionActivityRepository;
import com.thed.zephyr.capture.repositories.dynamodb.SessionRepository;
import com.thed.zephyr.capture.repositories.dynamodb.TemplateRepository;
import com.thed.zephyr.capture.repositories.dynamodb.VariableRepository;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.awss3.S3Service;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.data.BackUpService;
import com.thed.zephyr.capture.util.*;
import com.thed.zephyr.capture.util.backup.BackUpUtils;
import com.thed.zephyr.capture.util.backup.BackupBuffer.Backup;
import com.thed.zephyr.capture.util.backup.BackupSystemInfoBuffer;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.ServiceUnavailableException;
import javax.xml.soap.Node;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.zip.CRC32;

@Service
public class BackUpServiceImpl implements BackUpService {
    @Autowired
    private Logger log;
    @Autowired
    private JobProgressService jobProgressService;
    @Autowired
    private LockService lockService;
    @Autowired
    private CaptureI18NMessageSource captureI18NMessageSource;
    @Autowired
    private AmazonS3Client amazonS3Client;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private SessionActivityRepository sessionActivityRepository;
    @Autowired
    private VariableRepository variableRepository;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private DynamicProperty dynamicProperty;

    private Set<Class<?>> backUpClassesList;

    @Override
    public void createBackUp(AcHostModel acHostModel, String fileName, String jobProgressId,String userKey) throws HazelcastInstanceNotDefinedException {
        log.info("createBackUp --> Started : for ctid : {} with job id process : {} ", acHostModel.getCtId(), jobProgressId);
        JobProgress jobProgress = jobProgressService.createJobProgress(acHostModel, ApplicationConstants.BACKUP_JOB, ApplicationConstants.JOB_STATUS_INPROGRESS, jobProgressId);
        final String jobProgressTicket = jobProgressId != null ? jobProgressId : jobProgress.getId();
        CompletableFuture.runAsync(() -> {
            {
               final String lockKey = ApplicationConstants.BACKUP_LOCK_KEY;
                try {
                    if (!lockService.tryLock(acHostModel.getClientKey(), lockKey, 5)) {
                        log.warn("Not able to get lock during backup for ClientKey : " + acHostModel.getClientKey() + " probably backup in progress.");
                        jobProgressService.setErrorMessage(acHostModel, jobProgressTicket, captureI18NMessageSource.getMessage("capture.admin.plugin.test.section.item.zephyr.configuration.manual.backup.inprogress"));
                        jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_COMPLETED, jobProgressTicket);
                        return;
                    }
                    log.debug("Started create backup ...");
                    String tempFilesPath = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER);
                    File tempFolder = new File(tempFilesPath);
                    File systemInfoFile = createTempFile(acHostModel, tempFilesPath, null);
                    File backupFile = createTempFile(acHostModel, tempFilesPath, null);
                    File compressBackupFile = createTempFile(acHostModel, tempFilesPath, null);
                    File archiveTempFile = createTempFile(acHostModel, tempFilesPath, "tar");

                    try {
                        checkNumberStoredBackups(acHostModel);
                        Map<String, Integer> resultReport = createProtocolBufferObject(acHostModel, backupFile, jobProgressTicket);
                        compressBackupFile(backupFile, compressBackupFile);
                        createSystemInfoProtoBuffFile(acHostModel, systemInfoFile, resultReport, compressBackupFile,userKey);
                        Map<String, File> archiveFilesMap = ImmutableMap.of(
                                ApplicationConstants.BACKUP_ARCHIVE_NAME, compressBackupFile,
                                ApplicationConstants.SYSTEM_INFO_BACKUP_ARCHIVE_NAME, systemInfoFile);
                        BackUpUtils.createTarArchive(archiveTempFile, archiveFilesMap);
                        // TODO - check version how to get
                        String zfjVersion = "unavailable";
                        Map<String, String> metaData = new TreeMap<String, String>();
                        metaData.put(ApplicationConstants.CAPTURE_VERSION_KEY, zfjVersion);
                            s3Service.saveFile(BackUpUtils.getBackupsS3Prefix(acHostModel) + fileName + ".tar", archiveTempFile, metaData);
                        log.debug("Done with creating backup, fileName=" + fileName);
                    } finally {
                        backupFile.delete();
                        compressBackupFile.delete();
                        archiveTempFile.delete();
                        systemInfoFile.delete();
                        tempFolder.delete();
                    }


                    jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_COMPLETED, jobProgressId);
                    String message = captureI18NMessageSource.getMessage("capture.job.progress.status.success.message");
                    jobProgressService.setMessage(acHostModel, jobProgressId, message);
                    lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                } catch (Exception ex) {
                    log.error("Error in createBackUp() - ", ex);
                    try {
                        jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
                        String errorMessage = captureI18NMessageSource.getMessage("capture.common.internal.server.error");
                        jobProgressService.setErrorMessage(acHostModel, jobProgressId, errorMessage);
                        lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                    } catch (HazelcastInstanceNotDefinedException e) {
                        log.warn("Error in releassing the lock in catch block - ", ex);
                    }
                }
            }
        });

    }

    @Override
    public void deleteBackup(AcHostModel acHostModel, String s3FileKey) throws CaptureRuntimeException {
        String backupFileNameWithPath = s3FileKey.substring(s3FileKey.indexOf(ApplicationConstants.S3_DELIMITER) + 1);
        s3Service.deleteFile(backupFileNameWithPath);
    }

    private File createTempFile(AcHostModel acHostModel, String path, String extension) {
        String fileName = path + "/" + createRandomFileName(acHostModel);
        if (StringUtils.isNotBlank(extension)) {
            fileName += "." + extension;
        }
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException exception) {
            log.warn("Error during store temporary file in file system, file in memory will be used instead.", exception);
        }
        return file;
    }

    private String createRandomFileName(AcHostModel acHostModel) {
        try {
            // we need this sleep to avoid generate same names
            Thread.sleep(5);
        } catch (InterruptedException e) {
            log.debug("Error during Thread.sleep");
        }
        return new UniqueIdGenerator().getStringId();
    }

    private void checkNumberStoredBackups(AcHostModel acHostModel) throws CaptureRuntimeException {
        List<S3ObjectSummary> backupsList = s3Service.getListOfFiles(BackUpUtils.getBackupsS3Prefix(acHostModel), true, true);
        if (ApplicationConstants.NUMBER_STORED_BACKUPS == (backupsList.size())) {
            deleteBackup(acHostModel, backupsList.get(0).getKey());
        }
    }

    private Map<String, Integer> createProtocolBufferObject(AcHostModel acHostModel, File backupFile, String jobProgressTicket) throws CaptureRuntimeException, HazelcastInstanceNotDefinedException, ServiceUnavailableException {
        log.debug("Started create ProtocolBufferObject ...");
        Map<String, Integer> resultReport = new HashMap<>();
        //Get data from annotated classes
        final Set<Class<?>> classesList = getBackUpClassesList();
        jobProgressService.setTotalSteps(acHostModel, jobProgressTicket, classesList.size());
        for (Class clazz : classesList) {
            log.debug("Check class: " + clazz.getSimpleName());
            jobProgressService.setStepLabel(acHostModel, jobProgressTicket, clazz.getSimpleName());
            String entityName = getEntityName(clazz);
            @SuppressWarnings("unchecked")
            BackupEntity annotation = (BackupEntity) clazz.getAnnotation(BackupEntity.class);
            int storedObjects = addObjectsBufferFromDynamo(acHostModel, entityName, jobProgressTicket, backupFile, annotation);
            resultReport.put(entityName, storedObjects);
            jobProgressService.addCompletedSteps(acHostModel, jobProgressTicket, 1);
        }
        return resultReport;
    }

    private Set<Class<?>> getBackUpClassesList() {
        Set<Class<?>> backUpClassesList = new HashSet<>();
        Class aClass1 = Session.class;
        Class aClass2 = SessionActivity.class;
        Class aClass3 = Variable.class;
        Class aClass4 = Template.class;
        backUpClassesList.add(aClass1);
        backUpClassesList.add(aClass2);
        backUpClassesList.add(aClass3);
        backUpClassesList.add(aClass4);
        return backUpClassesList;
    }

    private String getEntityName(Class clazz) throws ServiceUnavailableException {
        String entityConstant = "";
        try {
            String property = clazz.getSimpleName().toUpperCase() + "_ENTITY";
            entityConstant = (String) ApplicationConstants.class.getDeclaredField(property).get(null);
        } catch (NoSuchFieldException exception) {
            log.error("You have marked class " + clazz + " as backup class, but forget include constant to ApplicationConstant.java", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        } catch (IllegalAccessException exception) {
            log.error("Error during 'getBucketName(Long, Class)' method, class=" + clazz, exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
        return entityConstant;
    }

    private int addObjectsBufferFromDynamo(AcHostModel acHostModel, String entityName, String jobProgressTicket, File backupFile, BackupEntity annotation) throws CaptureRuntimeException, HazelcastInstanceNotDefinedException, ServiceUnavailableException {
        jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entityName), 0));
        Integer totalObjects = 0;
        if (StringUtils.equals(entityName, ApplicationConstants.SESSION_ENTITY)) {
            Integer offset = 0;
            long total;
            do {
                Page<Session> sessionsPage = sessionRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(offset, ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT));
                totalObjects += storeSessionList(sessionsPage, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.bucketType());
                total = sessionsPage.getTotalElements();
                offset += ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT;
            } while (offset < total);

        } else if (StringUtils.equals(entityName, ApplicationConstants.SESSIONACTIVITY_ENTITY)) {
            Integer offset = 0;
            long total;
            Map<String, AttributeValue> lastKeyEvaluated = null;

            do {
                Map<String, Object> result = sessionActivityRepository.findByCtId(acHostModel.getCtId(), lastKeyEvaluated);
                lastKeyEvaluated = (Map<String, AttributeValue>) result.get("lastKeyEvaluated");
                List<SessionActivity> sessionActivities = (List<SessionActivity>) result.get("items");
                totalObjects += storeSessionActivityList(sessionActivities, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.bucketType());
                offset += ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT;
            } while (lastKeyEvaluated != null);
        } else if (StringUtils.equals(entityName, ApplicationConstants.VARIABLE_ENTITY)) {
            Integer offset = 0;
            long total;
            do {
                Page<Variable> variablePage = variableRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(offset, ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT));
                totalObjects += storeVariableList(variablePage, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.bucketType());
                total = variablePage.getTotalElements();
                offset += ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT;
            } while (offset < total);
        } else if (StringUtils.equals(entityName, ApplicationConstants.TEMPLATE_ENTITY)) {
            Integer offset = 0;
            long total;
            do {
                Page<Template> templatesPage = templateRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(offset, ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT));
                totalObjects += storeTemplateList(templatesPage, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.bucketType());
                total = templatesPage.getTotalElements();
                offset += ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT;
            } while (offset < total);
        }

        jobProgressService.addCurrentStepMessageToMessages(acHostModel, jobProgressTicket);
        return totalObjects;

    }

    private String getMessageForEntity(String entity) {
        if (StringUtils.equals(entity, ApplicationConstants.SESSION_ENTITY)) {
            return captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.backup.process.session");
        } else if (StringUtils.equals(entity, ApplicationConstants.SESSIONACTIVITY_ENTITY)) {
            return captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.backup.process.sessionactivity");
        } else if (StringUtils.equals(entity, ApplicationConstants.VARIABLE_ENTITY)) {
            return captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.backup.process.variable");
        } else if (StringUtils.equals(entity, ApplicationConstants.TEMPLATE_ENTITY)) {
            return captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.backup.process.template");
        }
        return null;
    }

    private int storeSessionList(Page<Session> sessionsPage, AcHostModel acHostModel, String entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String bucketType) throws CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (sessionsPage != null) {
            List<Session> sessions = sessionsPage.getContent();
            if (sessions != null && sessions.size() > 0) {
                sessions.forEach(session -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setBucket(bucketName);
                    objectBuffer.setBucketType(bucketType);
                    objectBuffer.setKey(session.getId());
                    String riakObjectJsonStr = sessionObjectToJsonString(session);
                    if (StringUtils.isNotBlank(riakObjectJsonStr)) {
                        objectBuffer.setJsonString(riakObjectJsonStr);
                        backUp.addObjects(objectBuffer);
                    } else {
                        log.warn("Session object has empty body, probably data was lost");
                    }
                });
            }

        }
        if (backUp.getObjectsList().size() > 0) {
            createTemporaryFileWithByteData(backUp.build(), backupFile);
            jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entityName), totalObjects + backUp.getObjectsList().size()));
        }
        return backUp.getObjectsList().size();
    }

    private int storeSessionActivityList(Page<SessionActivity> sessionActivitiesPage, AcHostModel acHostModel, String entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String bucketType) throws CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (sessionActivitiesPage != null) {
            List<SessionActivity> sessionActivities = sessionActivitiesPage.getContent();
            if (sessionActivities != null && sessionActivities.size() > 0) {
                sessionActivities.forEach(sessionActivity -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setBucket(bucketName);
                    objectBuffer.setBucketType(bucketType);
                    objectBuffer.setKey(sessionActivity.getId());
                    String objectJsonStr = sessionActivityObjectToJsonString(sessionActivity);
                    if (StringUtils.isNotBlank(objectJsonStr)) {
                        objectBuffer.setJsonString(objectJsonStr);
                        backUp.addObjects(objectBuffer);
                    } else {
                        log.warn("SessionActivity object has empty body, probably data was lost");
                    }
                });
            }

        }
        if (backUp.getObjectsList().size() > 0) {
            createTemporaryFileWithByteData(backUp.build(), backupFile);
            jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entityName), totalObjects + backUp.getObjectsList().size()));
        }
        return backUp.getObjectsList().size();
    }

    private int storeSessionActivityList(List<SessionActivity> sessionActivities, AcHostModel acHostModel, String entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String bucketType) throws CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (sessionActivities != null && sessionActivities.size() > 0) {
            sessionActivities.forEach(sessionActivity -> {
                Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                objectBuffer.setBucket(bucketName);
                objectBuffer.setBucketType(bucketType);
                objectBuffer.setKey(sessionActivity.getId());
                String objectJsonStr = sessionActivityObjectToJsonString(sessionActivity);
                if (StringUtils.isNotBlank(objectJsonStr)) {
                    objectBuffer.setJsonString(objectJsonStr);
                    backUp.addObjects(objectBuffer);
                } else {
                    log.warn("SessionActivity object has empty body, probably data was lost");
                }
            });
        }
        if (backUp.getObjectsList().size() > 0) {
            createTemporaryFileWithByteData(backUp.build(), backupFile);
            jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entityName), totalObjects + backUp.getObjectsList().size()));
        }
        return backUp.getObjectsList().size();
    }

    private int storeVariableList(Page<Variable> variablesPage, AcHostModel acHostModel, String entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String bucketType) throws CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (variablesPage != null) {
            List<Variable> variables = variablesPage.getContent();
            if (variables != null && variables.size() > 0) {
                variables.forEach(variable -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setBucket(bucketName);
                    objectBuffer.setBucketType(bucketType);
                    objectBuffer.setKey(variable.getId());
                    String objectJsonStr = variableObjectToJsonString(variable);
                    if (StringUtils.isNotBlank(objectJsonStr)) {
                        objectBuffer.setJsonString(objectJsonStr);
                        backUp.addObjects(objectBuffer);
                    } else {
                        log.warn("Variable object has empty body, probably data was lost");
                    }
                });
            }

        }
        if (backUp.getObjectsList().size() > 0) {
            createTemporaryFileWithByteData(backUp.build(), backupFile);
            jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entityName), totalObjects + backUp.getObjectsList().size()));
        }
        return backUp.getObjectsList().size();
    }

    private int storeTemplateList(Page<Template> templatesPage, AcHostModel acHostModel, String entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String bucketType) throws CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (templatesPage != null) {
            List<Template> templates = templatesPage.getContent();
            if (templates != null && templates.size() > 0) {
                templates.forEach(template -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setBucket(bucketName);
                    objectBuffer.setBucketType(bucketType);
                    objectBuffer.setKey(template.getId());
                    String objectJsonStr = templateObjectToJsonString(template);
                    if (StringUtils.isNotBlank(objectJsonStr)) {
                        objectBuffer.setJsonString(objectJsonStr);
                        backUp.addObjects(objectBuffer);
                    } else {
                        log.warn("Template object has empty body, probably data was lost");
                    }
                });
            }

        }
        if (backUp.getObjectsList().size() > 0) {
            createTemporaryFileWithByteData(backUp.build(), backupFile);
            jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entityName), totalObjects + backUp.getObjectsList().size()));
        }
        return backUp.getObjectsList().size();
    }


    private String sessionObjectToJsonString(Session session) {
        ObjectMapper mapper = new ObjectMapper();
        String riakObjJsonStr = null;
        try {
            riakObjJsonStr = mapper.writeValueAsString(session);
        } catch (JsonProcessingException exception) {
            log.error("Json error.", exception);
        }
        return riakObjJsonStr;
    }

    private String sessionActivityObjectToJsonString(SessionActivity sessionActivity) {
        ObjectMapper mapper = new ObjectMapper();
        String riakObjJsonStr = null;
        try {
            riakObjJsonStr = mapper.writeValueAsString(sessionActivity);
        } catch (JsonProcessingException exception) {
            log.error("Json error.", exception);
        }
        return riakObjJsonStr;
    }

    private String variableObjectToJsonString(Variable variable) {
        ObjectMapper mapper = new ObjectMapper();
        String riakObjJsonStr = null;
        try {
            riakObjJsonStr = mapper.writeValueAsString(variable);
        } catch (JsonProcessingException exception) {
            log.error("Json error.", exception);
        }
        return riakObjJsonStr;
    }

    private String templateObjectToJsonString(Template template) {
        ObjectMapper mapper = new ObjectMapper();
        String riakObjJsonStr = null;
        try {
            riakObjJsonStr = mapper.writeValueAsString(template);
        } catch (JsonProcessingException exception) {
            log.error("Json error.", exception);
        }
        return riakObjJsonStr;
    }


    private void createTemporaryFileWithByteData(Backup backUp, File backupFile) throws ServiceUnavailableException {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(backupFile, true);
        } catch (FileNotFoundException exception) {
            log.error("Can't find file during create FileOutputStream, fileName=" + backupFile.getName(), exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
        try {
            backUp.writeDelimitedTo(fileOutputStream);
            fileOutputStream.close();
        } catch (IOException exception) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                log.warn("Error during close outputStream in method 'createTemporaryFileWithByteData'.", e);
            }
            log.error("Error during write backUpBuffer into OutputStream.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
    }

    private void compressBackupFile(File uncompressFile, File compressFile) throws ServiceUnavailableException {
        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream;
        org.xerial.snappy.SnappyOutputStream snappyOutputStream;
        try {
            compressFile.createNewFile();
            fileInputStream = new FileInputStream(uncompressFile);
            fileOutputStream = new FileOutputStream(compressFile);
            snappyOutputStream = new org.xerial.snappy.SnappyOutputStream(fileOutputStream);

            byte[] buf = new byte[1024 * 1024];
            int len;
            while ((len = fileInputStream.read(buf)) > 0) {
                snappyOutputStream.write(buf, 0, len);
            }
            snappyOutputStream.close();
            fileInputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException exception) {
            log.error("Can't find file during create FileOutputStream, fileName=" + uncompressFile.getName(), exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        } catch (IOException exception) {
            log.error("Error during compress file", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
    }

    private void createSystemInfoProtoBuffFile(AcHostModel acHostModel, File destFile, Map<String, Integer> resultReport, File backupFile,String userKey) throws ServiceUnavailableException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(resultReport);
        } catch (Exception exception) {
            log.error("Error during parse map to json string.", exception);
        }
        String creator  = userKey !=null ? userKey : "system";
        BackupSystemInfoBuffer.BackupSystemInfo.Builder systemInfoBuilder = BackupSystemInfoBuffer.BackupSystemInfo.newBuilder();
        systemInfoBuilder.setAttachments(false);
        systemInfoBuilder.setComment(jsonString);
        systemInfoBuilder.setCreateDate(new Long(new Date().getTime()).toString());
        systemInfoBuilder.setCreator(creator);
         String supportedBEversion = dynamicProperty.getStringProp("current.supported.be.version", "unavailable").get();
        systemInfoBuilder.setDataVersion(supportedBEversion);
        systemInfoBuilder.setHost(acHostModel.getBaseUrl());
        systemInfoBuilder.setBackupCheckSum(Long.toString(calculateCheckSum(backupFile)));
        //TODO - Need to check what is JIRA ID
        systemInfoBuilder.setJiraId(acHostModel.getClientKey());
        try {
            systemInfoBuilder.setCtId(acHostModel.getCtId());
        } catch (Exception exception) {
            log.warn("Can't get ztId during create Backup SystemInfo.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
        systemInfoBuilder.setNumberOfObjects(extractTotalStoredObjectsFromJobProgress(resultReport));
        BackupSystemInfoBuffer.BackupSystemInfo systemInfo = systemInfoBuilder.build();

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(destFile);
        } catch (FileNotFoundException exception) {
            log.error("Can't find file during create FileOutputStream, fileName=" + destFile.getName(), exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
        try {
            systemInfo.writeTo(fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException exception) {
            log.error("Can't find file during create FileOutputStream, fileName=" + destFile.getName(), exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        } catch (IOException exception) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                log.warn("Error during close outputStream in method 'createTemporaryFileWithByteData'.", e);
            }
            log.error("Error during write backUpBuffer into OutputStream.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
    }

    private String extractTotalStoredObjectsFromJobProgress(Map<String, Integer> resultReport) {
        Integer count = 0;
        for (Integer objects : resultReport.values()) {
            count += objects;
        }
        return count.toString();
    }

    private long calculateCheckSum(File file) throws ServiceUnavailableException {
        InputStream fi;
        try {
            fi = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException exception) {
            log.error("Error during calculate backup checksum.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        }
        CRC32 gCRC = new CRC32();
        try {
            int gByte = 0;
            byte[] buf = new byte[1024 * 64];
            while ((gByte = fi.read(buf)) > 0) {
                gCRC.update(buf, 0, gByte);
            }
        } catch (Exception exception) {
            log.error("Error during calculate backup checksum.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("zephyr.admin.plugin.test.section.item.zephyr.configuration.restore.internalservererror"));
        } finally {
            try {
                fi.close();
            } catch (Exception exception) {
                log.warn("Can't close stream after calculate backup checksum", exception);
            }
        }
        return gCRC.getValue();
    }

}
