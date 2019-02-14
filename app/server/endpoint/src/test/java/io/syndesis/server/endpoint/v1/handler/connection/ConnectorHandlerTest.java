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

import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Test;
import org.springframework.context.ApplicationContext;

import io.syndesis.server.credential.Credentials;
import io.syndesis.server.dao.file.FileDataManager;
import io.syndesis.server.dao.file.IconDao;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.server.endpoint.v1.state.ClientSideState;
import io.syndesis.server.verifier.Verifier;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class ConnectorHandlerTest {

    private static final Credentials NO_CREDENTIALS = null;

    private static final EncryptionComponent NO_ENCRYPTION_COMPONENT = null;

    private static final Inspectors NO_INSPECTORS = null;

    private static final ClientSideState NO_STATE = null;

    private static final Verifier NO_VERIFIER = null;

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);

    private final DataManager dataManager = mock(DataManager.class);

    private final IconDao NO_ICON_DAO = null;

    private final FileDataManager NO_EXTENSION_DATA_MANAGER = null;

    private final io.syndesis.server.endpoint.v1.handler.connection.ConnectorHandler handler = new io.syndesis.server.endpoint.v1.handler.connection.ConnectorHandler(dataManager, NO_VERIFIER, NO_CREDENTIALS, NO_INSPECTORS, NO_STATE,
                                                                                                                                                                      NO_ENCRYPTION_COMPONENT, applicationContext, NO_ICON_DAO, NO_EXTENSION_DATA_MANAGER);

    @Test
    public void connectorIconShouldHaveCorrectContentType() throws IOException {
        try (MockWebServer mockWebServer = new MockWebServer(); final Buffer resultBuffer = new Buffer()) {
            mockWebServer.start();

            resultBuffer.writeAll(Okio.source(getClass().getResourceAsStream("test-image.png")));

            mockWebServer.enqueue(new MockResponse().setHeader(CONTENT_TYPE, "image/png").setBody(resultBuffer));

            final Connector connector = new Connector.Builder().id("connector-id").icon(mockWebServer.url("/u/23079786").toString())
                .build();
            when(dataManager.fetch(Connector.class, "connector-id")).thenReturn(connector);
            when(dataManager.fetchAll(Integration.class)).thenReturn(ListResult.of(Collections.emptyList()));

            final Response response = handler.getConnectorIcon("connector-id").get();

            assertThat(response.getStatusInfo()).as("Wrong status code").isEqualTo(Response.Status.OK);
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
                        final Dimension dimensions = new Dimension(reader.getWidth(0), reader.getHeight(0));
                        assertThat(dimensions.getHeight()).as("Wrong image height").isEqualTo(106d);
                        assertThat(dimensions.getWidth()).as("Wrong image width").isEqualTo(106d);
                    } finally {
                        reader.dispose();
                    }
                }
            }
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
    public void shouldDeleteConnectionsWhenDeletingConnector() {
        when(dataManager.fetchIdsByPropertyValue(Connection.class, "connectorId", "connector-id"))
            .thenReturn(new HashSet<>(Arrays.asList("connection1", "connection2")));
        handler.delete("connector-id");

        verify(dataManager).delete(Connector.class, "connector-id");
        verify(dataManager).delete(Connection.class, "connection1");
        verify(dataManager).delete(Connection.class, "connection2");
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
