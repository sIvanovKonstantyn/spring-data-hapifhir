package com.example.spring.data.fhir.query;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.example.spring.data.fhir.annotations.Query;
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
            Query annotation = method.getDeclaredAnnotation(Query.class);

            if (annotation != null) {
                extractFromQuery(annotation.value());
            } else {
                extractFromMethodName(method, metadata, queryName);
            }
        }
    }

    private void extractFromQuery(String query) {
        // Query validation can be added here...
        // Selected fields can be parsed here...
        // Sorting logic and pagination can be parsed here...

        String[] queryParts = query.split("WHERE", 2);
        String[] filters = queryParts[1].split("AND");
        for (String filter : filters) {
            String[] filterParts = filter.split("=");
            if (filterParts[1].contains(":")) {
                paramNames.add(filterParts[0].trim());
            }
        }
    }

    private void extractFromMethodName(Method method, RepositoryMetadata metadata, String queryName) {
        PartTree tree = new PartTree(queryName, metadata.getReturnedDomainClass(method));
        if (tree.iterator().hasNext()) {
            for (PartTree.OrPart orPart : tree) {
                for (Part part : orPart) {
                    paramNames.add(part.getProperty().getLeafProperty().getSegment());
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
