package com.example.spring.data.fhir.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

public class FhirRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableFhirRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new FhirRepositoryConfigExtension();
    }
}
