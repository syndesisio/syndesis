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
package io.syndesis.server.api.generator.swagger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.TransformException;
import javax.xml.crypto.dsig.TransformService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.connection.ConnectorSettings;
import io.syndesis.common.util.Json;
import io.syndesis.server.api.generator.ConnectorGenerator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import static io.syndesis.server.api.generator.swagger.TestHelper.reformatJson;
import static io.syndesis.server.api.generator.swagger.TestHelper.resource;

import static org.assertj.core.api.Assertions.assertThat;

abstract class BaseSwaggerGeneratorExampleTest extends AbstractSwaggerConnectorTest {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    final Connector expected;

    final String specification;

    public BaseSwaggerGeneratorExampleTest(final String connectorQualifier, final String name) throws IOException {
        specification = resource("/swagger/" + name + ".swagger.json", "/swagger/" + name + ".swagger.yaml");
        expected = Json.reader().forType(Connector.class)
            .readValue(resource("/swagger/" + name + "." + connectorQualifier + "_connector.json"));
    }

    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    public void shouldGenerateAsExpected() throws IOException {

        final ConnectorSettings connectorSettings = new ConnectorSettings.Builder()//
            .putConfiguredProperty("specification", specification)//
            .build();

        final Connector generated = generator().generate(SWAGGER_TEMPLATE, connectorSettings);

        final Map<String, String> generatedConfiguredProperties = generated.getConfiguredProperties();
        final String generatedSpecification = generatedConfiguredProperties.get("specification");

        final Map<String, String> expectedConfiguredProperties = expected.getConfiguredProperties();
        final String expectedSpecification = expectedConfiguredProperties.get("specification");
        assertThat(reformatJson(generatedSpecification)).isEqualTo(reformatJson(expectedSpecification));

        assertThat(without(generatedConfiguredProperties, "specification"))
            .containsAllEntriesOf(without(expectedConfiguredProperties, "specification"));

        assertThat(generated.getProperties().keySet()).as("Expecting the same properties to be generated")
            .containsOnlyElementsOf(expected.getProperties().keySet());
        assertThat(generated.getProperties()).containsAllEntriesOf(expected.getProperties());
        assertThat(generated).isEqualToIgnoringGivenFields(expected, "id", "icon", "properties", "configuredProperties", "actions");
        assertThat(generated.getIcon()).startsWith("data:image");
        assertThat(generated.getActions()).hasSameSizeAs(expected.getActions());

        for (final ConnectorAction expectedAction : expected.getActions()) {
            final String actionId = expectedAction.getId().get().replace("_id_", generated.getId().get());
            final Optional<ConnectorAction> maybeGeneratedAction = generated.findActionById(actionId);
            assertThat(maybeGeneratedAction).as("No action with id: " + actionId + " was generated").isPresent();

            final ConnectorAction generatedAction = maybeGeneratedAction.get();
            assertThat(generatedAction).as("Difference found for action: " + actionId).isEqualToIgnoringGivenFields(expectedAction, "id",
                "descriptor");

            assertThat(generatedAction.getDescriptor().getPropertyDefinitionSteps())
                .as("Generated and expected action definition property definition steps for action with id: " + actionId + " differs")
                .isEqualTo(expectedAction.getDescriptor().getPropertyDefinitionSteps());

            if (expectedAction.getDescriptor().getInputDataShape().isPresent()) {
                final DataShape generatedInputDataShape = generatedAction.getDescriptor().getInputDataShape().get();
                final DataShape expectedInputDataShape = expectedAction.getDescriptor().getInputDataShape().get();

                assertThat(generatedInputDataShape)
                    .as("Generated and expected input data shape for action with id: " + actionId + " differs")
                    .isEqualToIgnoringGivenFields(expectedInputDataShape, "specification");

                if (generatedInputDataShape.getKind() == DataShapeKinds.JSON_SCHEMA) {
                    assertThat(reformatJson(generatedInputDataShape.getSpecification()))
                        .as("Input data shape specification for action with id: " + actionId + " differ")
                        .isEqualTo(reformatJson(expectedInputDataShape.getSpecification()));
                } else {
                    assertThat(c14Xml(generatedInputDataShape.getSpecification()))
                        .as("Input data shape specification for action with id: " + actionId + " differ")
                        .isEqualTo(c14Xml(expectedInputDataShape.getSpecification()));
                }
            }

            if (expectedAction.getDescriptor().getOutputDataShape().isPresent()) {
                final DataShape generatedOutputDataShape = generatedAction.getDescriptor().getOutputDataShape().get();
                final DataShape expectedOutputDataShape = expectedAction.getDescriptor().getOutputDataShape().get();
                assertThat(generatedOutputDataShape)
                    .as("Generated and expected output data shape for action with id: " + actionId + " differs")
                    .isEqualToIgnoringGivenFields(expectedOutputDataShape, "specification");

                if (generatedOutputDataShape.getKind() == DataShapeKinds.JSON_SCHEMA) {
                    assertThat(reformatJson(generatedOutputDataShape.getSpecification()))
                        .as("Output data shape specification for action with id: " + actionId + " differ")
                        .isEqualTo(reformatJson(expectedOutputDataShape.getSpecification()));
                } else {
                    assertThat(c14Xml(generatedOutputDataShape.getSpecification()))
                        .as("Output data shape specification for action with id: " + actionId + " differ")
                        .isEqualTo(c14Xml(expectedOutputDataShape.getSpecification()));
                }
            }
        }
    }

    abstract ConnectorGenerator generator();

    private static String c14Xml(final String xml) {
        if (xml == null) {
            return null;
        }

        try {
            final DocumentBuilder documentBuilder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            final Document document = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            final TransformService transformation = TransformService.getInstance(CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS, "DOM");

            transformation.init(null);

            final NodeList allElements = document.getElementsByTagName("*");
            final List<Node> elements = new ArrayList<>();
            for (int i = 0; i < allElements.getLength(); i++) {
                elements.add(allElements.item(i));
            }

            final OctetStreamData data = (OctetStreamData) transformation.transform((NodeSetData) elements::iterator, null);

            try (final InputStream stream = data.getOctetStream()) {

                final byte[] buffy = new byte[stream.available()];
                stream.read(buffy);

                return new String(buffy, StandardCharsets.UTF_8);
            }
        } catch (GeneralSecurityException | TransformException | SAXException | IOException | ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }

    private static Map<String, String> without(final Map<String, String> map, final String key) {
        final Map<String, String> ret = new HashMap<>(map);

        ret.remove(key);

        return ret;
    }
}
