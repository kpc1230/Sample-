package com.thed.zephyr.capture.service.awss3;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.thed.zephyr.capture.exception.S3PluginException;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface S3Service {
    public List<S3ObjectSummary> getListOfFiles(String prefix, Boolean withPrefix, Boolean withSorting) throws S3PluginException;

    public void deleteFile(String fileName) throws S3PluginException;

    public Boolean saveFile(String fileName, File file, Map<String, String> metaData) throws S3PluginException;
}
