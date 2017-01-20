package com.redhat.ipaas.api.v1.rest;

import com.redhat.ipaas.api.v1.model.ListResult;
import com.redhat.ipaas.api.v1.model.WithId;

public interface DataAccessObject<T extends WithId> {

    /**
     * @return The Type of entity it supports.
     */
    Class<T> getType();

    /**
     * Fetches an entity by id.
     * @param id    The id.
     * @return      The matching entity.
     */
    T fetch(String id);

    /**
     * Fetches a {@link ListResult} containing all entities.
     * @return  The {@link ListResult}.
     */
    ListResult<T> fetchAll();

    /**
     * Creates a new entity.
     * @param entity    The entity.
     * @return          The created entity.
     */
    T create(T entity);


    /**
     * Updates the specified entity.
     * @param entity    The entity.
     * @return          The previous value or null.
     */
    T update(T entity);


    /**
     * Delete the specified entity.
     * @param entity    The entity.
     * @return          True on successful deletion, false otherwise.
     */
    boolean delete(WithId<T> entity);


    /**
     * Delete the entity with the specified id.
     * @param id        The id of the entity.
     * @return          True on successful deletion, false otherwise.
     */
    boolean delete(String id);

}
