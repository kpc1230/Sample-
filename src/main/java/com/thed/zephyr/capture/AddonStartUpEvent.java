package com.thed.zephyr.capture;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.thed.zephyr.capture.repositories.SessionActivityRepository;
import com.thed.zephyr.capture.service.db.CreateSessionActivityTableRequest;
import com.thed.zephyr.capture.service.db.CreateSessionTableRequest;
import com.thed.zephyr.capture.service.db.CreateTemplateTableRequest;
import com.thed.zephyr.capture.service.db.CreateTenantTableRequest;
import com.thed.zephyr.capture.service.db.CreateVariableTableRequest;
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
    @Autowired
    private SessionActivityRepository baseSessionActivityItemRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        TableCollection<ListTablesResult> tables = dynamoDB.listTables();
        createVariablesTableIfNotExist(tables, dynamoDB);
        createTenantTableIfNotExist(tables, dynamoDB);
        createSessionTableIfNotExist(tables, dynamoDB);
        createTemplateTableIfNotExist(tables, dynamoDB);
        createSessionActivityTableIfNotExist(tables, dynamoDB);
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

    private void createTemplateTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        for (Table table:tables){
            if(StringUtils.equals(ApplicationConstants.TEMPLATE_TABLE_NAME, table.getTableName())){
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Template DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateTemplateTableRequest().build();
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Template table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", ApplicationConstants.TEMPLATE_TABLE_NAME);
        }
    }


    private void createSessionActivityTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        for (Table table : tables) {
            if (StringUtils.equals(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME, table.getTableName())) {
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating SessionActivity DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateSessionActivityTableRequest().build();
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The SessionActivity table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        }
    }

    private void createVariablesTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        for (Table table : tables) {
            if (StringUtils.equals(ApplicationConstants.VARIABLE_TABLE_NAME, table.getTableName())) {
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Variable DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateVariableTableRequest().build();
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Variable table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", ApplicationConstants.VARIABLE_TABLE_NAME);
        }
    }
}
