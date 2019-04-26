/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.odata.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.assertj.core.util.Arrays;

public class Storage implements ODataServerConstants {

    private static Object storageLock = new Object();

    private static Storage storage;

    private List<Entity> productList;

    public static Storage getInstance() {
        synchronized(storageLock) {
            if (storage == null) {
                storage = new Storage();
            }
        }

        return storage;
    }

    public static void dispose() {
        storage = null;
    }

    private Storage() {
        reload();
    }

    public int getCount() {
        return productList.size();
    }

    /* PUBLIC FACADE */

    public void reload() {
        productList = new ArrayList<Entity>();
        initSampleData();
    }

    public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) throws ODataApplicationException {

        // actually, this is only required if we have more than one Entity Sets
        if (edmEntitySet.getName().equals(ProductsEdmProvider.ES_PRODUCTS_NAME)) {
            return getProducts();
        }

        return null;
    }

    public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) throws ODataApplicationException {

        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // actually, this is only required if we have more than one Entity Type
        if (edmEntityType.getName().equals(ProductsEdmProvider.ET_PRODUCT_NAME)) {
            return getProduct(edmEntityType, keyParams);
        }

        return null;
    }

    public Entity createEntityData(EdmEntitySet edmEntitySet, Entity entityToCreate) {
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // actually, this is only required if we have more than one Entity Type
        if (edmEntityType.getName().equals(ProductsEdmProvider.ET_PRODUCT_NAME)) {
            return createProduct(edmEntityType, entityToCreate);
        }

        return null;
    }

    /**
     * This method is invoked for PATCH or PUT requests
     */
    public void updateEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams, Entity updateEntity,
                                 HttpMethod httpMethod)
        throws ODataApplicationException {

        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // actually, this is only required if we have more than one Entity Type
        if (edmEntityType.getName().equals(ProductsEdmProvider.ET_PRODUCT_NAME)) {
            updateProduct(edmEntityType, keyParams, updateEntity, httpMethod);
        }
    }

    public void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) throws ODataApplicationException {
        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // actually, this is only required if we have more than one Entity Type
        if (edmEntityType.getName().equals(ProductsEdmProvider.ET_PRODUCT_NAME)) {
            deleteProduct(edmEntityType, keyParams);
        }
    }

    /*  INTERNAL */
    private EntityCollection getProducts() {
        EntityCollection retEntitySet = new EntityCollection();

        for (Entity productEntity : this.productList) {
            retEntitySet.getEntities().add(productEntity);
        }

        return retEntitySet;
    }

    private Entity getProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams) throws ODataApplicationException {

        // the list of entities at runtime
        EntityCollection entitySet = getProducts();

        /*  generic approach  to find the requested entity */
        Entity requestedEntity = Util.findEntity(edmEntityType, entitySet, keyParams);

        if (requestedEntity == null) {
            // this variable is null if our data doesn't contain an entity for the requested key
            // Throw suitable exception
            throw new ODataApplicationException("Entity for requested key doesn't exist",
                                                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        return requestedEntity;
    }

    private URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + OPEN_BRACKET + String.valueOf(id) + CLOSE_BRACKET);
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    private Entity createProduct(EdmEntityType edmEntityType, Entity entity) {
        // the ID of the newly created product entity is generated automatically
        int newId = 1;
        while (productIdExists(newId)) {
            newId++;
        }

        Property idProperty = entity.getProperty(PRODUCT_ID);
        if (idProperty != null) {
            idProperty.setValue(ValueType.PRIMITIVE, Integer.valueOf(newId));
        } else {
            // as of OData v4 spec, the key property can be omitted from the POST request body
            entity.getProperties().add(new Property(null, PRODUCT_ID, ValueType.PRIMITIVE, newId));
        }
        entity.setId(createId(ES_PRODUCTS_NAME, newId));
        this.productList.add(entity);

        return entity;

    }

    private boolean productIdExists(int id) {
        for (Entity entity : this.productList) {
            Integer existingID = (Integer)entity.getProperty(PRODUCT_ID).getValue();
            if (existingID.intValue() == id) {
                return true;
            }
        }

        return false;
    }

    private void updateProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams, Entity entity, HttpMethod httpMethod)
        throws ODataApplicationException {
        Entity productEntity = getProduct(edmEntityType, keyParams);
        if (productEntity == null) {
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        // loop over all properties and replace the values with the values of the given payload
        // Note: ignoring ComplexType, as we don't have it in our odata model
        List<Property> existingProperties = productEntity.getProperties();
        for (Property existingProp : existingProperties) {
            String propName = existingProp.getName();

            // ignore the key properties, they aren't updateable
            if (isKey(edmEntityType, propName)) {
                continue;
            }

            Property updateProperty = entity.getProperty(propName);
            // the request payload might not consider ALL properties, so it can be null
            if (updateProperty == null) {
                // if a property has NOT been added to the request payload
                // depending on the HttpMethod, our behavior is different
                if (httpMethod.equals(HttpMethod.PATCH)) {
                    // as of the OData spec, in case of PATCH, the existing property is not touched
                    continue; // do nothing
                } else if (httpMethod.equals(HttpMethod.PUT)) {
                    // as of the OData spec, in case of PUT, the existing property is set to null (or to default value)
                    existingProp.setValue(existingProp.getValueType(), null);
                    continue;
                }
            }

            // change the value of the properties
            existingProp.setValue(existingProp.getValueType(), updateProperty.getValue());
        }
    }

    private void deleteProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams) throws ODataApplicationException {
        Entity productEntity = getProduct(edmEntityType, keyParams);
        if (productEntity == null) {
            throw new ODataApplicationException("Entity not found", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        this.productList.remove(productEntity);
    }

    /* HELPER */

    private boolean isKey(EdmEntityType edmEntityType, String propertyName) {
        List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
        for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
            String keyPropertyName = propRef.getName();
            if (keyPropertyName.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    private ComplexValue createSpec(String productType, String detail1, String detail2, int powerType) {
        ComplexValue complexValue = new ComplexValue();
        List<Property> complexValueValue = complexValue.getValue();
        complexValueValue.add(new Property(
                                           EdmPrimitiveTypeKind.String.getFullQualifiedName().toString(),
                                           SPEC_PRODUCT_TYPE, ValueType.PRIMITIVE, productType));
        complexValueValue.add(new Property(
                                           EdmPrimitiveTypeKind.String.getFullQualifiedName().toString(),
                                           SPEC_DETAIL_1, ValueType.PRIMITIVE, detail1));
        complexValueValue.add(new Property(
                                           EdmPrimitiveTypeKind.String.getFullQualifiedName().toString(),
                                           SPEC_DETAIL_2, ValueType.PRIMITIVE, detail2));
        complexValueValue.add(new Property(
                                           ET_POWER_TYPE_FQN.getFullQualifiedNameAsString(),
                                           SPEC_POWER_TYPE, ValueType.ENUM, powerType));
        return complexValue;
    }

    private Entity createEntity(int id, String name, String description, String[] serials, ComplexValue spec) {
        Entity e = new Entity()
            .addProperty(new Property(
                                  EdmPrimitiveTypeKind.Int32.getFullQualifiedName().toString(),
                                  PRODUCT_ID, ValueType.PRIMITIVE, id))
            .addProperty(new Property(
                                  EdmPrimitiveTypeKind.String.getFullQualifiedName().toString(),
                                  PRODUCT_NAME, ValueType.PRIMITIVE, name))
            .addProperty(new Property(
                                  EdmPrimitiveTypeKind.String.getFullQualifiedName().toString(),
                                  PRODUCT_DESCRIPTION, ValueType.PRIMITIVE, description))
            .addProperty(new Property(
                                  EdmPrimitiveTypeKind.String.getFullQualifiedName().toString(),
                                  PRODUCT_SERIALS, ValueType.COLLECTION_PRIMITIVE, Arrays.asList(serials)))
            .addProperty(new Property(
                                  ET_SPEC_FQN.toString(),
                                  PRODUCT_SPEC, ValueType.COMPLEX, spec));

        e.setId(createId(ES_PRODUCTS_NAME, id));
        return e;
    }

    private void initSampleData() {
        // add some sample product entities
        String[] e1Serials = {"ae8353484", "er5845474", "px376876"};
        ComplexValue e1Spec = createSpec("Notebook", "CPU AMD Ryzen 3 2200U", "Dual-Core Cores", 0);

        productList.add(createEntity(1,
                                 "Notebook Basic 15",
                                 "Notebook Basic, 1.7GHz - 15 XGA - 1024MB DDR2 SDRAM - 40GB",
                                 e1Serials, e1Spec));

        String[] e2Serials = {"ae867484", "er586874", "px3429876"};
        ComplexValue e2Spec = createSpec("Tablet", "Android OS", "Resolution - 1920 x 1200", 0);

        productList.add(createEntity(2,
                                     "1UMTS PDA",
                                     "Ultrafast 3G UMTS/HSDPA Pocket PC, supports GSM network",
                                     e2Serials, e2Spec));

        String[] e3Serials = {"ae949549", "er342367", "px230434"};
        ComplexValue e3Spec = createSpec("Monitor", "Diagonal Size 22", "Aspect Ratio 16:9", 1);

        productList.add(createEntity(3,
                                     "Ergo Screen",
                                     "19 Optimum Resolution 1024 x 768 @ 85Hz, resolution 1280 x 960",
                                     e3Serials, e3Spec));
    }
}
