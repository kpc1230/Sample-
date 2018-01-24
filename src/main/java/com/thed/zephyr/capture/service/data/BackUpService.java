package com.thed.zephyr.capture.service.data;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.view.JobProgress;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface BackUpService {
    JobProgress createBackUp(AcHostModel acHostModel, String fileName, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException;

    JobProgress restoreData(AcHostModel acHostModel, String fileName, File file, Boolean foreignTenantId, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException;

    JobProgress restoreMultiPartDataFile(AcHostModel acHostModel, String fileName, MultipartFile mfile, Boolean foreignTenantId, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException, IOException;

    void deleteBackup(AcHostModel acHostModel, String s3FileKey) throws CaptureRuntimeException;

    void runDailyBackupJob();

    public List<S3ObjectSummary> getBackUpsList(AcHostModel acHostModel) throws CaptureRuntimeException;

    public File getFullBackupFile(AcHostModel acHostModel, String s3FileName) throws CaptureRuntimeException;
}
