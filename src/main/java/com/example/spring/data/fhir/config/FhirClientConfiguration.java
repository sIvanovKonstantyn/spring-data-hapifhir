package com.example.spring.data.fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirClientConfiguration {
    @Value("${spring.data.fhir.base-url}")
    private String fhirBaseUrl;

    @Bean
    @ConditionalOnMissingBean(value = IGenericClient.class)
    public IGenericClient fhirClient() {
        return FhirContext.forR4()
                .newRestfulGenericClient(fhirBaseUrl);
    }
}
