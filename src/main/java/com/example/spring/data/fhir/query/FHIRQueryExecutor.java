package com.example.spring.data.fhir.query;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.*;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class FHIRQueryExecutor {

    public Object execute(Object[] parameters, Class returnType , List<String> paramNames, IGenericClient fhirClient) {
        try {
            return BundleUtil
                    .toListOfResourcesOfType(
                            fhirClient.getFhirContext(),
                            fetchResourceByInputParameters(returnType, parameters, paramNames, fhirClient),
                            returnType
                    );
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Filter findFilterForParameter(List<Filter> filters, String parametersName) {
        return filters.stream()
                .filter(filter -> filter.name.equals(parametersName.toLowerCase()))
                .findAny()
                .orElse(null);
    }

    private void addFilter(Object[] args, IQuery<IBaseBundle> iQuery, int parameterIndex, boolean firstFilter, Filter currentFilter) {
        if (firstFilter) {
            iQuery
                    .where(currentFilter.filter(args[parameterIndex]));
        } else {
            iQuery
                    .and(currentFilter.filter(args[parameterIndex]));
        }
    }

    private List<Filter> takePossibleFilters(Class<IBaseResource> returnType) throws IllegalAccessException {
        List<Filter> filters = new LinkedList<>();

        for (Field returnTypeFiled : returnType.getFields()) {
            if (IParam.class.isAssignableFrom(returnTypeFiled.getType())) {

                IParam iParam = (IParam) returnTypeFiled.get(null);
                String paramName = iParam.getParamName();

                filters.add(new Filter(paramName, iParam));
            }
        }

        return filters;
    }

    private static class Filter {
        private String name;
        private IParam param;

        private Filter(String name, IParam param) {
            this.name = name
                    .replaceAll("_", "")
                    .replaceAll("-", "");

            this.param = param;
        }

        private ICriterion filter(Object value) {
            if (param instanceof TokenClientParam) {
                return ((TokenClientParam) param).exactly().identifier((String) value);
            }

            if (param instanceof ReferenceClientParam) {
                return ((ReferenceClientParam) param).hasId((String) value);
            }

            if (param instanceof StringClientParam) {
                return ((StringClientParam) param).matchesExactly().value((String) value);
            }

            return null;
        }
    }

    private Bundle fetchResourceByInputParameters(Class<IBaseResource> returnType, Object[] parameters,
                                                  List<String> parameterNames,
                                                  IGenericClient fhirClient) throws IllegalAccessException {

        IQuery<IBaseBundle> iQuery = fhirClient.search().forResource(returnType);

        IQuery<org.hl7.fhir.instance.model.api.IBaseBundle> filteredQuery = addFilters(parameters, returnType,
                parameterNames, iQuery);

        return filteredQuery
                .include(IBaseResource.INCLUDE_ALL)
                .returnBundle(Bundle.class)
                .execute();
    }

    private IQuery<IBaseBundle> addFilters(Object[] args, Class<IBaseResource> returnType, List<String> parametersNames,
                                           IQuery<IBaseBundle> iQuery) throws IllegalAccessException {

        if (parametersNames.isEmpty()) {
            return iQuery;
        }

        List<Filter> filters = takePossibleFilters(returnType);
        int parameterIndex = 0;
        boolean firstFilter = true;

        for (String parametersName : parametersNames) {
            Filter currentFilter = findFilterForParameter(filters, parametersName);

            if (currentFilter == null) {
                continue;
            }

            addFilter(args, iQuery, parameterIndex, firstFilter, currentFilter);

            firstFilter = false;
            parameterIndex++;
        }

        return iQuery;
    }
}
