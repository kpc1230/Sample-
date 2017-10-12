package com.thed.zephyr.capture.service.ac;

import com.atlassian.connect.spring.AtlassianHost;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Override
    public Optional<AtlassianHost> findFirstByBaseUrl(String baseUrl) {
        Optional<AtlassianHost> option = Optional.empty();
        IMap<String, AcHostModel> tenants = hazelcastInstance.getMap(ApplicationConstants.LOCATION_ACHOST);
        AcHostModel acHostModel = tenants.get(baseUrl);
        if(acHostModel == null){
            log.debug("Get tenant by baseURL:{} from DynamoDB", baseUrl);
            List<AcHostModel> acHostModels = acHostModelRepository.findByBaseUrl(baseUrl);
            if(acHostModels.size() > 0){
                acHostModel = acHostModels.get(0);
                putTenantIntoCache(tenants, acHostModel);
            }
           if (acHostModels.size() > 1) {
                log.warn("The error during getting AcHostModel from DB. Found more then one row with the same baseUrl:{}", baseUrl);
            }
        } else {
            log.debug("Get tenant by baseURL:{} from cache", baseUrl);
        }

        return acHostModel == null?Optional.empty():option.of(acHostModel);
    }

    @Override
    public AtlassianHost  save(AtlassianHost atlassianHost) {
        if(exists(atlassianHost.getClientKey())){

            return updateExistingHost(atlassianHost);
        }
        AcHostModel acHostModel = new AcHostModel(atlassianHost);
        acHostModel.setStatus(AcHostModel.TenantStatus.ACTIVE);
        removeTenantFromCache(atlassianHost);
        return acHostModelRepository.save(acHostModel);
    }

    @Override
    public <S extends AtlassianHost> Iterable<S> save(Iterable<S> iterable) {
        return null;
    }

    @Override
    public AtlassianHost findOne(String tenantKey) {
        IMap<String, AcHostModel> tenants = hazelcastInstance.getMap(ApplicationConstants.LOCATION_ACHOST);
        AcHostModel acHostModel = tenants.get(tenantKey);
        if(acHostModel == null){
            log.debug("Get tenant by key:{} from DynamoDB", tenantKey);
            List<AcHostModel> acHostModels = acHostModelRepository.findByClientKey(tenantKey);
            if(acHostModels.size() > 0){
                acHostModel = acHostModels.get(0);
                putTenantIntoCache(tenants, acHostModel);
            }
            if (acHostModels.size() > 1) {
                log.warn("The error during getting AcHostModel from DB. Found more then one row with the same baseUrl:{}", tenantKey);
            }
        } else {
            log.debug("Get tenant by key:{} from cache", tenantKey);
        }

        return acHostModel;
    }

    @Override
    public boolean exists(String clientKey) {
        return findOne(clientKey) != null?true:false;
    }

    @Override
    public Iterable<AtlassianHost> findAll() {
        log.debug("Get all tenants from DynamoDB");
        Iterable<AcHostModel> acHostModelList = acHostModelRepository.findAll();
        List<AtlassianHost> result = new ArrayList<>();
        for(AcHostModel acHostModel:acHostModelList){
            result.add((AtlassianHost)acHostModel);
        }

        return result;
    }

    @Override
    public Iterable<AtlassianHost> findAll(Iterable<String> iterable) {
        log.debug("Get all tenants with iterable from DynamoDB");
        List<AtlassianHost> result = new ArrayList<>();
        for(String clientKey:iterable){
            result.add(findOne(clientKey));
        }

        return result;
    }

    @Override
    public long count() {
        return acHostModelRepository.count();
    }

    @Override
    public void delete(String clientKey) {
        log.debug("Delete tenant with key:{} from DynamoDB", clientKey);
        AcHostModel acHostModel = (AcHostModel)findOne(clientKey);
        acHostModelRepository.delete(acHostModel.getCtId());
        removeTenantFromCache(acHostModel);
        log.debug("The tenant:{} was deleted", clientKey);
    }

    @Override
    public void delete(AtlassianHost atlassianHost) {
        log.debug("Delete tenant with atlassianHost key:{} from DynamoDB", atlassianHost.getClientKey());
        delete(atlassianHost.getClientKey());
        removeTenantFromCache(atlassianHost);
        log.debug("The tenant:{} was deleted", atlassianHost.getClientKey());
    }

    @Override
    public void delete(Iterable<? extends AtlassianHost> iterable) {
        for(AtlassianHost atlassianHost:iterable){
            delete(atlassianHost);
        }
    }

    @Override
    public void deleteAll() {
    //    acHostModelRepository.deleteAll();
        log.debug("Triggered delete all tenants method.");
    }

    private AtlassianHost updateExistingHost(AtlassianHost atlassianHost){
        AcHostModel oldAcHostModel = (AcHostModel)findOne(atlassianHost.getClientKey());
        AcHostModel acHostModel = new AcHostModel(atlassianHost);
        acHostModel.setCtId(oldAcHostModel.getCtId());
        acHostModel.setStatus(atlassianHost.isAddonInstalled()? AcHostModel.TenantStatus.ACTIVE: AcHostModel.TenantStatus.UNINSTALLED);

        return  acHostModelRepository.save(acHostModel);
    }

    private void putTenantIntoCache(IMap<String, AcHostModel> tenants, AcHostModel acHostModel){
        tenants.put(acHostModel.getClientKey(), acHostModel);
        tenants.put(acHostModel.getBaseUrl(), acHostModel);
    }

    private void removeTenantFromCache(AtlassianHost atlassianHost){
        IMap<String, AcHostModel> tenants = hazelcastInstance.getMap(ApplicationConstants.LOCATION_ACHOST);
        tenants.remove(atlassianHost.getClientKey());
        tenants.remove(atlassianHost.getBaseUrl());
    }
}
