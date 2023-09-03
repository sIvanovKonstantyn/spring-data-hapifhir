package com.example.spring.data.fhir.config;

import com.example.spring.data.fhir.repository.support.FhirRepositoryFactoryBean;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

public class FhirRepositoryConfigExtension
        extends RepositoryConfigurationExtensionSupport {
    private static final String MODULE_NAME = "Fhir";

    @Override
    protected String getModulePrefix() {
        return getModuleIdentifier();
    }

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {
        return FhirRepositoryFactoryBean.class.getName();
    }
}
