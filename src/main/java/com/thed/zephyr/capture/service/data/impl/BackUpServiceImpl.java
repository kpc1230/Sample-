package com.thed.zephyr.capture.service.data.impl;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.atlassian.connect.spring.AtlassianHostUser;
import com.atlassian.connect.spring.internal.auth.jwt.JwtAuthentication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.thed.zephyr.capture.annotation.BackupEntity;
import com.thed.zephyr.capture.exception.*;
import com.thed.zephyr.capture.model.*;
import com.thed.zephyr.capture.model.view.JobProgress;
import com.thed.zephyr.capture.repositories.dynamodb.*;
import com.thed.zephyr.capture.service.JobProgressService;
import com.thed.zephyr.capture.service.ac.DynamoDBAcHostRepository;
import com.thed.zephyr.capture.service.awss3.S3Service;
import com.thed.zephyr.capture.service.cache.ITenantAwareCache;
import com.thed.zephyr.capture.service.cache.LockService;
import com.thed.zephyr.capture.service.data.BackUpService;
import com.thed.zephyr.capture.service.data.LicenseService;
import com.thed.zephyr.capture.service.data.SessionService;
import com.thed.zephyr.capture.service.db.elasticsearch.ESUtilService;
import com.thed.zephyr.capture.util.*;
import com.thed.zephyr.capture.util.backup.BackUpUtils;
import com.thed.zephyr.capture.util.backup.BackupBuffer.Backup;
import com.thed.zephyr.capture.util.backup.BackupSystemInfoBuffer;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.ServiceUnavailableException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.BlockingQueue;
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
    @Autowired
    private ITenantAwareCache tenantAwareCache;
    @Autowired
    private DynamoDBOperations dynamoDBOperations;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private AcHostModelRepository acHostModelRepository;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private ESUtilService esUtilService;


    @Override
    public JobProgress createBackUp(AcHostModel acHostModel, String fileName, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException {
        CompletableFuture.runAsync(() -> {
            try {
                createBackUpFunction(acHostModel, fileName, jobProgressId, userKey);
            } catch (HazelcastInstanceNotDefinedException ex) {
                log.warn("Error in releasing the lock in catch block - ", ex);
            }
        });
        return null;
    }

    @Override
    public JobProgress restoreMultiPartDataFile(AcHostModel acHostModel, String fileName, MultipartFile mfile, Boolean foreignTenantId, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException, IOException {
        String tempFilesPath = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER);
        File tempFolder = new File(tempFilesPath);
        tempFolder.mkdir();
        String tempFileName = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER) + "/" + BackUpUtils.createRandomFileName(acHostModel);
        File tempFile = new File(tempFileName);
        FileCopyUtils.copy(mfile.getBytes(), tempFile);
        return restoreData(acHostModel, mfile.getName(), tempFile, false, jobProgressId, userKey);
    }

    @Override
    public JobProgress restoreData(AcHostModel acHostModel, String fileName, File file, Boolean foreignTenantId, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException {
        final String lockKey = ApplicationConstants.RESTORE_LOCK_KEY;
        JobProgress jobProgress = jobProgressService.createJobProgress(acHostModel, ApplicationConstants.RESTORE_JOB, ApplicationConstants.JOB_STATUS_INPROGRESS, jobProgressId);
        final String jobProgressTicket = jobProgressId != null ? jobProgressId : jobProgress.getId();
        String cct = CaptureUtil.getCurrentCtId();
        CompletableFuture.runAsync(() -> {
            {
                long startTime = System.currentTimeMillis();
                try {
                    if (!lockService.tryLock(acHostModel.getClientKey(), lockKey, 5)) {
                        jobProgressService.setErrorMessage(acHostModel, jobProgressTicket, captureI18NMessageSource.getMessage("capture.job.backup.inprogress.error"));
                        jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_COMPLETED, jobProgressTicket);
                        return;
                    }
                    log.debug("Started restore backup from filename=" + fileName + " ...");
                    String tempBackupFileName = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER) + "/" + BackUpUtils.createRandomFileName(acHostModel);
                    File backupFile;
                    if (file != null) {
                        backupFile = file;
                    } else {
                        backupFile = new File(tempBackupFileName);
                        backupFile.createNewFile();
                        jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString("capture.job.backup.process.download.backup", null));
                        s3Service.getFileContent(BackUpUtils.getBackupsS3Prefix(acHostModel) + fileName, backupFile);
                    }
                    tenantAwareCache.clearTenantCache(acHostModel);
                    jobProgressService.setTotalSteps(acHostModel, jobProgressTicket, Integer.valueOf((int) backupFile.length()));
                    jobProgressService.setStepLabel(acHostModel, jobProgressTicket, "0");
                    String tempFilesPath = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER);
                    File tempFolder = new File(tempFilesPath);
                    Map<String, File> extractedFilesMap = new HashMap<String, File>();
                    File backupDataFile = null;
                    File systemInfoBackupFile = null;
                    BackupSystemInfoBuffer.BackupSystemInfo backupSystemInfo;

                    try {
                        extractedFilesMap = BackUpUtils.extractTarArchive(acHostModel, backupFile, tempFilesPath);
                        backupDataFile = extractedFilesMap.get(ApplicationConstants.BACKUP_ARCHIVE_NAME);
                        systemInfoBackupFile = extractedFilesMap.get(ApplicationConstants.SYSTEM_INFO_BACKUP_ARCHIVE_NAME);
                        if (systemInfoBackupFile == null) {
                            log.warn("The backup tar file was corrupted..");
                            jobProgressService.setErrorMessage(acHostModel, jobProgressTicket, captureI18NMessageSource.getMessage("capture.job.restore.error.extract.tar.archive"));
                            lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                            jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_FAILED, jobProgressTicket);
                            return;
                        }
                        backupSystemInfo = extractSystemFileInfo(systemInfoBackupFile);
                        if (!checkBackupCheckSum(backupSystemInfo, backupDataFile)) {
                            log.warn("The backup file from tar archive don't fit to SystemInfoFile, checksum is different");
                            jobProgressService.setErrorMessage(acHostModel, jobProgressTicket, captureI18NMessageSource.getMessage("capture.job.restore.error.perform.tar.archive"));
                            lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                            jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_FAILED, jobProgressTicket);
                            return;
                        }
                        restoreDataFromFile(backupDataFile, foreignTenantId, acHostModel, new ArrayList<String>(), jobProgressTicket, backupSystemInfo, cct);
                        createRestoreReport(acHostModel, backupSystemInfo, jobProgressTicket);

                        jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(captureI18NMessageSource.getMessage("capture.job.backup.process.index.new.data"), null));
                        String reIndexJobProgressTicket = new UniqueIdGenerator().getStringId();
                        esUtilService.reindexTenantESData(acHostModel, reIndexJobProgressTicket, userKey, null, false);
                        lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                        jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_COMPLETED, jobProgressTicket);
                        log.info("Restore job has been completed for the tenant {} , took {} milli seconds ", acHostModel.getCtId(), (System.currentTimeMillis() - startTime));
                        return;
                    } catch (ExtractTarArchiveException exception) {
                        log.warn("Error during extract tar archive fileName:" + fileName + ". Possibly it is old style backup file. We don't support old backups any more.", exception);
                        jobProgressService.setErrorMessage(acHostModel, jobProgressTicket, "capture.job.restore.uncompatiblebackup");
                        lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                        jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_FAILED, jobProgressTicket);
                        return;
                    } catch (IncompatibleBackupException exception) {
                        jobProgressService.setErrorMessage(acHostModel, jobProgressTicket, exception.getMessage());
                        lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                        jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_FAILED, jobProgressTicket);
                        return;
                    } finally {
                        backupFile.delete();
                        if (backupDataFile != null) {
                            backupDataFile.delete();
                        }
                        if (systemInfoBackupFile != null) {
                            systemInfoBackupFile.delete();
                        }
                        tempFolder.delete();
                    }

                } catch (Exception ex) {
                    log.error("Error in createBackUp() - ", ex);
                    try {
                        jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
                        String errorMessage = captureI18NMessageSource.getMessage("capture.common.internal.server.error");
                        jobProgressService.setErrorMessage(acHostModel, jobProgressId, errorMessage);
                        lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                    } catch (HazelcastInstanceNotDefinedException e) {
                        log.warn("Error in releasing the lock in catch block - ", ex);
                    }
                }
            }
        });
        return jobProgress;
    }

    @Override
    public void deleteBackup(AcHostModel acHostModel, String s3FileKey) throws CaptureRuntimeException {
        String backupFileNameWithPath = s3FileKey.substring(s3FileKey.indexOf(ApplicationConstants.S3_DELIMITER) + 1);
        s3Service.deleteFile(backupFileNameWithPath);
    }

    @Override
    public void runDailyBackupJob() {
        BlockingQueue<AcHostModel> queue = hazelcastInstance.getQueue("backup-jobs");

        log.info("Backup job publisher, should run - One per entire cluster");
        try {
            acHostModelRepository.findAll()
                    .forEach(acHostModel -> {
                        log.info("Creating backup job for Tenant: " + acHostModel.getCtId());
                        queue.offer(acHostModel);
                    });
        } catch (Exception exp) {
            log.error("BackupJobPublisher", exp);
        }
        CompletableFuture.runAsync(() -> {
            int hazelCastNotActive = 0;
            while (true && hazelCastNotActive < 3) {
                AcHostModel acHostModel = null;
                try {
                    acHostModel = queue.take();
                    JwtAuthentication jwtAuthentication = new JwtAuthentication(new AtlassianHostUser(acHostModel, Optional.ofNullable(null)), new JWTClaimsSet());
                    SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                    Optional<AddonInfo> licenseInfoOption = null;
                    licenseInfoOption = licenseService.getAddonInfo(acHostModel);
                    if (licenseInfoOption.isPresent()) {
                        AddonInfo.License licenseInfo = licenseInfoOption.get().getLicense();
                        if (licenseInfo != null && licenseInfo.isActive()) {
                            log.info("********************** Daily BackupService: starting backup of " + acHostModel.getCtId());
                            String fileName = acHostModel.getCtId() + "-" + ISODateTimeFormat.dateTime().print(DateTime.now().getMillis());
                            fileName = StringUtils.replace(fileName, "+", "-");
                            log.info("FileName " + fileName);
                            String jobProgressId = new UniqueIdGenerator().getStringId();
                            log.info("Calling service ... with jobProgressId : {},userKey: {} ", jobProgressId, "system");
                            JobProgress jobProgress = createBackUpFunction(acHostModel, fileName, jobProgressId, "system");
                            log.info("********************** Daily BackupService: completed backup of " + acHostModel.getCtId());
                        } else {
                            log.warn("BackupService: Skipping backup as Capture licence is not active, license expired, tenantId: " + acHostModel.getCtId());
                        }
                    } else {
                        log.warn("BackupService: Skipping backup as license Info Option is null for the tenantId: " + acHostModel.getCtId());
                    }

                } catch (HazelcastInstanceNotActiveException hcexp) {
                    hazelCastNotActive += 1;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (HazelcastInstanceNotDefinedException exp) {
                    log.error("HazelcastInstanceNotDefinedException exception happend for this tenant {} while backup  ", acHostModel.getCtId(), exp);
                } catch (UnauthorizedException exp) {
                    log.error("UnauthorizedException exception happend for this tenant {} while backup  ", acHostModel.getCtId(), exp);
                }

            }
        });
    }

    @Override
    public List<S3ObjectSummary> getBackUpsList(AcHostModel acHostModel) throws CaptureRuntimeException {
        return (acHostModel != null) ? s3Service.getListOfFiles(BackUpUtils.getBackupsS3Prefix(acHostModel), true, true) : new ArrayList<>();
    }

    @Override
    public File getFullBackupFile(AcHostModel acHostModel, String s3FileName) throws CaptureRuntimeException {
        String tempFilesPath = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER);
        File tempFolder = new File(tempFilesPath);
        tempFolder.mkdir();
        String tempFileName = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER) + "/" + BackUpUtils.createRandomFileName(acHostModel);
        File tempFile = new File(tempFileName);
        try {
            s3Service.getFileContent(BackUpUtils.getBackupsS3Prefix(acHostModel) + s3FileName, tempFile);
            return tempFile;
        } catch (Exception exception) {
            tempFile.delete();
            tempFolder.delete();
        }
        return null;
    }


    private JobProgress createBackUpFunction(AcHostModel acHostModel, String fileName, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException {
        JobProgress jobProgress = jobProgressService.createJobProgress(acHostModel, ApplicationConstants.BACKUP_JOB, ApplicationConstants.JOB_STATUS_INPROGRESS, jobProgressId);
        final String jobProgressTicket = jobProgressId != null ? jobProgressId : jobProgress.getId();
        long startTime = System.currentTimeMillis();
        log.info("createBackUp --> Started : for ctid : {} with job id process : {} ", acHostModel.getCtId(), jobProgressId);
        final String lockKey = ApplicationConstants.BACKUP_LOCK_KEY;
        try {
            if (!lockService.tryLock(acHostModel.getClientKey(), lockKey, 5)) {
                log.warn("Not able to get lock during backup for ClientKey : " + acHostModel.getClientKey() + " probably backup in progress.");
                jobProgressService.setErrorMessage(acHostModel, jobProgressTicket, captureI18NMessageSource.getMessage("capture.job.manual.backup.inprogress"));
                jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.JOB_STATUS_COMPLETED, jobProgressTicket);
                return jobProgress;
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
                createSystemInfoProtoBuffFile(acHostModel, systemInfoFile, resultReport, compressBackupFile, userKey);
                Map<String, File> archiveFilesMap = ImmutableMap.of(
                        ApplicationConstants.BACKUP_ARCHIVE_NAME, compressBackupFile,
                        ApplicationConstants.SYSTEM_INFO_BACKUP_ARCHIVE_NAME, systemInfoFile);
                BackUpUtils.createTarArchive(archiveTempFile, archiveFilesMap);
                String supportedBEversion = dynamicProperty.getStringProp("current.supported.be.version", "unavailable").get();
                Map<String, String> metaData = new TreeMap<String, String>();
                metaData.put(ApplicationConstants.CAPTURE_VERSION_KEY, supportedBEversion);
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
            String message = captureI18NMessageSource.getMessage("capture.job.progress.status.success.message.backup");
            jobProgressService.setMessage(acHostModel, jobProgressId, message);
            lockService.deleteLock(acHostModel.getClientKey(), lockKey);
            log.info("createBackUp --> Completed : for ctid : {} with job id process : {} ,took {} milli seconds ", acHostModel.getCtId(), jobProgressId, (System.currentTimeMillis() - startTime));
        } catch (Exception ex) {
            log.error("Error in createBackUp() - ", ex);
            try {
                jobProgressService.completedWithStatus(acHostModel, ApplicationConstants.INDEX_JOB_STATUS_FAILED, jobProgressId);
                String errorMessage = captureI18NMessageSource.getMessage("capture.common.internal.server.error");
                jobProgressService.setErrorMessage(acHostModel, jobProgressId, errorMessage);
                lockService.deleteLock(acHostModel.getClientKey(), lockKey);
                log.info("createBackUp --> Completed with : for ctid : {} with job id process : {} ,took {} milli seconds ", acHostModel.getCtId(), jobProgressId, (System.currentTimeMillis() - startTime));
            } catch (HazelcastInstanceNotDefinedException e) {
                log.warn("Error in releassing the lock in catch block - ", ex);
            }
        }
        return jobProgress;
    }

    private void createRestoreReport(AcHostModel acHostModel, BackupSystemInfoBuffer.BackupSystemInfo backupSystemInfo, String jobProgressTicket) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Integer> resultReport = mapper.readValue(backupSystemInfo.getComment(), Map.class);
            for (Map.Entry<String, Integer> entry : resultReport.entrySet()) {
                jobProgressService.addStepMessages(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entry.getKey()), entry.getValue()));
            }
        } catch (Exception exception) {
            log.error("Error during parse backup result report from SystemInfo file.", exception);
            return;
        }
    }

    private Boolean checkBackupCheckSum(BackupSystemInfoBuffer.BackupSystemInfo backupSystemInfo, File backupDataFile) throws ServiceUnavailableException {
        long checkSum = calculateCheckSum(backupDataFile);
        String checkSumFromInfo = backupSystemInfo.getBackupCheckSum();
        return StringUtils.equals(Long.toString(checkSum), checkSumFromInfo);
    }

    private void restoreDataFromFile(File backUpFile, Boolean foreignTenantId, AcHostModel acHostModel, List<String> cleanException, String jobProgressTicket, BackupSystemInfoBuffer.BackupSystemInfo backupSystemInfo, String ctId) throws CaptureRuntimeException, ServiceUnavailableException {
        String tempFilePath = BackUpUtils.getTemporaryFolderName(acHostModel, ApplicationConstants.S3_BACKUP_FOLDER);
        File uncompressTempBackupFile = new File(tempFilePath + "/uncompressFile");//createTempFile(acw, tempFile, null);
        getUncompressBackupFileName(backUpFile, uncompressTempBackupFile);
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(uncompressTempBackupFile);
        } catch (FileNotFoundException exception) {
            log.error("File not found exception", exception);
            throw new ServiceUnavailableException("capture.job.restore.internalservererror");
        }
        try {
            Backup.Builder backupBuilder = Backup.newBuilder();
            Boolean checkBackup = false;
            while (backupBuilder.mergeDelimitedFrom(inputStream)) {
                if (!checkBackup) {
                    checkBackupAndCleanCurrentData(backupSystemInfo, foreignTenantId, acHostModel, cleanException, ctId, jobProgressTicket);
                    checkBackup = true;
                }
                restoreDataFromBackUpBuffer(backupBuilder.build(), foreignTenantId, acHostModel, jobProgressTicket);
                backupBuilder = Backup.newBuilder();
            }
        } catch (IOException exception) {
            log.warn("Error during parse backup file.", exception);
            throw new ServiceUnavailableException("capture.job.restore.backup.incompatible");
        } catch (IncompatibleBackupException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Error during parse backup file.", exception);
            throw new ServiceUnavailableException("capture.job.restore.internalservererror");
        } finally {
            try {
                inputStream.close();
            } catch (IOException streamCloseException) {
                log.warn("Error close inputStream exception", streamCloseException);
            } finally {
                uncompressTempBackupFile.delete();
            }
        }
    }

    private void restoreDataFromBackUpBuffer(Backup backUpBuffer, Boolean foreignTenantId, AcHostModel acHostModel, String jobProgressTicket) throws CaptureRuntimeException, HazelcastInstanceNotDefinedException, ServiceUnavailableException {
        log.debug("Started restore data from protocolBuffer object ...");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        int index = 0;
        for (Backup.Object objectBuffer : backUpBuffer.getObjectsList()) {
            try {
                String tableName = getNameTableName(objectBuffer);
                if (tableName != null) {
                    if (StringUtils.equals(tableName, ApplicationConstants.SESSION_TABLE_NAME)) {
                        Session session = mapper.readValue(objectBuffer.getJsonString(), Session.class);
                        if (session == null) {
                            log.warn("Error during mapped string to json object. Was received null,  item:" + session
                                    + " objectBuffer.getJsonString(): " + objectBuffer.getJsonString()
                                    + " objectBuffer.getEntity(): " + objectBuffer.getEntity()
                                    + " objectBuffer.getEntityType(): " + objectBuffer.getEntityType());
                            continue;
                        } else {
                            sessionRepository.save(session);
                        }
                    } else if (StringUtils.equals(tableName, ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME)) {

                        String json = objectBuffer.getJsonString();
                        JSONObject newJObject = null;
                        try {
                            JSONParser parser = new JSONParser();
                            newJObject = (JSONObject) parser.parse(json);

                            if (newJObject != null) {
                                String clazz = newJObject.get("clazz").toString();
                                Class<?> cls = Class.forName(clazz);
                                SessionActivity sessionActivity = (SessionActivity) mapper.readValue(objectBuffer.getJsonString(), cls);
                                if (sessionActivity == null) {
                                    log.warn("Error during mapped string to json object. Was received null,  item:" + sessionActivity
                                            + " objectBuffer.getJsonString(): " + objectBuffer.getJsonString()
                                            + " objectBuffer.getEntity(): " + objectBuffer.getEntity()
                                            + " objectBuffer.getEntityType(): " + objectBuffer.getEntityType());
                                    continue;
                                } else {
                                    sessionActivityRepository.save(sessionActivity);
                                }
                            }
                        } catch (Exception exp) {
                            log.error("Exception occured while parsing object ");
                        }

                    } else if (StringUtils.equals(tableName, ApplicationConstants.VARIABLE_TABLE_NAME)) {
                        Variable variable = mapper.readValue(objectBuffer.getJsonString(), Variable.class);
                        if (variable == null) {
                            log.warn("Error during mapped string to json object. Was received null,  item:" + variable
                                    + " objectBuffer.getJsonString(): " + objectBuffer.getJsonString()
                                    + " objectBuffer.getEntity(): " + objectBuffer.getEntity()
                                    + " objectBuffer.getEntityType(): " + objectBuffer.getEntityType());
                            continue;
                        } else {
                            variableRepository.save(variable);
                        }
                    } else if (StringUtils.equals(tableName, ApplicationConstants.TEMPLATE_TABLE_NAME)) {
                        Template template = mapper.readValue(objectBuffer.getJsonString(), Template.class);
                        if (template == null) {
                            log.warn("Error during mapped string to json object. Was received null,  item:" + template
                                    + " objectBuffer.getJsonString(): " + objectBuffer.getJsonString()
                                    + " objectBuffer.getEntity(): " + objectBuffer.getEntity()
                                    + " objectBuffer.getEntityType(): " + objectBuffer.getEntityType());
                            continue;
                        } else {
                            templateRepository.save(template);
                        }
                    }

                } else {

                }
                index++;
                Integer totalObjects = Integer.parseInt(jobProgressService.getStepLabel(acHostModel, jobProgressTicket)) + 1;
                jobProgressService.setStepLabel(acHostModel, jobProgressTicket, totalObjects.toString());
                jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(captureI18NMessageSource.getMessage("capture.job.backup.restore.object"), totalObjects));
            } catch (IOException exception) {
                log.error("Error during mapped string to json object.", exception);
                throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.s3service.serviceunavailable"));
            }
        }

        Integer completedSteps = jobProgressService.getCompletedSteps(acHostModel, jobProgressTicket) + index * 180;
        Integer totalSteps = jobProgressService.getTotalSteps(acHostModel, jobProgressTicket);
        log.debug("CompletedSteps: " + completedSteps);
        if (totalSteps > completedSteps) {
            jobProgressService.setCompletedSteps(acHostModel, jobProgressTicket, completedSteps);
        } else {
            jobProgressService.setCompletedSteps(acHostModel, jobProgressTicket, totalSteps - 180);
        }
        jobProgressService.setMessage(acHostModel, jobProgressTicket, "Total restored objects " + jobProgressService.getStepLabel(acHostModel, jobProgressTicket));

        log.debug("Done with restore data. Was restored " + backUpBuffer.getObjectsList().size() + " objects. Index=" + index);
    }

    private String getNameTableName(Backup.Object objectBuffer) throws CaptureRuntimeException {
        String tableName = null;
        String entityType = StringUtils.isNotBlank(objectBuffer.getEntityType()) ? objectBuffer.getEntityType() : null;
        tableName = entityType != null ? entityType : null;
        return tableName;
    }

    private void checkBackupAndCleanCurrentData(BackupSystemInfoBuffer.BackupSystemInfo backupSystemInfo, Boolean foreignTenantId, AcHostModel acHostModel, List<String> cleanException, String ctId, String jobProgressTicket) throws CaptureRuntimeException, HazelcastInstanceNotDefinedException, ServiceUnavailableException {
        String currentTenantId = acHostModel.getCtId();
        if (StringUtils.equals(ctId, backupSystemInfo.getCtId())) {
            cleanCurrentData(acHostModel, cleanException, jobProgressTicket);
        } else if (!dynamicProperty.getBoolProp(ApplicationConstants.ALLOW_FOREIGN_CTID_DYNAMIC_KEY, false).get()) {
            log.warn("CtId id doesn't fit, this backup belongs to other tenant current ctId:{} ctId id from backup:{}", ctId, backupSystemInfo.getCtId());
            throw new IncompatibleBackupException(captureI18NMessageSource.getMessage("capture.job.restore.uncompatiblebackup"));
        } else if (StringUtils.equals(currentTenantId, backupSystemInfo.getJiraId())) {
            //  cleanCurrentData(acHostModel, cleanException, jobProgressTicket);
            //  copyZtIdFolderInS3(ztId, backupSystemInfo.getCtId());
        } else if (foreignTenantId) {
            //  cleanCurrentData(acHostModel, cleanException, jobProgressTicket);
            //   copyZtIdFolderInS3(acHostModel, backupSystemInfo.getCtId());
        } else {
            log.warn("CtId doesn't fit as long as jira tenantId, current ctId:{} ctId from backup:{}, current jira tenantId:{} jira tenantId from backup:{}", ctId, backupSystemInfo.getCtId(), currentTenantId, backupSystemInfo.getJiraId());
            throw new IncompatibleBackupException(captureI18NMessageSource.getMessage("capture.job.restore.uncompatiblebackup"));
        }
    }

    private void cleanCurrentData(AcHostModel acHostModel, List<String> exceptionList, String jobProgressTicket) throws CaptureRuntimeException, HazelcastInstanceNotDefinedException, ServiceUnavailableException {
        log.debug("Started clean DynamoDB ...");
        jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(captureI18NMessageSource.getMessage("capture.job.backup.process.clean.old.data"), null));
        final Set<Class<?>> classesList = getBackUpClassesList();
        String ctId = acHostModel.getCtId();
        for (Class clazz : classesList) {
            String entityName = getEntityName(clazz);
            @SuppressWarnings("unchecked")
            BackupEntity annotation = (BackupEntity) clazz.getAnnotation(BackupEntity.class);
            String entityType = annotation.entityType();
            if (!exceptionList.contains(entityType)) {
                cleanDynamoDBDateForCtid(acHostModel, annotation, entityName);
            }
        }

        log.debug("Done with clean DynamoDB.");
    }


    private void cleanDynamoDBDateForCtid(AcHostModel acHostModel, BackupEntity annotation, String entityName) throws CaptureRuntimeException {
        if (StringUtils.equals(entityName, ApplicationConstants.SESSION_ENTITY)) {
            dynamoDBOperations.deleteAllItems(acHostModel.getCtId(), ApplicationConstants.SESSION_TABLE_NAME);
        } else if (StringUtils.equals(entityName, ApplicationConstants.SESSIONACTIVITY_ENTITY)) {
            dynamoDBOperations.deleteAllItems(acHostModel.getCtId(), ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        } else if (StringUtils.equals(entityName, ApplicationConstants.VARIABLE_ENTITY)) {
            dynamoDBOperations.deleteAllItems(acHostModel.getCtId(), ApplicationConstants.VARIABLE_TABLE_NAME);
        } else if (StringUtils.equals(entityName, ApplicationConstants.TEMPLATE_ENTITY)) {
            dynamoDBOperations.deleteAllItems(acHostModel.getCtId(), ApplicationConstants.TEMPLATE_TABLE_NAME);
        }
    }


    private void getUncompressBackupFileName(File backUpFile, File destUncompressFile) throws ServiceUnavailableException, IncompatibleBackupException {
        FileInputStream fileInputStream;
        org.xerial.snappy.SnappyInputStream snappyInputStream;
        try {
            fileInputStream = new FileInputStream(backUpFile);
            snappyInputStream = new org.xerial.snappy.SnappyInputStream(fileInputStream);
        } catch (FileNotFoundException exception) {
            log.error("Can't found backup temporary file", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        } catch (IOException exception) {
            log.error("Error during uncompress backup file.", exception);
            throw new IncompatibleBackupException(captureI18NMessageSource.getMessage("capture.job.restore.error.uncompress.file"));
        } catch (Exception exception) {
            log.error("Error during uncompress backup file.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        }
        try {
            Files.copy(snappyInputStream, destUncompressFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            log.warn("Error uncompress backup file with Snappy.", exception);
            throw new ServiceUnavailableException("capture.job.restore.internalservererror");
        } finally {
            try {
                snappyInputStream.close();
            } catch (IOException e) {
                log.warn("Error close SnappyInputStream after uncompress backup file", e);
            }
        }
    }


    private BackupSystemInfoBuffer.BackupSystemInfo extractSystemFileInfo(File systemInfoFile) throws ServiceUnavailableException {
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(systemInfoFile);
        } catch (FileNotFoundException exception) {
            log.error("File not found exception", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        }
        BackupSystemInfoBuffer.BackupSystemInfo backupSystemInfo;
        try {
            backupSystemInfo = BackupSystemInfoBuffer.BackupSystemInfo.parseFrom(inputStream);
        } catch (Exception exception) {
            log.error("File not found exception", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        } finally {
            try {
                inputStream.close();
            } catch (Exception exception) {
                log.warn("Can't close stream.", exception);
            }
        }
        return backupSystemInfo;
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
        if (ApplicationConstants.NUMBER_STORED_BACKUPS <= (backupsList.size())) {
            deleteBackup(acHostModel, backupsList.get(0).getKey());
        }
    }

    private Map<String, Integer> createProtocolBufferObject(AcHostModel acHostModel, File backupFile, String
            jobProgressTicket) throws
            CaptureRuntimeException, HazelcastInstanceNotDefinedException, ServiceUnavailableException {
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
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        } catch (IllegalAccessException exception) {
            log.error("Error during 'getEntity(Long, Class)' method, class=" + clazz, exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        }
        return entityConstant;
    }

    private int addObjectsBufferFromDynamo(AcHostModel acHostModel, String entityName, String
            jobProgressTicket, File backupFile, BackupEntity annotation) throws
            CaptureRuntimeException, HazelcastInstanceNotDefinedException, ServiceUnavailableException {
        jobProgressService.setStepMessage(acHostModel, jobProgressTicket, JobProgress.toJsonString(getMessageForEntity(entityName), 0));
        Integer totalObjects = 0;
        if (StringUtils.equals(entityName, ApplicationConstants.SESSION_ENTITY)) {
            Integer offset = 0;
            long total;
            do {
                Page<Session> sessionsPage = sessionRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(offset/ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT, ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT));
                totalObjects += storeSessionList(sessionsPage, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.entityType());
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
                totalObjects += storeSessionActivityList(sessionActivities, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.entityType());
                offset += ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT;
            } while (lastKeyEvaluated != null);
        } else if (StringUtils.equals(entityName, ApplicationConstants.VARIABLE_ENTITY)) {
            Integer offset = 0;
            long total;
            do {
                Page<Variable> variablePage = variableRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(offset, ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT));
                totalObjects += storeVariableList(variablePage, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.entityType());
                total = variablePage.getTotalElements();
                offset += ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT;
            } while (offset < total);
        } else if (StringUtils.equals(entityName, ApplicationConstants.TEMPLATE_ENTITY)) {
            Integer offset = 0;
            long total;
            do {
                Page<Template> templatesPage = templateRepository.findByCtId(acHostModel.getCtId(), CaptureUtil.getPageRequest(offset, ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT));
                totalObjects += storeTemplateList(templatesPage, acHostModel, entityName, totalObjects, backupFile, jobProgressTicket, annotation.entityType());
                total = templatesPage.getTotalElements();
                offset += ApplicationConstants.MAX_BULK_RECORDS_DEFAULT_LIMIT;
            } while (offset < total);
        }

        jobProgressService.addCurrentStepMessageToMessages(acHostModel, jobProgressTicket);
        return totalObjects;

    }

    private String getMessageForEntity(String entity) {
        if (StringUtils.equals(entity, ApplicationConstants.SESSION_ENTITY)) {
            return captureI18NMessageSource.getMessage("capture.job.backup.process.session");
        } else if (StringUtils.equals(entity, ApplicationConstants.SESSIONACTIVITY_ENTITY)) {
            return captureI18NMessageSource.getMessage("capture.job.backup.process.sessionactivity");
        } else if (StringUtils.equals(entity, ApplicationConstants.VARIABLE_ENTITY)) {
            return captureI18NMessageSource.getMessage("capture.job.backup.process.variable");
        } else if (StringUtils.equals(entity, ApplicationConstants.TEMPLATE_ENTITY)) {
            return captureI18NMessageSource.getMessage("capture.job.backup.process.template");
        }
        return null;
    }

    private int storeSessionList(Page<Session> sessionsPage, AcHostModel acHostModel, String entityName, Integer
            totalObjects, File backupFile, String jobProgressTicket, String entityType) throws
            CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (sessionsPage != null) {
            List<Session> sessions = sessionsPage.getContent();
            if (sessions != null && sessions.size() > 0) {
                sessions.forEach(session -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setEntity(bucketName);
                    objectBuffer.setEntityType(entityType);
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

    private int storeSessionActivityList(Page<SessionActivity> sessionActivitiesPage, AcHostModel
            acHostModel, String entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String
                                                 entityType) throws CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (sessionActivitiesPage != null) {
            List<SessionActivity> sessionActivities = sessionActivitiesPage.getContent();
            if (sessionActivities != null && sessionActivities.size() > 0) {
                sessionActivities.forEach(sessionActivity -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setEntity(bucketName);
                    objectBuffer.setEntityType(entityType);
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

    private int storeSessionActivityList(List<SessionActivity> sessionActivities, AcHostModel
            acHostModel, String entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String
                                                 entityType) throws CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (sessionActivities != null && sessionActivities.size() > 0) {
            sessionActivities.forEach(sessionActivity -> {
                Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                objectBuffer.setEntity(bucketName);
                objectBuffer.setEntityType(entityType);
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

    private int storeVariableList(Page<Variable> variablesPage, AcHostModel acHostModel, String
            entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String entityType) throws
            CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (variablesPage != null) {
            List<Variable> variables = variablesPage.getContent();
            if (variables != null && variables.size() > 0) {
                variables.forEach(variable -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setEntity(bucketName);
                    objectBuffer.setEntityType(entityType);
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

    private int storeTemplateList(Page<Template> templatesPage, AcHostModel acHostModel, String
            entityName, Integer totalObjects, File backupFile, String jobProgressTicket, String entityType) throws
            CaptureRuntimeException, ServiceUnavailableException, HazelcastInstanceNotDefinedException {
        Backup.Builder backUp = Backup.newBuilder();
        if (templatesPage != null) {
            List<Template> templates = templatesPage.getContent();
            if (templates != null && templates.size() > 0) {
                templates.forEach(template -> {
                    Backup.Object.Builder objectBuffer = Backup.Object.newBuilder();
                    String bucketName = acHostModel.getCtId() + ApplicationConstants.BUCKET_KEY_SEPARATOR + entityName;
                    objectBuffer.setEntity(bucketName);
                    objectBuffer.setEntityType(entityType);
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
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
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
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
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
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        } catch (IOException exception) {
            log.error("Error during compress file", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        }
    }

    private void createSystemInfoProtoBuffFile(AcHostModel acHostModel, File
            destFile, Map<String, Integer> resultReport, File backupFile, String userKey) throws
            ServiceUnavailableException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(resultReport);
        } catch (Exception exception) {
            log.error("Error during parse map to json string.", exception);
        }
        String creator = userKey != null ? userKey : "system";
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
            log.warn("Can't get ctId during create Backup SystemInfo.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        }
        systemInfoBuilder.setNumberOfObjects(extractTotalStoredObjectsFromJobProgress(resultReport));
        BackupSystemInfoBuffer.BackupSystemInfo systemInfo = systemInfoBuilder.build();

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(destFile);
        } catch (FileNotFoundException exception) {
            log.error("Can't find file during create FileOutputStream, fileName=" + destFile.getName(), exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        }
        try {
            systemInfo.writeTo(fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException exception) {
            log.error("Can't find file during create FileOutputStream, fileName=" + destFile.getName(), exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
        } catch (IOException exception) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                log.warn("Error during close outputStream in method 'createTemporaryFileWithByteData'.", e);
            }
            log.error("Error during write backUpBuffer into OutputStream.", exception);
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
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
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
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
            throw new ServiceUnavailableException(captureI18NMessageSource.getMessage("capture.job.restore.internalservererror"));
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
