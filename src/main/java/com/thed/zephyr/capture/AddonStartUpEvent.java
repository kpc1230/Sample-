package com.thed.zephyr.capture;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.atlassian.connect.spring.AddonUninstalledEvent;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.service.db.*;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
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
    private DynamoDBTableNameResolver dynamoDBTableNameResolver;
    @Autowired
    private AtlassianHostRepository atlassianHostRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        TableCollection<ListTablesResult> tables = dynamoDB.listTables();
        createTenantTableIfNotExist(tables, dynamoDB);
        createSessionTableIfNotExist(tables, dynamoDB);
        createTemplateTableIfNotExist(tables, dynamoDB);
        createSessionActivityTableIfNotExist(tables, dynamoDB);
        createVariableTableIfNotExist(tables, dynamoDB);
    }

    @EventListener
    public void uninstalledEvent(AddonUninstalledEvent addonUninstalledEvent){
        log.info("Add-on uninstalled event triggered ...");
        String tenantId = addonUninstalledEvent.getHost().getClientKey();
        try {
            AcHostModel acHostModel = (AcHostModel)atlassianHostRepository.findOne(tenantId);
            acHostModel.setStatus(AcHostModel.TenantStatus.UNINSTALLED);
            atlassianHostRepository.save(acHostModel);
        } catch (Exception exception) {
            log.error("Error during uninstalledEvent", exception);
        }
    }

	private void createTenantTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        String tenantTableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.TENANT_TABLE_NAME);
        for (Table table:tables){
            if(StringUtils.equals(tenantTableName, table.getTableName())){
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Tenant DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateTenantTableRequest().build(tenantTableName);
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Tenant table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", tenantTableName);
        }
    }

    private void createSessionTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        String sessionTableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.SESSION_TABLE_NAME);
        for (Table table:tables){
            if(StringUtils.equals(sessionTableName, table.getTableName())){
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Session DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateSessionTableRequest().build(sessionTableName);
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Session table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", sessionTableName);
        }
    }

    private void createTemplateTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        String templateTableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.TEMPLATE_TABLE_NAME);
        for (Table table:tables){
            if(StringUtils.equals(templateTableName, table.getTableName())){
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Template DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateTemplateTableRequest().build(templateTableName);
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Template table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", templateTableName);
        }
    }


    private void createSessionActivityTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB) {
        String sessionActivityTableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.SESSION_ACTIVITY_TABLE_NAME);
        for (Table table : tables) {
            if (StringUtils.equals(sessionActivityTableName, table.getTableName())) {
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating SessionActivity DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateSessionActivityTableRequest().build(sessionActivityTableName);
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The SessionActivity table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", sessionActivityTableName);
        }
    }

    private  void createVariableTableIfNotExist(TableCollection<ListTablesResult> tables, DynamoDB dynamoDB){
        String variableTableName = dynamoDBTableNameResolver.getTableNameWithPrefix(ApplicationConstants.VARIABLE_TABLE_NAME);
        for (Table table : tables) {
            if (StringUtils.equals(variableTableName, table.getTableName())) {
                log.debug("The table:{} already created, skip creation", table.getTableName());
                return;
            }
        }
        log.info("Creating Variable DynamoDB table ...");
        try {
            CreateTableRequest createTableRequest = new CreateVariableTableRequest().build(variableTableName);
            Table table = dynamoDB.createTable(createTableRequest);
            table.waitForActive();
            log.info("The Variable table was successfully created");
        } catch (InterruptedException e) {
            log.error("Error during creation DynamoDB table:{}", variableTableName);
        }
    }
}















