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
package io.syndesis.connector.odata;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.camel.Message;
import org.apache.camel.component.connector.DefaultConnectorComponent;
import org.apache.camel.component.olingo4.Olingo4Component;
import org.apache.camel.component.olingo4.Olingo4Configuration;
import org.apache.camel.component.olingo4.internal.Olingo4Constants;
import org.apache.camel.spi.UriParam;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientObjectFactory;
import org.apache.olingo.client.api.serialization.ODataSerializer;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.data.Entity;

import static org.apache.olingo.commons.api.format.ContentType.APPLICATION_JSON;

/**
 * Abstract class for OData connectors.
 * @author dhirajsb
 */
public abstract class AbstractODataConnector extends DefaultConnectorComponent {

    @UriParam(defaultValue = "http://services.odata.org/TripPinRESTierService(SessionId)")
    private String serviceUri;

/* TODO expose standard OData headers as connector properties
    private Map<String, String> httpHeaders;
*/

/* TODO expose proxy host, port and scheme to create this property
    private HttpHost proxy;
*/

/* TODO expose these component properties somehow
    private SSLContextParameters sslContextParameters;
*/

    protected final ODataClient odataClient = ODataClientFactory.getClient();
    protected final ClientObjectFactory objFactory = odataClient.getObjectFactory();

    public AbstractODataConnector(String componentName, String componentScheme, String className) {
        super(componentName, componentScheme, className);
    }

    @Override
    public String createEndpointUri(final String scheme, final Map<String, String> options) throws URISyntaxException {

        // set serviceUri on delegate component
        Olingo4Component delegate = getCamelContext().getComponent(scheme, Olingo4Component.class);
        Olingo4Configuration configuration = new Olingo4Configuration();
        configuration.setServiceUri(this.serviceUri);
        delegate.setConfiguration(configuration);

        setAfterProducer( exchange -> {
            if (!exchange.isFailed()) {
                ClientEntity clientEntity = exchange.getIn().getBody(ClientEntity.class);
                if (clientEntity != null) {
                    // convert client entity to JSON
                    final StringWriter writer = new StringWriter();
                    final Entity entity = odataClient.getBinder().getEntity(clientEntity);
                    final ODataSerializer serializer = odataClient.getSerializer(APPLICATION_JSON);
                    serializer.write(writer, entity);
                    exchange.getIn().setBody(writer.toString());
                }
            }
            // TODO handle failure on missing resource 404
        });

        return super.createEndpointUri(scheme, options);
    }

    public String getServiceUri() {
        return serviceUri;
    }

    /**
     * Target OData service base URI, e.g. http://services.odata.org/OData/OData.svc
     */
    public void setServiceUri(String serviceUri) {
        this.serviceUri = serviceUri;
    }

    protected void ignoreResponseHeaders(Message in) {
        in.removeHeader(Olingo4Constants.PROPERTY_PREFIX + Olingo4Constants.RESPONSE_HTTP_HEADERS);
    }
}
