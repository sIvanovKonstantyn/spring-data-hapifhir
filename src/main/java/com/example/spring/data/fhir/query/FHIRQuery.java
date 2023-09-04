package com.example.spring.data.fhir.query;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FHIRQuery implements RepositoryQuery {

    private final Method method;
    private final RepositoryMetadata metadata;
    private final ProjectionFactory factory;

    private final List<String> paramNames = new ArrayList<>();

    private final Class returnType;
    private final IGenericClient fhirClient;

    public FHIRQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, IGenericClient fhirClient) {
        this.method = method;
        this.metadata = metadata;
        this.factory = factory;
        this.fhirClient = fhirClient;
        this.returnType = metadata.getReturnedDomainClass(method);
        extractParametersNames(method, metadata);
    }

    private void extractParametersNames(Method method, RepositoryMetadata metadata) {
        var queryName = method.getName();
        if (method.getParameterCount() > 0) {
            PartTree tree = new PartTree(queryName, metadata.getReturnedDomainClass(method));
            if (tree.iterator().hasNext()) {
                for (PartTree.OrPart orPart : tree) {
                    for (Part part : orPart) {
                        paramNames.add(part.getProperty().getLeafProperty().getSegment());
                    }
                }
            }
        }
    }

    @Override
    public Object execute(Object[] parameters) {
        return new FHIRQueryExecutor().execute(parameters, returnType, paramNames, fhirClient);
    }


    @Override
    public QueryMethod getQueryMethod() {
        return new QueryMethod(method, metadata, factory);
    }
}
