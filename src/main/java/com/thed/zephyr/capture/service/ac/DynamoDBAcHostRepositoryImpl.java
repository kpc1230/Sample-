package com.thed.zephyr.capture.service.ac;

import com.atlassian.connect.spring.AtlassianHost;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.model.AcHostModel.GDPRMigrationStatus;
import com.thed.zephyr.capture.repositories.dynamodb.AcHostModelRepository;
import com.thed.zephyr.capture.util.ApplicationConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
        log.debug("Call findFirstByBaseUrl baseUrl:{}", baseUrl);
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
        log.debug("Call save atlassianHost:{}", atlassianHost.getClientKey());
        if(exists(atlassianHost.getClientKey())){
            return updateExistingHost(atlassianHost, false);
        } else if(isTenantTemporary(atlassianHost.getClientKey())){
            return updateExistingHost(atlassianHost, true);
        }
        AcHostModel acHostModel = new AcHostModel(atlassianHost);
        acHostModel.setStatus(AcHostModel.TenantStatus.ACTIVE);
        acHostModel.setMigrated(GDPRMigrationStatus.GDPR);
        removeTenantFromCache(atlassianHost);

        return acHostModelRepository.save(acHostModel);
    }

    @Override
    public <S extends AtlassianHost> Iterable<S> save(Iterable<S> iterable) {
        return null;
    }

    @Override
    public AtlassianHost findOne(String tenantKey) {
        log.trace("Call findOne tenantKey:{}", tenantKey);
        IMap<String, AcHostModel> tenants = hazelcastInstance.getMap(ApplicationConstants.LOCATION_ACHOST);
        AcHostModel acHostModel = tenants.get(tenantKey);
        if(acHostModel == null){
            log.trace("Get tenant by key:{} from DynamoDB", tenantKey);
            List<AcHostModel> acHostModels = acHostModelRepository.findByClientKey(tenantKey);
            if(acHostModels.size() > 0){
                acHostModel = acHostModels.get(0);
                if(acHostModel.getStatus() == AcHostModel.TenantStatus.TEMPORARY){
                    return null;
                }
                putTenantIntoCache(tenants, acHostModel);
            }
            if (acHostModels.size() > 1) {
                log.warn("The error during getting AcHostModel from DB. Found more then one row with the same baseUrl:{}", tenantKey);
            }
        } else {
            log.trace("Get tenant by key:{} from cache", tenantKey);
        }

        return acHostModel;
    }

    @Override
    public boolean exists(String clientKey) {
        log.debug("Call exists clientKey:{}", clientKey);
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

    @Override
    public AcHostModel findByCtId(String ctId) {
        IMap<String, AcHostModel> tenants = hazelcastInstance.getMap(ApplicationConstants.LOCATION_ACHOST);
        AcHostModel acHostModel = tenants.get(ctId);
        if(acHostModel == null){
            log.debug("Getting acHostModel from dynamoDB ctId:{}", ctId);
            acHostModel = acHostModelRepository.findOne(ctId);
            if(acHostModel != null){
                putTenantIntoCache(tenants, acHostModel);
            }
        }

        return acHostModel;
    }

    private void putTenantIntoCache(IMap<String, AcHostModel> tenants, AcHostModel acHostModel){
        tenants.put(acHostModel.getClientKey(), acHostModel);
        tenants.put(acHostModel.getCtId(), acHostModel);
        if(acHostModel.getBaseUrl() != null){
            tenants.put(acHostModel.getBaseUrl(), acHostModel);
        }
    }

    private void removeTenantFromCache(AtlassianHost atlassianHost){
        IMap<String, AcHostModel> tenants = hazelcastInstance.getMap(ApplicationConstants.LOCATION_ACHOST);
        AcHostModel acHostModel = tenants.get(atlassianHost.getClientKey());
        tenants.remove(atlassianHost.getClientKey());
        tenants.remove(atlassianHost.getBaseUrl());
        if(acHostModel != null){
            tenants.remove(acHostModel.getCtId());
        }
    }

    private boolean isTenantTemporary(String clientKey){
        List<AcHostModel> acHostModels = acHostModelRepository.findByClientKey(clientKey);

        return acHostModels.size() > 0 && acHostModels.get(0).getStatus() == AcHostModel.TenantStatus.TEMPORARY;
    }

    private AtlassianHost updateExistingHost(AtlassianHost atlassianHost, boolean temporaryTenant){
        if (atlassianHost instanceof AcHostModel){
            removeTenantFromCache(atlassianHost);
            ((AcHostModel)atlassianHost).setLastModifiedDate(Calendar.getInstance());
            return  acHostModelRepository.save((AcHostModel)atlassianHost);
        }
        AcHostModel oldAcHostModel;
        if(temporaryTenant){
            oldAcHostModel = getTemporaryTenant(atlassianHost.getClientKey());
        } else{
            oldAcHostModel = (AcHostModel)findOne(atlassianHost.getClientKey());
        }
        AcHostModel acHostModel = new AcHostModel(atlassianHost);
        acHostModel.setCtId(oldAcHostModel.getCtId());
        acHostModel.setCreatedDate(oldAcHostModel.getCreatedDate() != null?oldAcHostModel.getCreatedDate():acHostModel.getCreatedDate());
        acHostModel.setStatus(atlassianHost.isAddonInstalled()? AcHostModel.TenantStatus.ACTIVE: AcHostModel.TenantStatus.UNINSTALLED);
        acHostModel.setMigrated(oldAcHostModel.getMigrated());
        log.info("AcHostModel updated installed:{}", atlassianHost.isAddonInstalled());
        removeTenantFromCache(atlassianHost);

        return  acHostModelRepository.save(acHostModel);
    }

    private AcHostModel getTemporaryTenant(String clientKey){
        List<AcHostModel> acHostModels = acHostModelRepository.findByClientKey(clientKey);
        if(acHostModels.size() > 0 && acHostModels.get(0).getStatus() == AcHostModel.TenantStatus.TEMPORARY){
            return acHostModels.get(0);
        }
        return null;
    }
}
