package com.example.spring.data.fhir.query;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import java.lang.reflect.Method;

public class FHIRQueryLookupStrategy implements QueryLookupStrategy {

    private final IGenericClient fhirClient;

    public FHIRQueryLookupStrategy(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {
        return new FHIRQuery(method, metadata, factory, fhirClient);
    }
}
