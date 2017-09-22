/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.verifier.v1.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import io.syndesis.connector.ColumnMode;
import io.syndesis.connector.StoredProcedureColumn;
import io.syndesis.connector.StoredProcedureMetadata;

@Component("sql-stored-adapter")
public final class SqlStoredMetadataAdapter implements MetadataAdapter<JsonSchema> {

    final static String PROCEDURE_NAME     = "procedureName";
    final static String PROCEDURE_TEMPLATE = "template";

    @Override
    public SyndesisMetadata<JsonSchema> adapt(final Map<String, Object> properties, final MetaData metadata) {

        final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();

        if (isPresentAndNonNull(properties, PROCEDURE_NAME)) {
            // fetch metadata for the named procedure
            List<PropertyPair> ppList = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Map<String, StoredProcedureMetadata> procedureMap = (Map<String, StoredProcedureMetadata>) metadata.getPayload();
            StoredProcedureMetadata storedProcedure = procedureMap.get(properties.get(PROCEDURE_NAME));
            ppList.add(new PropertyPair(storedProcedure.getTemplate(), PROCEDURE_TEMPLATE));
            enrichedProperties.put(PROCEDURE_TEMPLATE,ppList);

            // build the input and output schemas
            JSONBeanSchemaBuilder builderIn = new JSONBeanSchemaBuilder();
            JSONBeanSchemaBuilder builderOut = new JSONBeanSchemaBuilder();
            if (storedProcedure.getColumnList()!=null && !storedProcedure.getColumnList().isEmpty()) {
                for (StoredProcedureColumn column : storedProcedure.getColumnList()) {
                      if (column.getMode().equals(ColumnMode.IN) || column.getMode().equals(ColumnMode.INOUT)) {
                          builderIn.addField(column.getName(), column.getJdbcType());
                      }
                      if (column.getMode().equals(ColumnMode.OUT) || column.getMode().equals(ColumnMode.INOUT)) {
                          builderOut.addField(column.getName(), column.getJdbcType());
                      }
                }
            }
            return new SyndesisMetadata<>(enrichedProperties, builderIn.build(), builderOut.build());
        } else {
            // return list of all stored procedures in the database
            List<PropertyPair> ppList = new ArrayList<>();
            @SuppressWarnings("unchecked")
            Map<String, StoredProcedureMetadata> procedureMap = (Map<String, StoredProcedureMetadata>) metadata.getPayload();
            for (String storedProcedureName : procedureMap.keySet()) {
                PropertyPair pp = new PropertyPair(storedProcedureName, storedProcedureName);
                ppList.add(pp);
            }
            enrichedProperties.put(PROCEDURE_NAME,ppList);
            return new SyndesisMetadata<>(enrichedProperties, null, null);
        }
    }

    static boolean isPresent(final Map<String, Object> properties, final String property) {
        return properties != null && properties.containsKey(property);
    }

    static boolean isPresentAndNonNull(final Map<String, Object> properties, final String property) {
        return isPresent(properties, property) && properties.get(property) != null;
    }

}
