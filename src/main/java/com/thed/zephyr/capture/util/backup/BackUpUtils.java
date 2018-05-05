package com.thed.zephyr.capture.util.backup;

import com.thed.zephyr.capture.exception.CaptureRuntimeException;
import com.thed.zephyr.capture.exception.CreateTarArchiveException;
import com.thed.zephyr.capture.exception.ExtractTarArchiveException;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.UniqueIdGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xeustechnologies.jtar.TarOutputStream;

import java.io.*;
import java.util.HashMap;
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

    public static String createRandomFileName(AcHostModel acHostModel) {
        try {
            // we need this sleep to avoid generate same names
            Thread.sleep(5);
        } catch (InterruptedException e) {
            log.debug("Error during Thread.sleep");
        }
        return new UniqueIdGenerator().getStringId();
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
            throw new CreateTarArchiveException("zephyr.common.error.minor.issue", ApplicationConstants.CreateTarArchiveException_ERROR_KEY + "", exception);
        }
    }

    public static Map<String, File> extractTarArchive(AcHostModel acHostModel, File archive, String tempPath) throws ExtractTarArchiveException {
        if (archive.length() < 3000) {
            throw new ExtractTarArchiveException("File size very small for tar archive.");
        }
        Map<String, File> result = new HashMap<>();
        TarInputStream tis;
        try {
            tis = new TarInputStream(new BufferedInputStream(new FileInputStream(archive)));
        } catch (FileNotFoundException e) {
            throw new ExtractTarArchiveException(e);
        }

        try {
            TarEntry entry;
            while ((entry = tis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[2048];
                File file = new File(tempPath + "/" + createRandomFileName(acHostModel));
                Thread.sleep(2);
                result.put(entry.getName(), file);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream dest = new BufferedOutputStream(fos);
                try {
                    while ((count = tis.read(data)) != -1) {
                        if (count == 0) {
                            throw new ExtractTarArchiveException("Internal error wile extract tar file ");
                        }
                        dest.write(data, 0, count);
                    }
                } finally {
                    dest.flush();
                    dest.close();
                }
            }
        } catch (Exception exception) {
            for (Map.Entry<String, File> entry : result.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().delete();
                }
            }
            throw new ExtractTarArchiveException(exception);
        } finally {
            try {
                tis.close();
            } catch (IOException exception) {
                log.warn("Can't close stream after extract tar archive.", exception);
            }
        }
        return result;
    }
}
