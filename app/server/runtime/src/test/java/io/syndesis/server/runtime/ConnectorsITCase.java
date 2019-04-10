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
package io.syndesis.server.runtime;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.server.verifier.AlwaysOkVerifier;
import io.syndesis.server.verifier.Verifier;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorsITCase extends BaseITCase {

    @Autowired
    private Verifier verifier;

    @Test
    public void connectorListWithValidToken() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<ListResult<Connector>> type = (Class) ListResult.class;
        final ResponseEntity<ListResult<Connector>> response = get("/api/v1/connectors", type);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        final ListResult<Connector> result = response.getBody();
        assertThat(result.getTotalCount()).as("connectors total").isGreaterThan(2);
        assertThat(result.getItems().size()).as("connector list").isGreaterThan(2);
    }

    @Test
    public void connectorsGetTest() {
        final ResponseEntity<Connector> response = get("/api/v1/connectors/twitter", Connector.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        final Connector connector = response.getBody();
        assertThat(connector).isNotNull();
        assertThat(connector.getId()).contains("twitter");
    }

    @Test
    public void connectorsListForbidden() {
        final ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1/connectors", JsonNode.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void filterConnectorList() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Class<ListResult<Connector>> type = (Class) ListResult.class;
        final ResponseEntity<ListResult<Connector>> response = get("/api/v1/connectors?query=name=Salesforce", type);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        final ListResult<Connector> result = response.getBody();
        assertThat(result.getTotalCount()).as("connectors total").isEqualTo(1);
        assertThat(result.getItems().size()).as("connector list").isEqualTo(1);
    }

    @Test
    public void testUpdateIcon() throws IOException {
        final LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("icon", new InputStreamResource(getClass().getResourceAsStream("test-image.png")));

        final ResponseEntity<Connector> updated = post("/api/v1/connectors/twitter/icon",
            multipartData, Connector.class, tokenRule.validToken(), HttpStatus.OK,
            multipartHeaders());

        assertThat(updated.getBody().getId()).isPresent();
        assertThat(updated.getBody().getIcon()).isNotBlank().startsWith("db:");

        final ResponseEntity<ByteArrayResource> got = get("/api/v1/connectors/twitter/icon", ByteArrayResource.class);
        assertThat(got.getHeaders().getFirst("Content-Type")).isEqualTo("image/png");

        try (ImageInputStream iis = ImageIO.createImageInputStream(got.getBody().getInputStream());) {
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

    @Test
    @Ignore
    public void verifyBadTwitterConnectionSettings() throws IOException {

        // AlwaysOkVerifier never fails.. do don't try this test case, if that's
        // whats being used.
        Assume.assumeFalse(verifier instanceof AlwaysOkVerifier);

        final Properties credentials = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/valid-twitter-keys.properties")) {
            credentials.load(is);
        }
        credentials.put("accessTokenSecret", "badtoken");

        final ResponseEntity<Verifier.Result> response = post("/api/v1/connectors/twitter/verifier/connectivity", credentials,
            Verifier.Result.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        final Verifier.Result result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Verifier.Result.Status.ERROR);
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    public void testUpdateConnectorAndIconViaMultipart() throws IOException {
        final Connector initialConnector = dataManager.fetch(Connector.class, "twitter");

        assertThat(initialConnector.getIcon()).isNotBlank().doesNotStartWith("db:");

        final Connector connectorWithNewdescription = new Connector.Builder().createFrom(initialConnector)
            .description("Updated!").build();

        final LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("connector", connectorWithNewdescription);
        multipartData.add("icon", new InputStreamResource(getClass().getResourceAsStream("test-image.png")));

        final ResponseEntity<Void> updated = put("/api/v1/connectors/twitter",
            multipartData, Void.class, tokenRule.validToken(), HttpStatus.NO_CONTENT,
            multipartHeaders());

        final ResponseEntity<Connector> updatedConnector = get("/api/v1/connectors/twitter", Connector.class);

        assertThat(updatedConnector.getBody().getId()).isPresent();
        assertThat(updatedConnector.getBody().getIcon()).isNotBlank().startsWith("db:");
        assertThat(updatedConnector.getBody().getDescription()).isNotBlank().isEqualTo("Updated!");

        final ResponseEntity<ByteArrayResource> got = get("/api/v1/connectors/twitter/icon", ByteArrayResource.class);
        assertThat(got.getHeaders().getFirst("Content-Type")).isEqualTo("image/png");

        try (ImageInputStream iis = ImageIO.createImageInputStream(got.getBody().getInputStream());) {
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

    @Test
    public void testUpdateConnectorWithoutIconViaMultipart() throws IOException {
        final Connector initialConnector = dataManager.fetch(Connector.class, "twitter");

        final Connector connectorWithNewdescription = new Connector.Builder().createFrom(initialConnector)
            .description("Updated!").build();

        final LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("connector", connectorWithNewdescription);

        final ResponseEntity<Void> updated = put("/api/v1/connectors/twitter",
            multipartData, Void.class, tokenRule.validToken(), HttpStatus.NO_CONTENT,
            multipartHeaders());

        final ResponseEntity<Connector> updatedConnector = get("/api/v1/connectors/twitter", Connector.class);

        assertThat(updatedConnector.getBody().getId()).isPresent();
        assertThat(updatedConnector.getBody().getDescription()).isNotBlank().isEqualTo("Updated!");
        assertThat(updatedConnector.getBody().getIcon()).isNotBlank().isEqualTo(initialConnector.getIcon());
    }

    // Disabled as it works only for the LocalProcessVerifier which would needs
    // some update
    @Test
    @Ignore
    public void verifyGoodTwitterConnectionSettings() throws IOException {
        final Properties credentials = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/valid-twitter-keys.properties")) {
            credentials.load(is);
        }

        final ResponseEntity<Verifier.Result> response = post("/api/v1/connectors/twitter/verifier/connectivity", credentials,
            Verifier.Result.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        final Verifier.Result result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Verifier.Result.Status.OK);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    public void shouldDeleteConnectors() {
        dataManager.create(new Connector.Builder().id("test-connector").build());
        dataManager.create(new Connection.Builder().id("test-connection").connectorId("test-connector").build());

        final ResponseEntity<Void> response = delete("/api/v1/connectors/test-connector");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(dataManager.fetch(Connector.class, "test-connector")).isNull();
        assertThat(dataManager.fetch(Connection.class, "test-connection")).isNull();
    }

    private HttpHeaders multipartHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

}
