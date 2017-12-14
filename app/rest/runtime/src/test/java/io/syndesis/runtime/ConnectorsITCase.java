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
package io.syndesis.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;

import io.syndesis.dao.icon.IconDataAccessObject;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
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

import io.syndesis.model.ListResult;
import io.syndesis.model.connection.Connector;
import io.syndesis.verifier.AlwaysOkVerifier;
import io.syndesis.verifier.Verifier;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ConnectorsITCase extends BaseITCase {

    @Autowired
    private Verifier verifier;

    @Autowired
    private IconDataAccessObject iconDao;

    @Test
    public void connectorsListForbidden() {
        ResponseEntity<JsonNode> response = restTemplate().getForEntity("/api/v1/connectors", JsonNode.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void connectorListWithValidToken() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<ListResult<Connector>> type = (Class) ListResult.class;
        ResponseEntity<ListResult<Connector>> response = get("/api/v1/connectors", type);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        ListResult<Connector> result = response.getBody();
        assertThat(result.getTotalCount()).as("connectors total").isGreaterThan(2);
        assertThat(result.getItems().size()).as("connector list").isGreaterThan(2);
    }

    @Test
    public void connectorsGetTest() {
        ResponseEntity<Connector> response = get("/api/v1/connectors/twitter", Connector.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        Connector connector = response.getBody();
        assertThat(connector).isNotNull();
        assertThat(connector.getId()).contains("twitter");
    }

    // Disabled as it works only for the LocalProcessVerifier which would needs some update
    @Test
    @Ignore
    public void verifyGoodTwitterConnectionSettings() throws IOException {
        Properties credentials = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/valid-twitter-keys.properties")) {
            credentials.load(is);
        }

        ResponseEntity<Verifier.Result> response = post("/api/v1/connectors/twitter/verifier/connectivity", credentials, Verifier.Result.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        Verifier.Result result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Verifier.Result.Status.OK);
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    @Ignore
    public void verifyBadTwitterConnectionSettings() throws IOException {

        // AlwaysOkVerifier never fails.. do don't try this test case, if that's whats being used.
        Assume.assumeFalse(verifier instanceof AlwaysOkVerifier);

        Properties credentials = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/valid-twitter-keys.properties")) {
            credentials.load(is);
        }
        credentials.put("accessTokenSecret", "badtoken");

        ResponseEntity<Verifier.Result> response = post("/api/v1/connectors/twitter/verifier/connectivity", credentials, Verifier.Result.class);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        Verifier.Result result = response.getBody();
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Verifier.Result.Status.ERROR);
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    public void filterConnectorList() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<ListResult<Connector>> type = (Class) ListResult.class;
        ResponseEntity<ListResult<Connector>> response = get("/api/v1/connectors?query=connectorGroupId=trade", type);
        assertThat(response.getStatusCode()).as("component list status code").isEqualTo(HttpStatus.OK);
        ListResult<Connector> result = response.getBody();
        assertThat(result.getTotalCount()).as("connectors total").isEqualTo(2);
        assertThat(result.getItems().size()).as("connector list").isEqualTo(2);
    }

    @Test
    public void testUpdateIcon() throws IOException {
        ResponseEntity<Connector> updated = post(
            "/api/v1/connectors/twitter/icon",
            multipartBody(getClass().getResourceAsStream("test-image.png")),
            Connector.class,
            tokenRule.validToken(),
            HttpStatus.OK,
            multipartHeaders()
        );

        assertThat(updated.getBody().getId()).isPresent();
        assertThat(updated.getBody().getIcon()).isNotBlank().startsWith("db:");

        ResponseEntity<ByteArrayResource> got = get("/api/v1/connectors/twitter/icon", ByteArrayResource.class);
        assertThat(got.getHeaders().getFirst("Content-Type")).isEqualTo("image/png");

        try (ImageInputStream iis = ImageIO.createImageInputStream(got.getBody().getInputStream());) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                final ImageReader reader = readers.next();
                try {
                    reader.setInput(iis);
                    final Dimension dimensions = new Dimension(reader.getWidth(0), reader.getHeight(0));
                    assertThat(dimensions.getHeight()).isEqualTo(106d).as("Wrong image height");
                    assertThat(dimensions.getWidth()).isEqualTo(106d).as("Wrong image width");
                } finally {
                    reader.dispose();
                }
            }
        }
    }

    private HttpHeaders multipartHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private MultiValueMap<String, Object> multipartBody(InputStream is) {
        LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("file", new InputStreamResource(is));
        return multipartData;
    }

}
