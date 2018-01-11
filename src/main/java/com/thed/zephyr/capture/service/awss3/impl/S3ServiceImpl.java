package com.thed.zephyr.capture.service.awss3.impl;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.thed.zephyr.capture.exception.S3PluginException;
import com.thed.zephyr.capture.service.awss3.S3Service;
import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.DynamicProperty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
public class S3ServiceImpl implements S3Service {
    private static final Logger log = LoggerFactory.getLogger(S3ServiceImpl.class);
    @Autowired
    private AmazonS3Client amazonS3Client;
    @Autowired
    private DynamicProperty dynamicProperty;

    public static final String AWS_ACCESS_KEY = "aws.access.key";
    public static final String AWS_SECRET_KEY = "aws.secret.key";
    public static final String AWS_BUCKET = "aws.backup.bucket";
    public static final String AWS_BUCKET_POSTFIX = "aws.bucket.postfix";
    public static final String AC_KEY = "ac.key";

    @Override
    public List<S3ObjectSummary> getListOfFiles(String prefix, Boolean withPrefix, Boolean withSorting) throws S3PluginException {
        List<S3ObjectSummary> result = new ArrayList<>();
        Bucket s3Bucket = createBucket();
        prefix = prependBucketPostfixToFileName(prefix);
        ObjectListing objectListing;
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(s3Bucket.getName())
                .withPrefix(prefix);
        if (withPrefix) {
            listObjectsRequest.withDelimiter(ApplicationConstants.S3_DELIMITER);
        }
        try {
            do {
                objectListing = amazonS3Client.listObjects(listObjectsRequest);
                result.addAll(objectListing.getObjectSummaries());
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
        } catch (Exception exception) {
            log.error("Error during get list of files in S3Plugin, bucketName:{}", s3Bucket.getName(), exception);
            throw new S3PluginException(exception);
        }
        if (withSorting) {
            result = sortBackupsListByDate(result);
        }
        return result;
    }

    @Override
    public File getFileContent(String fileName, File destinationFile) throws S3PluginException {
        Bucket s3Bucket = createBucket();
        try {
            fileName = prependBucketPostfixToFileName(fileName);
            GetObjectRequest getObjectRequest = new GetObjectRequest(s3Bucket.getName(), fileName);
            amazonS3Client.getObject(getObjectRequest, destinationFile);
        } catch (Exception exception) {
            if (exception instanceof AmazonS3Exception && ((AmazonS3Exception)exception).getStatusCode() == 404) {
                log.warn("Can't find file in S3 service, bucketName:{} fileName:{}", s3Bucket.getName(), fileName, exception);
                throw new S3PluginException(exception);
            }
            log.error("Error during getting File content in S3Plugin, bucketName:{} fileName:{} destination file:", s3Bucket.getName(), fileName, destinationFile, exception);
            throw new S3PluginException(exception);
        }
        return destinationFile;
    }

    @Override
    public Boolean saveFile(String fileName, File file, Map<String, String> metaData) throws S3PluginException {
        Bucket s3Bucket = createBucket();
        try {
            fileName = prependBucketPostfixToFileName(fileName);
            PutObjectRequest putObjectRequest = new PutObjectRequest(s3Bucket.getName(), fileName, file);
            if (!metaData.isEmpty()) {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                for (Map.Entry<String, String> entry : metaData.entrySet()) {
                    objectMetadata.addUserMetadata(entry.getKey(), entry.getValue());
                }
                putObjectRequest.setMetadata(objectMetadata);
            }
            amazonS3Client.putObject(putObjectRequest);

            return true;
        } catch (Exception exception) {
            log.error("Error during save file in S3Plugin, bucketName:{} fileName:{}", s3Bucket.getName(), fileName, exception);
            throw new S3PluginException(exception);
        }
    }

    public void deleteFile(String fileName) throws S3PluginException {
        Bucket s3Bucket = createBucket();
        try {
            fileName = prependBucketPostfixToFileName(fileName);
            amazonS3Client.deleteObject(s3Bucket.getName(), fileName);
        } catch (Exception exception) {
            if (exception instanceof AmazonS3Exception && ((AmazonS3Exception) exception).getStatusCode() == 404) {
                log.warn("Can't find file in s3 service probably it's already deleted bucketName:{} fileName:{}", s3Bucket.getName(), fileName, exception);
            } else {
                log.error("Error during delete file in S3Plugin, bucketName:{} fileName:{}", s3Bucket.getName(), fileName, exception);
                throw new S3PluginException(exception);
            }
        }
    }

    private Bucket createBucket() throws S3PluginException {
        String bucketName = dynamicProperty.getStringProp(AWS_BUCKET, null).get();
        if (StringUtils.isBlank(bucketName)) {
            bucketName = dynamicProperty.getStringProp(AC_KEY, null).get();
        }
        try {
            if (amazonS3Client.doesBucketExist(bucketName)) {
                log.warn("The bucket:{} already exist! Check your permissions to this bucket.", bucketName);
                return new Bucket(bucketName);
            }
            Bucket bucket = amazonS3Client.createBucket(bucketName);
            log.info("Created bucket:{} in AWS S3 service.", bucket.getName());
            return bucket;
        } catch (Exception exception) {
            log.error("Error during create new bucket in S3Plugin, bucketName:{} ", bucketName, exception);
            throw new S3PluginException(exception);
        }
    }

    private String prependBucketPostfixToFileName(String fileName) {
        return dynamicProperty.getStringProp(AWS_BUCKET_POSTFIX, "captureTenantData").get() + ApplicationConstants.S3_DELIMITER + fileName;
    }

    private List<S3ObjectSummary> sortBackupsListByDate(List<S3ObjectSummary> backupsList) {
        List<S3ObjectSummary> resultList = new ArrayList<S3ObjectSummary>();
        for (S3ObjectSummary s3ObjectSummary : backupsList) {
            if (s3ObjectSummary.getSize() > 0) {
                resultList.add(s3ObjectSummary);
            }
        }
        Collections.sort(resultList, new Comparator<S3ObjectSummary>() {
            @Override
            public int compare(S3ObjectSummary o1, S3ObjectSummary o2) {
                return o1.getLastModified().getTime() < o2.getLastModified().getTime() ? -1 :
                        o1.getLastModified().getTime() == o2.getLastModified().getTime() ? 0 : 1;
            }
        });
        return resultList;
    }


}
