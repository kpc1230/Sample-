package com.atlassian.bonfire.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import com.atlassian.annotations.tenancy.TenancyScope;
import com.atlassian.annotations.tenancy.TenantAware;

@Service(BonfireComponentManager.SERVICE)
public class BonfireComponentManagerImpl implements BonfireComponentManager, BeanFactoryAware {

    @TenantAware(value = TenancyScope.TENANTLESS, comment = "Universal across all tenants")
    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (AutowireCapableBeanFactory) beanFactory;
    }

    @Override
    public <T> T instatiateComponent(Class<T> clazz) {
        //noinspection unchecked
        return (T) beanFactory.createBean(clazz);
    }
}
