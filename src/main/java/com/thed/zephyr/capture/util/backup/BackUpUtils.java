package com.thed.zephyr.capture.util.backup;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CreateTarArchiveException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarOutputStream;

import java.io.*;
import java.util.Map;

public class BackUpUtils {
    private static Logger log = LoggerFactory.getLogger(BackUpUtils.class);

    public static String getTemporaryFolderName(AcHostModel acHostModel, String folderName) {
        String path = ApplicationConstants.TEMPORARY_FOLDER_PATH + folderName;
        new File(path).mkdirs();
        path += "/" + acHostModel.getCtId().replace(":", ".");
        new File(path).mkdirs();
        return String.valueOf(path);
    }

    public static String getBackupsS3Prefix(AcHostModel acHostModel) throws CaptureRuntimeException {
        String prefix = String.valueOf(StringUtils.replaceChars(acHostModel.getCtId(), ":", ".") +
                ApplicationConstants.S3_DELIMITER +
                ApplicationConstants.S3_BACKUP_FOLDER +
                ApplicationConstants.S3_DELIMITER);
        return prefix;
    }

    public static void createTarArchive(File destFile, Map<String, File> archiveFilesMap) throws CaptureRuntimeException {
        try {
            FileOutputStream dest = new FileOutputStream(destFile);
            TarOutputStream out = new TarOutputStream(new BufferedOutputStream(dest));
            for (Map.Entry<String, File> entry : archiveFilesMap.entrySet()) {
                out.putNextEntry(new TarEntry(entry.getValue(), entry.getKey()));
                BufferedInputStream origin = new BufferedInputStream(new FileInputStream(entry.getValue()));
                int count;
                byte data[] = new byte[2048];
                while ((count = origin.read(data)) != -1) {
                    out.write(data, 0, count);
                }
                out.flush();
                origin.close();
            }
            out.close();
        } catch (Exception exception) {
            log.error("Error during create tar zip of backup.", exception);
            throw new CreateTarArchiveException(exception);
        }
    }
}
