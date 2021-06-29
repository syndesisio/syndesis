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
package io.syndesis.server.endpoint.v1.handler.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.ConfigurationProperty.PropertyValue;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.DynamicConnectionPropertiesMetadata;
import io.syndesis.common.model.connection.WithDynamicProperties;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.file.FileDataManager;
import io.syndesis.server.dao.file.IconDao;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.server.verifier.MetadataConfigurationProperties;
import io.syndesis.server.verifier.Verifier;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;

public class ConnectorHandlerTest {

    private static final Credentials NO_CREDENTIALS = null;

    private static final EncryptionComponent NO_ENCRYPTION_COMPONENT = null;

    private static final Inspectors NO_INSPECTORS = null;

    private static final ClientSideState NO_STATE = null;

    private static final Verifier NO_VERIFIER = null;

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private final DataManager dataManager = mock(DataManager.class);

    private static final IconDao NO_ICON_DAO = null;

    private static final FileDataManager NO_EXTENSION_DATA_MANAGER = null;

    private static final MetadataConfigurationProperties NO_METADATA_CONFIGURATION_PROPERTIES = null;

    private final ConnectorHandler handler =
        new ConnectorHandler(dataManager, NO_VERIFIER, NO_CREDENTIALS, NO_INSPECTORS, NO_STATE,
            NO_ENCRYPTION_COMPONENT, applicationContext, NO_ICON_DAO, NO_EXTENSION_DATA_MANAGER,
            NO_METADATA_CONFIGURATION_PROPERTIES);

    @Test
    public void connectorIconShouldHaveCorrectContentType() throws IOException {
        final WireMockServer wiremock = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .useChunkedTransferEncoding(ChunkedEncodingPolicy.NEVER));

        wiremock.start();

        try {
            byte[] png = IOUtils.toByteArray(ConnectorHandlerTest.class.getResource("test-image.png"));
            wiremock.stubFor(get("/u/23079786")
                .willReturn(aResponse()
                    .withHeader(CONTENT_TYPE, "image/png")
                    .withHeader(CONTENT_LENGTH, String.valueOf(png.length))
                    .withBody(png)
                )
            );

            final Connector connector = new Connector.Builder()
                .id("connector-id")
                .icon("http://localhost:" + wiremock.port() + "/u/23079786")
                .build();
            when(dataManager.fetch(Connector.class, "connector-id")).thenReturn(connector);
            when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(Collections.emptyList()));

            final Response response = handler.getConnectorIcon("connector-id").get();

            assertThat(response.getStatusInfo().getStatusCode()).as("Wrong status code").isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(response.getHeaderString(CONTENT_TYPE)).as("Wrong content type").isEqualTo("image/png");
            assertThat(response.getHeaderString(CONTENT_LENGTH)).as("Wrong content length").isEqualTo("2018");

