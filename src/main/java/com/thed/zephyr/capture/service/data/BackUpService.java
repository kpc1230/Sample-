package com.thed.zephyr.capture.service.data;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.HazelcastInstanceNotDefinedException;
import com.thed.zephyr.capture.model.AcHostModel;

import java.io.File;

public interface BackUpService {
     void createBackUp(AcHostModel acHostModel, String fileName, String jobProgressId,String userKey) throws HazelcastInstanceNotDefinedException;
     void restoreData(AcHostModel acHostModel, String fileName, File file, Boolean foreignTenantId, String jobProgressId, String userKey) throws HazelcastInstanceNotDefinedException;
     void deleteBackup(AcHostModel acHostModel, String s3FileKey) throws CaptureRuntimeException;
     void runDailyBackupJob();
}
