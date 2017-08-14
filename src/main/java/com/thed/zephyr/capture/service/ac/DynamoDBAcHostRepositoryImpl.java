package com.thed.zephyr.capture.service.ac;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.atlassian.connect.spring.AtlassianHost;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * Created by aliakseimatsarski on 8/11/17.
 */
public class DynamoDBAcHostRepositoryImpl implements DynamoDBAcHostRepository {

    @Autowired
    private Logger log;

    @Autowired
    private AcHostModelRepository acHostModelRepository;

    @Override
    public Optional<AtlassianHost> findFirstByBaseUrl(String s) {
        return null;
    }

    @Override
    public AtlassianHost  save(AtlassianHost atlassianHost) {
        AcHostModel acHostModel = new AcHostModel(atlassianHost);
        acHostModel.setStatus(AcHostModel.TenantStatus.ACTIVE);
        return acHostModelRepository.save(acHostModel);
    }

    @Override
    public <S extends AtlassianHost> Iterable<S> save(Iterable<S> iterable) {
        return null;
    }

    @Override
    public AtlassianHost findOne(String tenantKey) {
        List<AcHostModel> acHostModel = acHostModelRepository.findByClientKey(tenantKey);
        if (acHostModel.size() == 1){
            return acHostModel.get(0);
        } else if(acHostModel.size() > 1){
            log.error("The error during getting AcHostModel from DB. Found more then one row with the same clientKey:{}", tenantKey);
            return acHostModel.get(0);
        }

        return null;
    }

    @Override
    public boolean exists(String s) {
        return false;
    }

    @Override
    public Iterable<AtlassianHost> findAll() {
        return null;
    }

    @Override
    public Iterable<AtlassianHost> findAll(Iterable<String> iterable) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(String s) {
        log.debug("Triggere delete method s:{}", s);
    }

    @Override
    public void delete(AtlassianHost atlassianHost) {
        log.debug("Triggere delete method atlassianHost:{}", atlassianHost);
    }

    @Override
    public void delete(Iterable<? extends AtlassianHost> iterable) {
        log.debug("Triggere delete method with collection of atlassianHost");
    }

    @Override
    public void deleteAll() {
        log.debug("Triggere delete all method ");
    }
}
