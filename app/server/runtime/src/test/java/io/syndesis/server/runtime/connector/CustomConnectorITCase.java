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
package io.syndesis.server.runtime.connector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorGroup;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.model.connection.ConnectorTemplate;
import io.syndesis.common.model.icon.Icon;
import io.syndesis.server.api.generator.ConnectorGenerator;
import io.syndesis.server.dao.file.IconDao;
import io.syndesis.server.runtime.BaseITCase;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration
public class CustomConnectorITCase extends BaseITCase {

    private static final String SECOND_TEMPLATE_ID = "second-connector-template";

    private static final String TEMPLATE_ID = "connector-template";

    private final Connector connector1 = new Connector.Builder().id("connector-from-template-1")
        .connectorGroup(new ConnectorGroup.Builder().id(TEMPLATE_ID).name("connector-template-group").build()).connectorGroupId(TEMPLATE_ID)
        .build();

    private final Connector connector2 = new Connector.Builder().id("connector-from-template-2")
        .connectorGroup(new ConnectorGroup.Builder().id(TEMPLATE_ID).name("connector-template-group").build()).build();

    private final Connector connector3 = new Connector.Builder().id("connector-from-second-template")
        .connectorGroup(new ConnectorGroup.Builder().id(SECOND_TEMPLATE_ID).name("second-connector-template-group").build()).build();

    @Autowired
    private IconDao iconDao;

    private final Connector nonCustomConnector = new Connector.Builder().id("non-custom-connector").build();

    private final ConnectorTemplate template = createConnectorTemplate(TEMPLATE_ID, "connector template");

    @Configuration
    public static class TestConfiguration {
        private static final ActionsSummary ACTIONS_SUMMARY = new ActionsSummary.Builder().totalActions(5).putActionCountByTag("foo", 3)
            .putActionCountByTag("bar", 2).build();

        private static final ConfigurationProperty PROPERTY_1 = new ConfigurationProperty.Builder().displayName("Property 1").build();

        @Bean(TEMPLATE_ID)
        public static ConnectorGenerator testGenerator() {
            return new ConnectorGenerator(new Connector.Builder()
                .addTags("from-connector")
                .build()) {
                @Override
                public Connector generate(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    return generateTestConnector(connectorTemplate, connectorSettings);
                }

                @Override
                public APISummary info(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    final Connector base = generateTestConnector(connectorTemplate, connectorSettings);

                    return new APISummary.Builder().createFrom(base).actionsSummary(ACTIONS_SUMMARY).build();
                }

                Connector generateTestConnector(final ConnectorTemplate connectorTemplate, final ConnectorSettings connectorSettings) {
                    return new Connector.Builder().createFrom(baseConnectorFrom(connectorTemplate, connectorSettings))//
                        .icon("test-icon")//
                        .putProperty("property1", PROPERTY_1)//
                        .build();
                }

                @Override
                protected String determineConnectorDescription(final ConnectorTemplate connectorTemplate,
                    final ConnectorSettings connectorSettings) {
                    return "test-description";
                }

                @Override
                protected String determineConnectorName(final ConnectorTemplate connectorTemplate,
                    final ConnectorSettings connectorSettings) {
                    return "test-name";
                }
            };

        }
    }

    @Before
    public void createConnectorTemplates() {
        dataManager.create(template);
        final ConnectorTemplate secondTemplate = createConnectorTemplate(SECOND_TEMPLATE_ID, "connector template");

        dataManager.create(secondTemplate);

        dataManager.create(connector1);
        dataManager.create(connector2);
        dataManager.create(connector3);
        dataManager.create(nonCustomConnector);
    }

    @Test
    public void shouldCreateNewCustomConnectors() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder().connectorTemplateId(TEMPLATE_ID).build();

        final ResponseEntity<Connector> response = post("/api/v1/connectors/custom", connectorSettings, Connector.class);

