package com.thed.zephyr.capture.service.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

public class DynamoDBTableNameResolver extends DynamoDBMapperConfig.DefaultTableNameResolver {

    @Autowired
    private Logger log;
    @Autowired
    private Environment environment;

    @Override
    public String getTableName(Class clazz, DynamoDBMapperConfig config) {
        String base = super.getTableName(clazz, config);
        String prefix =  environment.getProperty(ApplicationConstants.DYNAMODB_TABEL_NAME_PREFIX);
        log.debug("Table prefix:{} base:{}", prefix, base);

        return prefix == null ? base : prefix + base;
    }

    public String getTableNameWithPrefix(String tableName){
        String tableNamePrefix = environment.getProperty(ApplicationConstants.DYNAMODB_TABEL_NAME_PREFIX, "");
        log.debug("Table name:{} with prefix:{}", tableName, tableNamePrefix);

        return String.valueOf(tableNamePrefix + tableName);
    }
}
