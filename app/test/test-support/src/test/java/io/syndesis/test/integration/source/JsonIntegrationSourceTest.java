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

package io.syndesis.test.integration.source;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.Json;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class JsonIntegrationSourceTest {

    @Test
    public void getFromJsonString() throws JsonProcessingException {
        Integration expected = new Integration.Builder()
                .name("test-integration")
                .addConnection(new Connection.Builder()
                    .name("test-connection")
                    .connector(new Connector.Builder()
                        .name("test-connector")
                        .addAction(new ConnectorAction.Builder()
                            .name("test-action")
                            .descriptor(new ConnectorDescriptor.Builder()
                                .componentScheme("test")
                                .build())
                            .build())
                        .build())
                    .build())
                .addFlow(new Flow.Builder()
                    .name("test-flow")
                    .addStep(new Step.Builder()
                        .stepKind(StepKind.log)
                        .putConfiguredProperty("customText", "Hello from Syndesis")
                        .build())
                    .build())
                .build();

        JsonIntegrationSource source = new JsonIntegrationSource(Json.writer().forType(Integration.class).writeValueAsString(expected));
        Assert.assertEquals(expected, source.get());
    }

    @Test
    public void getFromJsonFile() throws IOException, URISyntaxException {
        Integration expected = Json.readFromStream(IntegrationExportSourceTest.class.getResourceAsStream("TimerToLog.json"), Integration.class);
        JsonIntegrationSource source = new JsonIntegrationSource(Paths.get(JsonIntegrationSourceTest.class.getResource("TimerToLog.json").toURI()));
        Assert.assertEquals(expected, source.get());
    }
}
