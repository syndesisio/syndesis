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
package io.syndesis.connector.odata2.meta;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import io.syndesis.connector.odata2.ODataConstants;
import io.syndesis.connector.odata2.ODataUtil;
import io.syndesis.connector.odata2.meta.ODataMetadata.PropertyMetadata;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;
import org.apache.camel.util.ObjectHelper;
import org.apache.olingo.odata2.api.edm.Edm;
import org.apache.olingo.odata2.api.edm.EdmEntityContainer;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmNamed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ODataMetaDataExtension extends AbstractMetaDataExtension implements ODataConstants {

    private static final Logger LOG = LoggerFactory.getLogger(ODataMetaDataExtension.class);

    ODataMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        //
        // Method called twice.
        // 1) After selection of the type of OData request, ie. READ, UPDATE
        // 2) After the user has selected the resource, eg. People, SvcView, and the wizard step is complete
        //
        ODataMetadata odataMetadata = buildMetadata(parameters);
        return Optional.of(new DefaultMetaData(null, null, odataMetadata));
    }

    private static ODataMetadata buildMetadata(Map<String, Object> parameters) {
        ODataMetadata odataMetadata = new ODataMetadata();

        String serviceUrl = ConnectorOptions.extractOption(parameters, SERVICE_URI);
        if (ObjectHelper.isEmpty(serviceUrl)) {
            return odataMetadata;
        }
        LOG.debug("Retrieving metadata for connection to odata-v2 with service url {}", serviceUrl);

        try {
            Edm edm = ODataUtil.readEdm(serviceUrl, parameters);
            //
            // Will happen prior to entering data for the resource path
            // and after the step has been completed.
            //
            extractEdmNames(odataMetadata, edm);

            //
            // Will only happen after the step has been completed when
            // the resource path, inc. the resourcePath, has been populated
            //
            String resourcePath = ConnectorOptions.extractOption(parameters, RESOURCE_PATH);
            if (ObjectHelper.isNotEmpty(resourcePath)) {
                extractEdmMetadata(odataMetadata, edm, resourcePath);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to get resource metadata from " + serviceUrl + ".", e);
        }

        return odataMetadata;
    }

    private static void extractEdmMetadata(ODataMetadata odataMetadata, Edm edm, String resourcePath) throws EdmException {
        if (ObjectHelper.isEmpty(resourcePath)) {
            LOG.warn("No method name with which to query OData service.");
            return;
        }

        EdmEntityContainer entityContainer = edm.getDefaultEntityContainer();
        EdmEntitySet entitySet = entityContainer.getEntitySet(resourcePath);
        if (ObjectHelper.isEmpty(entitySet)) {
            LOG.warn("No entity set associated with the selected api name: {}.", resourcePath);
            return;
        }

        EdmTypeConverter visitor = new EdmTypeConverter();
        EdmEntityType entityType = entitySet.getEntityType();
        Set<PropertyMetadata> properties = visitor.visit(entityType);
        odataMetadata.setEntityProperties(properties);
    }

    private static void extractEdmNames(ODataMetadata odataMetadata, Edm edm) throws EdmException {
        EdmEntityContainer container = edm.getDefaultEntityContainer();
        Set<EdmNamed> namedList = new HashSet<>(container.getEntitySets());

//
// TODO
// Consider whether we need these in the future.
//
//            namedList.addAll(container.getFunctionImports());
//            namedList.addAll(container.getSingletons());
//            namedList.addAll(container.getActionImports());
//
        Set<String> names = new TreeSet<>();
        for (EdmNamed entity : namedList) {
            names.add(entity.getName());
        }

        odataMetadata.setEntityNames(names);
    }
}
