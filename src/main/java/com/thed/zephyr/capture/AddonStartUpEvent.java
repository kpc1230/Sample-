package com.thed.zephyr.capture;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.thed.zephyr.capture.service.db.CreateSessionTableRequest;
import com.thed.zephyr.capture.service.db.CreateTenantTableRequest;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by aliakseimatsarski on 8/11/17.
 */
@Component
public class AddonStartUpEvent implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private Logger log;
    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        TableCollection<ListTablesResult> tables = dynamoDB.listTables();
        createTenantTableIfNotExist(tables, dynamoDB);
        createSessionTableIfNotExist(tables, dynamoDB);
    }

    private void createTenantTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        for (Table table:tables){
            if(StringUtils.equals(ApplicationConstants.TENANT_TABLE_NAME, table.getTableName())){
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Tenant DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateTenantTableRequest().build();
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Tenant table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", ApplicationConstants.TENANT_TABLE_NAME);
        }
    }

    private void createSessionTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        for (Table table:tables){
            if(StringUtils.equals(ApplicationConstants.SESSION_TABLE_NAME, table.getTableName())){
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Session DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateSessionTableRequest().build();
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Session table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", ApplicationConstants.SESSION_TABLE_NAME);
        }
    }
}
