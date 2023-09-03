package com.example.spring.data.fhir.repository.support;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.data.repository.CrudRepository;

import java.util.*;
import java.util.stream.StreamSupport;

public class SimpleFhirRepository<T extends  Resource, ID> implements CrudRepository<T, ID> {
    private final IGenericClient fhirClient;
    private final Class<T> modelType;

    public SimpleFhirRepository(IGenericClient fhirClient, Class<T> modelType) {
        this.fhirClient = fhirClient;
        this.modelType = modelType;
    }

    @Override
    public <S extends T> S save(S entity) {
        if (entity.hasId()) {
            fhirClient.update().resource(entity).execute();
        } else {
            entity.setId(fhirClient.create().resource(entity).execute().getId());
        }

        return entity;
    }

    @Override
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        entities.forEach(this::save);
        return entities;
    }

    @Override
    public Optional<T> findById(ID id) {
        IBaseResource result = fhirClient.read().resource(getModelType().getSimpleName())
                .withId((String) id).execute();
        return Optional.ofNullable((T)result);
    }

    private Class<T> getModelType() {
        return modelType;
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public Iterable<T> findAll() {
        Class<T> modelType = getModelType();
        return BundleUtil
                .toListOfResourcesOfType(
                        fhirClient.getFhirContext(),
                        fhirClient.search().forResource(modelType.getSimpleName()).returnBundle(Bundle.class).execute(),
                        modelType
                );
    }

    @Override
    public Iterable<T> findAllById(Iterable<ID> ids) {
        Class<T> modelType = getModelType();
        List<String> idsList =
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(ids.iterator(), Spliterator.ORDERED),
                                false)
                        .map(String.class::cast)
                        .toList();

        return BundleUtil
                .toListOfResourcesOfType(
                        fhirClient.getFhirContext(),
                        fhirClient.search()
                                .forResource(modelType)
                                .where(IAnyResource.RES_ID.exactly().codes(idsList))
                                .returnBundle(Bundle.class).execute(),
                        modelType
                );
    }

    @Override
    public long count() {
        return ((Collection) findAll()).size();
    }

    @Override
    public void deleteById(ID id) {
        fhirClient.delete().resourceById(getModelType().getSimpleName(), (String)id).execute();
    }


    @Override
    public void delete(T entity) {
        deleteById((ID) entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        throw new IllegalStateException("Unsupported operation");
    }
}
