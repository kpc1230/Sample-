package com.thed.zephyr.capture.service.ac;

import com.atlassian.connect.spring.AtlassianHost;
import com.thed.zephyr.capture.model.AcHostModel;
import com.thed.zephyr.capture.repositories.AcHostModelRepository;
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

    @Override
    public Optional<AtlassianHost> findFirstByBaseUrl(String baseUrl) {
        Optional<AtlassianHost> option = Optional.empty();
        List<AcHostModel> acHostModel = acHostModelRepository.findByBaseUrl(baseUrl);
        if (acHostModel.size() == 1) {
            return option.of(acHostModel.get(0));
        } else if (acHostModel.size() > 1) {
            log.warn("The error during getting AcHostModel from DB. Found more then one row with the same baseUrl:{}", baseUrl);
            return option.of(acHostModel.get(0));
        }
        return option;
    }

    @Override
    public AtlassianHost  save(AtlassianHost atlassianHost) {
        if(exists(atlassianHost.getClientKey())){

            return updateExistingHost(atlassianHost);
        }
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
            log.warn("The error during getting AcHostModel from DB. Found more then one row with the same clientKey:{}", tenantKey);
            return acHostModel.get(0);
        }

        return null;
    }

    @Override
    public boolean exists(String clientKey) {
        return findOne(clientKey) != null?true:false;
    }

    @Override
    public Iterable<AtlassianHost> findAll() {
        Iterable<AcHostModel> acHostModelList = acHostModelRepository.findAll();
        List<AtlassianHost> result = new ArrayList<>();
        for(AcHostModel acHostModel:acHostModelList){
            result.add((AtlassianHost)acHostModel);
        }
        return result;
    }

    @Override
    public Iterable<AtlassianHost> findAll(Iterable<String> iterable) {
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
        AcHostModel acHostModel = (AcHostModel)findOne(clientKey);
        acHostModelRepository.delete(acHostModel.getCtId());
        log.debug("The tenant:{} was deleted", clientKey);
    }

    @Override
    public void delete(AtlassianHost atlassianHost) {
        delete(atlassianHost.getClientKey());
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
        log.debug("Triggere delete all method ");
    }

    private AtlassianHost updateExistingHost(AtlassianHost atlassianHost){
        AcHostModel oldAcHostModel = (AcHostModel)findOne(atlassianHost.getClientKey());
        AcHostModel acHostModel = new AcHostModel(atlassianHost);
        acHostModel.setCtId(oldAcHostModel.getCtId());
        acHostModel.setStatus(atlassianHost.isAddonInstalled()? AcHostModel.TenantStatus.ACTIVE: AcHostModel.TenantStatus.UNINSTALLED);

        return  acHostModelRepository.save(acHostModel);
    }
}