            final StreamingOutput so = (StreamingOutput) response.getEntity();
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (BufferedSink sink = Okio.buffer(Okio.sink(bos)); BufferedSource source = new Buffer();
                 ImageInputStream iis = ImageIO.createImageInputStream(source.inputStream());) {
                so.write(sink.outputStream());
                source.readAll(sink);
                final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (readers.hasNext()) {
                    final ImageReader reader = readers.next();
                    try {
                        reader.setInput(iis);
                        assertThat(reader.getHeight(0)).as("Wrong image height").isEqualTo(106d);
                        assertThat(reader.getWidth(0)).as("Wrong image width").isEqualTo(106d);
                    } finally {
                        reader.dispose();
                    }
                }
            }
        } finally {
            wiremock.stop();
        }
    }

    @Test
    public void shouldAugmentWithConnectorUsage() {
        final Connector connector1 = newConnector("1");
        final Connector connector2 = newConnector("2");
        final Connector connector3 = newConnector("3");

        final Step step1a = new Step.Builder().action(newActionBy(connector1)).build();
        final Step step1b = new Step.Builder().action(newActionBy(connector1)).build();
        final Step step2 = new Step.Builder().action(newActionBy(connector2)).build();

        final Integration deployment1 = newIntegration(Arrays.asList(step1a, step1b));
        final Integration deployment2 = newIntegration(Collections.singletonList(step2));
        final Integration deployment3 = newIntegration(Collections.singletonList(step2));

        when(dataManager.fetchAll(Integration.class))
            .thenReturn(new ListResult.Builder<Integration>().addItems(deployment1, deployment2, deployment3).build());

        final List<Connector> augmented = handler.augmentedWithUsage(Arrays.asList(connector1, connector2, connector3));

        assertThat(augmented).contains(usedConnector(connector1, 1), usedConnector(connector2, 2), usedConnector(connector3, 0));
    }

    @Test
    public void shouldListApiConnectors() {
        final Connector connector1 = new Connector.Builder().id("1").connectorGroupId("1").build();
        final Connector connector2 = new Connector.Builder().id("2").connectorGroupId("2").build();

        final List<Connector> connectors = Arrays.asList(
            connector1,
            connector2,
            new Connector.Builder().id("3").build(),
            new Connector.Builder().id("4").connectorGroupId("4").build());

        // verify predicates in listApiConnectors()
        when(dataManager.fetchAll(eq(Connector.class), any()))
            .then(a -> {
                ListResult<Connector> result = ListResult.of(connectors);
                final Object[] operators = a.getArguments();
                for (int i = 1; i < operators.length; i++) {
                    @SuppressWarnings("unchecked")
                    Function<ListResult<Connector>, ListResult<Connector>> operator = (Function<ListResult<Connector>
                        , ListResult<Connector>>) operators[i];
                    result = operator.apply(result);
                }
                return result;
            });

        // no integrations, 0 usage for all connectors
        when(dataManager.fetchAll(Integration.class))
            .thenReturn(ListResult.of(Collections.emptyList()));

        final ListResult<Connector> result = handler.listApiConnectors(Arrays.asList("1", "2"), 1, 50);

        assertThat(result).size().isEqualTo(2);
        assertThat(result).contains(connector1, connector2);
    }

    @Test
    public void shouldDeleteConnectionsWhenDeletingConnector() {
        when(dataManager.fetchIdsByPropertyValue(Connection.class, "connectorId", "connector-id"))
            .thenReturn(new HashSet<>(Arrays.asList("connection1", "connection2")));
        handler.delete("connector-id");

        verify(dataManager).delete(Connector.class, "connector-id");
        verify(dataManager).delete(Connection.class, "connection1");
        verify(dataManager).delete(Connection.class, "connection2");
    }

    @Test
    public void shouldNotFailToEnrichDynamicPropertiesWithNoResponse() {
        final ConnectorPropertiesHandler connectorPropertiesHandler = mock(ConnectorPropertiesHandler.class);

        final ConnectorHandler connectorHandler = new ConnectorHandler(dataManager, NO_VERIFIER, NO_CREDENTIALS, NO_INSPECTORS, NO_STATE,
            NO_ENCRYPTION_COMPONENT, applicationContext, NO_ICON_DAO, NO_EXTENSION_DATA_MANAGER,
            NO_METADATA_CONFIGURATION_PROPERTIES) {
            @Override
            public ConnectorPropertiesHandler properties(@NotNull String connectorId) {
                return connectorPropertiesHandler;
            }
        };

        final DynamicConnectionPropertiesMetadata metaResponse = DynamicConnectionPropertiesMetadata.NOTHING;

        when(connectorPropertiesHandler.dynamicConnectionProperties("connectorId")).thenReturn(metaResponse);

        final Connector connector = new Connector.Builder()
            .id("connectorId")
            .build();
        final Connector withDynamicProperties = connectorHandler.enrichConnectorWithDynamicProperties(connector);

        final Connector expected = new Connector.Builder()
            .id("connectorId")
            .build();

        assertThat(withDynamicProperties).isEqualTo(expected);
    }

    @Test
    public void shouldEnrichDynamicPropertiesWithResponseFromMeta() {
        final ConnectorPropertiesHandler connectorPropertiesHandler = mock(ConnectorPropertiesHandler.class);

        final ConnectorHandler connectorHandler = new ConnectorHandler(dataManager, NO_VERIFIER, NO_CREDENTIALS, NO_INSPECTORS, NO_STATE,
            NO_ENCRYPTION_COMPONENT, applicationContext, NO_ICON_DAO, NO_EXTENSION_DATA_MANAGER,
            NO_METADATA_CONFIGURATION_PROPERTIES) {
            @Override
            public ConnectorPropertiesHandler properties(@NotNull String connectorId) {
                return connectorPropertiesHandler;
            }
        };

        final DynamicConnectionPropertiesMetadata metaResponse = new DynamicConnectionPropertiesMetadata.Builder()
            .putProperty("property", Arrays.asList(
                new WithDynamicProperties.ActionPropertySuggestion.Builder().displayValue("Value 1").value("value1").build(),
                new WithDynamicProperties.ActionPropertySuggestion.Builder().displayValue("Value 2").value("value2").build()
                )
            ).build();
        when(connectorPropertiesHandler.dynamicConnectionProperties("connectorId")).thenReturn(metaResponse);

        final Connector connector = new Connector.Builder()
            .id("connectorId")
            .putProperty("property", new ConfigurationProperty.Builder().build())
            .build();
        final Connector withDynamicProperties = connectorHandler.enrichConnectorWithDynamicProperties(connector);

        final Connector expected = new Connector.Builder()
            .id("connectorId")
            .putProperty("property", new ConfigurationProperty.Builder()
                .addEnum(PropertyValue.Builder.of("value1", "Value 1"), PropertyValue.Builder.of("value2", "Value 2"))
                .build())
            .build();

        assertThat(withDynamicProperties).isEqualTo(expected);
    }

    @Test
    public void shouldEnrichDynamicPropertiesKeepingStaticPropertiesWithResponseFromMeta() {
        final ConnectorPropertiesHandler connectorPropertiesHandler = mock(ConnectorPropertiesHandler.class);

        final ConnectorHandler connectorHandler = new ConnectorHandler(dataManager, NO_VERIFIER, NO_CREDENTIALS, NO_INSPECTORS, NO_STATE,
            NO_ENCRYPTION_COMPONENT, applicationContext, NO_ICON_DAO, NO_EXTENSION_DATA_MANAGER,
            NO_METADATA_CONFIGURATION_PROPERTIES) {
            @Override
            public ConnectorPropertiesHandler properties(@NotNull String connectorId) {
                return connectorPropertiesHandler;
            }
        };

        final DynamicConnectionPropertiesMetadata metaResponse = new DynamicConnectionPropertiesMetadata.Builder()
            .putProperty("dynamicProperty", Arrays.asList(
                new WithDynamicProperties.ActionPropertySuggestion.Builder().displayValue("Value 1").value("value1").build(),
                new WithDynamicProperties.ActionPropertySuggestion.Builder().displayValue("Value 2").value("value2").build()
                )
            ).build();
        when(connectorPropertiesHandler.dynamicConnectionProperties("connectorId")).thenReturn(metaResponse);

        final Connector connector = new Connector.Builder()
            .id("connectorId")
            .putProperty("staticProperty", new ConfigurationProperty.Builder().build())
            .putProperty("dynamicProperty", new ConfigurationProperty.Builder().build())
            .build();
        final Connector withDynamicProperties = connectorHandler.enrichConnectorWithDynamicProperties(connector);

        final Connector expected = new Connector.Builder()
            .id("connectorId")
            .putProperty("staticProperty", new ConfigurationProperty.Builder().build())
            .putProperty("dynamicProperty", new ConfigurationProperty.Builder()
                .addEnum(PropertyValue.Builder.of("value1", "Value 1"), PropertyValue.Builder.of("value2", "Value 2"))
                .build())
            .build();

        assertThat(withDynamicProperties).isEqualTo(expected);
    }

    @Test
    public void shouldNotFailToEnrichDynamicPropertiesWithErrorResponse() {
        final ConnectorPropertiesHandler connectorPropertiesHandler = mock(ConnectorPropertiesHandler.class);

        final ConnectorHandler connectorHandler = new ConnectorHandler(dataManager, NO_VERIFIER, NO_CREDENTIALS, NO_INSPECTORS, NO_STATE,
            NO_ENCRYPTION_COMPONENT, applicationContext, NO_ICON_DAO, NO_EXTENSION_DATA_MANAGER,
            NO_METADATA_CONFIGURATION_PROPERTIES) {
            @Override
            public ConnectorPropertiesHandler properties(@NotNull String connectorId) {
                return connectorPropertiesHandler;
            }
        };

        //It would never provide an error, as in such case circuit breaker provide the fallback implementation
        final DynamicConnectionPropertiesMetadata metaResponse = DynamicConnectionPropertiesMetadata.NOTHING;
        when(connectorPropertiesHandler.dynamicConnectionProperties("connectorId")).thenReturn(metaResponse);

        final Connector connector = new Connector.Builder()
            .id("connectorId")
            .build();
        final Connector withDynamicProperties = connectorHandler.enrichConnectorWithDynamicProperties(connector);

        final Connector expected = new Connector.Builder()
            .id("connectorId")
            .build();

        assertThat(withDynamicProperties).isEqualTo(expected);
    }

    private static ConnectorAction newActionBy(final Connector connector) {
        return new ConnectorAction.Builder()//
            .descriptor(//
                new ConnectorDescriptor.Builder()//
                    .connectorId(connector.getId().get())//
                    .build())//
            .build();
    }

    private static Connector newConnector(final String id) {
        return new Connector.Builder().id(id).build();
    }

    private static Connector usedConnector(final Connector connector, final int usage) {
        return new Connector.Builder().createFrom(connector).uses(usage).build();
    }

    private static Integration newIntegration(List<Step> steps) {
        return new Integration.Builder()
            .id("test")
            .name("test")
            .addFlow(new Flow.Builder().steps(steps).build())
            .build();
    }
}
