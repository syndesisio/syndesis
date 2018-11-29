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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.EdmMetadataRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.http.HttpClientFactory;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmNamed;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.syndesis.connector.odata.ODataConstants;
import io.syndesis.connector.odata.ODataUtil;

public class ODataMetaDataExtension extends AbstractMetaDataExtension implements ODataConstants {

    private static final Logger LOG = LoggerFactory.getLogger(ODataMetaDataExtension.class);

    ODataMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        String serviceUrl = (String) parameters.get(SERVICE_URI);
        if (serviceUrl == null) {
            return Optional.empty();
        }

        LOG.debug("Retrieving metadata for connection to odata with service url {}", serviceUrl);
        try {
            ODataClient client = ODataClientFactory.getClient();
            HttpClientFactory factory = ODataUtil.newHttpFactory(parameters);
            client.getConfiguration().setHttpClientFactory(factory);

            EdmMetadataRequest request = client.getRetrieveRequestFactory().getMetadataRequest(serviceUrl);
            ODataRetrieveResponse<Edm> response = request.execute();

            if(response.getStatusCode() != 200) {
                throw new IllegalStateException("Metatdata response failure. Return code: " + response.getStatusCode());
            }

            Edm edm = response.getBody();

            Set<EdmNamed> namedList = new HashSet<>();
            List<EdmSchema> schemas = edm.getSchemas();
            for (EdmSchema schema : schemas) {
                EdmEntityContainer container = schema.getEntityContainer();
                if (container == null) {
                    continue;
                }

                namedList.addAll(container.getEntitySets());
                namedList.addAll(container.getFunctionImports());
                namedList.addAll(container.getSingletons());
                namedList.addAll(container.getActionImports());
            }

            Set<String> names = new TreeSet<>();
            namedList.stream().distinct().forEach((entity) -> {
                names.add(entity.getName());
            });

            return Optional
                        .of(MetaDataBuilder.on(getCamelContext()).withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                                .withAttribute(MetaData.JAVA_TYPE, String.class).withPayload(names).build());

            } catch (Exception e) {
                throw new IllegalStateException(
                    "Failed to get resource metadata from " + serviceUrl + ".", e);
            }
    }
}
