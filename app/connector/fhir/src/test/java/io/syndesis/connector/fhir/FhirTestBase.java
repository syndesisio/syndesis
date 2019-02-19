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
package io.syndesis.connector.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.connector.support.test.ConnectorTestSupport;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okXml;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class FhirTestBase extends ConnectorTestSupport {

    @Rule
    public WireMockRule fhirServer =  new WireMockRule(options().dynamicPort());

    protected FhirContext fhirContext = new FhirContext(FhirVersionEnum.valueOf("DSTU3"));

    /**
     * Override to enable basic authentication
     * @return
     */
    public String getUsername() {
        return null;
    }

    /**
     * Override to enable basic authentication.
     * @return
     */
    public String getPassword() {
        return null;
    }

    /**
     * Override to enable bearer token authentication as specified in OAuth 2.0
     * @return
     */
    public String getBearerToken() {
        return null;
    }

    @Before
    public void stubFhirServerMetadata() throws IOException {
        try (InputStream metadataResponseIn = FhirReadTest.class.getResourceAsStream("metadata_response.xml")) {
            String metadataResponse = IOUtils.toString(metadataResponseIn, StandardCharsets.UTF_8);
            fhirServer.stubFor(get(urlEqualTo("/metadata")).willReturn(okXml(metadataResponse)));
        }
    }

    protected Step newFhirEndpointStep(String actionId, Consumer<Step.Builder> consumer) {
        final Connector connector = getResourceManager().mandatoryLoadConnector("fhir");
        final ConnectorAction action = getResourceManager().mandatoryLookupAction(connector, actionId);

        Connection.Builder connectionBuilder = new Connection.Builder()
            .connector(connector)
            .putConfiguredProperty("serverUrl", fhirServer.baseUrl());

        if (getUsername() != null || getPassword() != null) {
            if (getUsername() == null || getPassword() == null) {
                throw new IllegalStateException("Both username and password must not be null to enable basic authentication");
            }
            if (getBearerToken() != null) {
                throw new IllegalStateException("You must not provide both username/password and bearer token");
            }

            connectionBuilder.putConfiguredProperty("username", getUsername());
            connectionBuilder.putConfiguredProperty("password", getPassword());
        } else if (getBearerToken() != null) {
            connectionBuilder.putConfiguredProperty("accessToken", getBearerToken());
        }

        final Step.Builder builder = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(action)
            .connection(connectionBuilder.build());

        consumer.accept(builder);

        return builder.build();
    }

    protected String toXml(IBaseResource object) {
        String result = fhirContext.newXmlParser().encodeResourceToString(object);
        return result;
    }

    protected List<String> toXmls(IBaseResource... objects) {
        List<String> results = new ArrayList<>();
        for (IBaseResource object: objects) {
            results.add(toXml(object));
        }
        return results;
    }

    public StubMapping stubFhirRequest(MappingBuilder mappingBuilder) {
        if (getUsername() != null) {
            mappingBuilder.withBasicAuth(getUsername(), getPassword());
        } else if (getBearerToken() != null) {
            mappingBuilder.withHeader("Authorization", equalTo("Bearer " + getBearerToken()));
        }

        return fhirServer.stubFor(mappingBuilder);
    }
}
