package com.example.spring.data.fhir.repository.support;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

public class FhirRepositoryFactoryBean<T extends Repository<S, String>, S>
        extends RepositoryFactoryBeanSupport<T, S, String> implements BeanFactoryAware {
    private static final String FHIR_CLIENT_BEAN_NAME = "fhirClient";
    private IGenericClient fhirClient;

    protected FhirRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        super.setBeanFactory(beanFactory);
        this.fhirClient = beanFactory.getBean(FHIR_CLIENT_BEAN_NAME, IGenericClient.class);
    }
    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
        Assert.notNull(fhirClient, "fhirClient bean should be created!");
        return new FhirRepositoryFactory(fhirClient);
    }
}
