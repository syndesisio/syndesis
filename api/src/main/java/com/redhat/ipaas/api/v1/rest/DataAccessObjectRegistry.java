package com.redhat.ipaas.api.v1.rest;

import com.redhat.ipaas.api.v1.model.WithId;
import com.redhat.ipaas.api.v1.rest.exception.IPaasServerException;

import java.util.Map;

public interface DataAccessObjectRegistry {

    Map<Class, DataAccessObject> getDataAccessObjectMapping();

    /**
     * Finds the {@link DataAccessObject} for the specified type.
     * @param type  The class of the specified type.
     * @param <T>   The specified type.
     * @return      The {@link DataAccessObject} if found, or null no matching {@link DataAccessObject} was found.
     */
    default <T extends WithId> DataAccessObject<T> getDataAccessObject(Class<T> type) {
        return getDataAccessObjectMapping().get(type);
    }


    /**
     * Finds the {@link DataAccessObject} for the specified type.
     * @param type  The class of the specified type.
     * @param <T>   The specified type.
     * @return      The {@link DataAccessObject} or throws {@link IPaasServerException}.
     */
    default <T extends WithId> DataAccessObject<T> getDataAccessObjectRequired(Class<T> type) {
        DataAccessObject dao = getDataAccessObjectMapping().get(type);
        if (dao != null) {
            return (DataAccessObject<T>) getDataAccessObjectMapping().get(type);
        }
        throw new IllegalArgumentException("No data access object found for type: [" + type + "].");
    }

    /**
     * Regiester a {@link DataAccessObject}.
     * @param dataAccessObject  The {@link DataAccessObject} to register.
     * @param <T>               The type of the {@link DataAccessObject}.
     */
    default <T extends WithId> void registerDataAccessObject(DataAccessObject<T> dataAccessObject) {
        getDataAccessObjectMapping().put(dataAccessObject.getType(), dataAccessObject);
    }
}