        final Connector created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getDescription()).isEqualTo("test-description");
        assertThat(dataManager.fetch(Connector.class, response.getBody().getId().get())).isNotNull();
    }

    @Test
    public void shouldCreateNewCustomConnectorsFromMultipartWithIcon() throws IOException {
        final ResponseEntity<Connector> response = post("/api/v1/connectors/custom",
            multipartBody(
                new ConnectorSettings.Builder().connectorTemplateId(TEMPLATE_ID)
                    .putConfiguredProperty("specification", "here-be-specification").build(),
                CustomConnectorITCase.class.getResourceAsStream("/io/syndesis/server/runtime/test-image.png")),
            Connector.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        final Connector created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getDescription()).isEqualTo("test-description");
        assertThat(dataManager.fetch(Connector.class, response.getBody().getId().get())).isNotNull();
        assertThat(created.getIcon()).startsWith("db:");
        final Icon icon = dataManager.fetch(Icon.class, created.getIcon().substring(3));
        assertThat(icon.getMediaType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);

        try (InputStream storedIcon = iconDao.read(icon.getId().get());
            InputStream expectedIcon = CustomConnectorITCase.class.getResourceAsStream("/io/syndesis/server/runtime/test-image.png")) {
            assertThat(storedIcon).hasSameContentAs(expectedIcon);
        }
    }

    @Test
    public void shouldCreateNewCustomConnectorsFromMultipartWithSpecificationAndIcon() throws IOException {
        final ResponseEntity<Connector> response = post("/api/v1/connectors/custom",
            multipartBody(new ConnectorSettings.Builder().connectorTemplateId(TEMPLATE_ID).build(),
                getClass().getResourceAsStream("/io/syndesis/server/runtime/test-image.png"),
                new ByteArrayInputStream("here-be-specification".getBytes(StandardCharsets.US_ASCII))),
            Connector.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        final Connector created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getDescription()).isEqualTo("test-description");
        assertThat(dataManager.fetch(Connector.class, response.getBody().getId().get())).isNotNull();
        assertThat(created.getIcon()).startsWith("db:");
        final Icon icon = dataManager.fetch(Icon.class, created.getIcon().substring(3));
        assertThat(icon.getMediaType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);

        try (InputStream storedIcon = iconDao.read(icon.getId().get());
            InputStream expectedIcon = CustomConnectorITCase.class.getResourceAsStream("/io/syndesis/server/runtime/test-image.png")) {
            assertThat(storedIcon).hasSameContentAs(expectedIcon);
        }
    }

    @Test
    public void shouldOfferCustomConnectorInfo() {
        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder().connectorTemplateId(TEMPLATE_ID).icon("test-icon")
            .build();

        final ResponseEntity<APISummary> response = post("/api/v1/connectors/custom/info", connectorSettings, APISummary.class);

        final APISummary expected = new APISummary.Builder()// \
            .name("test-name")//
            .description("test-description")//
            .icon("test-icon")//
            .putProperty("property1", TestConfiguration.PROPERTY_1)//
            .actionsSummary(TestConfiguration.ACTIONS_SUMMARY)//
            .build();
        assertThat(response.getBody()).isEqualTo(expected);
    }

    @Test
    public void shouldProvideSummaryForCustomConnectors() {
        final ResponseEntity<Connector> responseForCustomConnector = get("/api/v1/connectors/connector-from-template-1", Connector.class);

        assertThat(responseForCustomConnector.getBody().getActionsSummary()).isPresent();

        final ResponseEntity<Connector> responseForNonCustomConnector = get("/api/v1/connectors/non-custom-connector", Connector.class);

        assertThat(responseForNonCustomConnector.getBody().getActionsSummary()).isNotPresent();
    }

    private MultiValueMap<String, Object> multipartBody(final ConnectorSettings connectorSettings, final InputStream icon) {
        final LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("connectorSettings", connectorSettings);
        multipartData.add("icon", new InputStreamResource(icon));
        return multipartData;
    }

    private MultiValueMap<String, Object> multipartBody(final ConnectorSettings connectorSettings, final InputStream icon,
        final InputStream specification) {
        final MultiValueMap<String, Object> multipartData = multipartBody(connectorSettings, icon);
        multipartData.add("specification", new InputStreamResource(specification));

        return multipartData;
    }

    private HttpHeaders multipartHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private static ConnectorTemplate createConnectorTemplate(final String id, final String name) {
        return new ConnectorTemplate.Builder()//
            .id(id)//
            .name(name)//
            .build();
    }
}
