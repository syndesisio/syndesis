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
package io.syndesis.connector.odata.meta;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.DefaultMetaData;
import org.apache.camel.util.ObjectHelper;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNamed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.odata.ODataConstants;
import io.syndesis.connector.odata.ODataUtil;
import io.syndesis.connector.odata.meta.ODataMetadata.PropertyMetadata;
import io.syndesis.connector.support.util.ConnectorOptions;

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

    private ODataMetadata buildMetadata(Map<String, Object> parameters) {
        ODataMetadata odataMetadata = new ODataMetadata();

        String serviceUrl = ConnectorOptions.extractOption(parameters, SERVICE_URI);
        if (ObjectHelper.isEmpty(serviceUrl)) {
            return odataMetadata;
        }
        LOG.debug("Retrieving metadata for connection to odata with service url {}", serviceUrl);

        try {
            Edm edm = requestEdm(parameters, serviceUrl);
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

    private void extractEdmMetadata(ODataMetadata odataMetadata, Edm edm, String resourcePath) {
        if (ObjectHelper.isEmpty(resourcePath)) {
            LOG.warn("No method name with which to query OData service.");
            return;
        }

        EdmEntityContainer entityContainer = edm.getEntityContainer();
        EdmEntitySet entitySet = entityContainer.getEntitySet(resourcePath);
        if (ObjectHelper.isEmpty(entitySet)) {
            LOG.warn("No entity set associated with the selected api name: {}.", resourcePath);
            return;
        }

        EdmTypeConvertor visitor = new EdmTypeConvertor();
        EdmEntityType entityType = entitySet.getEntityType();
        Set<PropertyMetadata> properties = visitor.visit(entityType);
        odataMetadata.setEntityProperties(properties);
    }

    private void extractEdmNames(ODataMetadata odataMetadata, Edm edm) {
        Set<EdmNamed> namedList = new HashSet<>();
        EdmEntityContainer container = edm.getEntityContainer();
        namedList.addAll(container.getEntitySets());

//
// TODO
// Consider whether we need these in the future.
//
//            namedList.addAll(container.getFunctionImports());
//            namedList.addAll(container.getSingletons());
//            namedList.addAll(container.getActionImports());
//
        Set<String> names = new TreeSet<>();
        namedList.stream().distinct().forEach((entity) -> {
            names.add(entity.getName());
        });

        odataMetadata.setEntityNames(names);
    }

    private Edm requestEdm(Map<String, Object> parameters, String serviceUrl) {
        ODataClient client = ODataClientFactory.getClient();
        HttpClientFactory factory = ODataUtil.newHttpFactory(parameters);
        client.getConfiguration().setHttpClientFactory(factory);

        EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl);
        ODataRetrieveResponse<Edm> response = request.execute();

        if (response.getStatusCode() != 200) {
            throw new IllegalStateException("Metatdata response failure. Return code: " + response.getStatusCode());
        }

        return response.getBody();
    }
}
