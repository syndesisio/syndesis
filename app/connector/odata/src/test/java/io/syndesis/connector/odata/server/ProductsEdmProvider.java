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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;

/**
 * Taken from the tutorial:
 * https://olingo.apache.org/doc/odata4/tutorials/read/tutorial_read.html
 */
public class ProductsEdmProvider extends CsdlAbstractEdmProvider implements ODataServerConstants {

    @Override
    public CsdlEnumType getEnumType(FullQualifiedName enumTypeName) {
        if (enumTypeName.equals(ET_POWER_TYPE_FQN)) {
            CsdlEnumMember twoForty = new CsdlEnumMember();
            twoForty.setName("240V");
            twoForty.setValue("0");

            CsdlEnumMember oneTen = new CsdlEnumMember();
            oneTen.setName("110V");
            oneTen.setValue("1");

            CsdlEnumType powerTypeEnum = new CsdlEnumType();
            powerTypeEnum.setName(ET_POWER_TYPE_NAME);
            powerTypeEnum.setMembers(Arrays.asList(twoForty, oneTen));
            return powerTypeEnum;
        }

        return null;
    }

    @Override
    public CsdlComplexType getComplexType(FullQualifiedName complexTypeName) {
        if (complexTypeName.equals(ET_SPEC_FQN)) {
            CsdlProperty type = new CsdlProperty().setName(SPEC_PRODUCT_TYPE)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty detail1 = new CsdlProperty().setName(SPEC_DETAIL_1)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty detail2 = new CsdlProperty().setName(SPEC_DETAIL_2)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty powerType = new CsdlProperty().setName(SPEC_POWER_TYPE)
                .setType(ET_POWER_TYPE_FQN);
            CsdlComplexType complexType = new CsdlComplexType();
            complexType.setName(ET_SPEC_NAME);
            complexType.setProperties(Arrays.asList(type, detail1, detail2, powerType));
            return complexType;
        }

        return null;
    }

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {
        // this method is called for one of the EntityTypes that are configured in the Schema
        if (entityTypeName.equals(ET_PRODUCT_FQN)) {

            //create EntityType properties
            CsdlProperty id = new CsdlProperty().setName(PRODUCT_ID).setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName(PRODUCT_NAME).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty description = new CsdlProperty().setName(PRODUCT_DESCRIPTION).setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty serialNums = new CsdlProperty()
                .setName(PRODUCT_SERIALS)
                .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName())
                .setCollection(true);
            CsdlProperty specification = new CsdlProperty()
                .setName(PRODUCT_SPEC)
                .setType(ET_SPEC_FQN);

            // create CsdlPropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName(PRODUCT_ID);

            // configure EntityType
            CsdlEntityType entityType = new CsdlEntityType();
            entityType.setName(ET_PRODUCT_NAME);
            entityType.setProperties(Arrays.asList(id, name, description, serialNums, specification));
            entityType.setKey(Collections.singletonList(propertyRef));

            return entityType;
        }

        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        if (entityContainer.equals(CONTAINER)) {
            if (entitySetName.equals(ES_PRODUCTS_NAME)) {
                CsdlEntitySet entitySet = new CsdlEntitySet();
                entitySet.setName(ES_PRODUCTS_NAME);
                entitySet.setType(ET_PRODUCT_FQN);

                return entitySet;
            }
        }

        return null;
    }

    @Override
    public CsdlEntityContainer getEntityContainer() {

        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.add(getEntitySet(CONTAINER, ES_PRODUCTS_NAME));

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);

        return entityContainer;
    }

    @Override
    public List<CsdlSchema> getSchemas() {

        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        List<CsdlEnumType> enumTypes = new ArrayList<>();
        enumTypes.add(getEnumType(ET_POWER_TYPE_FQN));
        schema.setEnumTypes(enumTypes);

        List<CsdlComplexType> complexTypes = new ArrayList<>();
        complexTypes.add(getComplexType(ET_SPEC_FQN));
        schema.setComplexTypes(complexTypes);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<>();
        entityTypes.add(getEntityType(ET_PRODUCT_FQN));
        schema.setEntityTypes(entityTypes);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        schemas.add(schema);

        return schemas;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

        // This method is invoked when displaying the Service Document at e.g. http://localhost:8080/DemoService/DemoService.svc
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }

        return null;
    }

}
