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
package io.syndesis.connector.webhook;

import java.util.Collections;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebhookConnectorCustomizerTest {

    static final String SIMPLE_SCHEMA = "{\n" + //
        "  \"type\": \"object\",\n" + //
        "  \"id\": \"io:syndesis:webhook\",\n" + //
        "  \"properties\": {\n" + //
        "    \"parameters\": {\n" + //
        "      \"type\": \"object\",\n" + //
        "      \"properties\": {\n" + //
        "        \"source\": {\n" + //
        "          \"type\": \"string\"\n" + //
        "        },\n" + //
        "        \"status\": {\n" + //
        "          \"type\": \"string\"\n" + //
        "        }\n" + //
        "      }\n" + //
        "    },\n" + //
        "    \"body\": {\n" + //
        "      \"type\": \"object\",\n" + //
        "      \"properties\": {\n" + //
        "        \"company\": {\n" + //
        "          \"type\": \"string\"\n" + //
        "        },\n" + //
        "        \"email\": {\n" + //
        "          \"type\": \"string\"\n" + //
        "        },\n" + //
        "        \"phone\": {\n" + //
        "          \"type\": \"string\"\n" + //
        "        }\n" + //
        "      }\n" + //
        "    }\n" + //
        "  }\n" + //
        "}";

    final ComponentProxyComponent component = new ComponentProxyComponent("test", "test");

    @Test
    public void shouldAddWrapperProcessorIfSyndesisJsonSchemaGiven() {
        final WebhookConnectorCustomizer customizer = new WebhookConnectorCustomizer();
        customizer.setOutputDataShape(new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA).specification(SIMPLE_SCHEMA).build());

        customizer.customize(component, Collections.emptyMap());

        final Processor beforeConsumer = component.getBeforeConsumer();
        assertThat(beforeConsumer).isInstanceOf(WrapperProcessor.class);
        final WrapperProcessor wrapper = (WrapperProcessor) beforeConsumer;
        assertThat(wrapper.parameters).containsOnly("source", "status");
    }

    @Test
    public void shouldDestroyAllOutput() throws Exception {
        final WebhookConnectorCustomizer customizer = new WebhookConnectorCustomizer();
        customizer.customize(component, Collections.emptyMap());

        final Processor afterConsumer = component.getAfterConsumer();
        assertThat(afterConsumer).isNotNull();

        final Exchange exchange = mock(Exchange.class);
        final Message message = mock(Message.class);
        when(exchange.getOut()).thenReturn(message);

        afterConsumer.process(exchange);

        verify(message).setBody("");
        verify(message).removeHeaders("*");
        verify(message).setHeader(Exchange.HTTP_RESPONSE_CODE, 204);
        verify(message).setHeader(Exchange.HTTP_RESPONSE_TEXT, "No Content");
    }

}
