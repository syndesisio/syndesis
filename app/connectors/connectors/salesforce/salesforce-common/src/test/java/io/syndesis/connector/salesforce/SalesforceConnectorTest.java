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
package io.syndesis.connector.salesforce;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.connector.DefaultConnectorEndpoint;
import org.apache.camel.component.salesforce.api.dto.AbstractDTOBase;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.processor.Pipeline;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SalesforceConnectorTest {

    private final Processor afterProcessor = exchange -> {
        // nop
    };

    private Processor afterProducer;

    private final Processor beforeProcessor = exchange -> {
        // nop
    };

    private Processor beforeProducer;

    private final SalesforceConnector connector = new SalesforceConnector("salesforce-connector", null, SalesforceConnector.class) {
        // annonymous class just for test
    };

    private final SalesforceConnector connectorWithExistingProcessors = new SalesforceConnector("salesforce-connector", null,
        SalesforceConnector.class) {
        {
            setBeforeProducer(beforeProcessor);
            setAfterProducer(afterProcessor);
        }
    };

    private final CamelContext context = new DefaultCamelContext();

    private final Exchange exchange = new DefaultExchange(context);

    @Before
    public void setupConnector() throws Exception {
        connector.setCamelContext(context);
        connectorWithExistingProcessors.setCamelContext(context);

        final DefaultMessage out = new DefaultMessage(context);
        exchange.setOut(out);

        final DefaultConnectorEndpoint endpoint = (DefaultConnectorEndpoint) connector.createEndpoint("salesforce-connector");
        afterProducer = endpoint.getAfterProducer();
        beforeProducer = endpoint.getBeforeProducer();
    }

    @Test
    public void shouldAllowNullInput() throws Exception {
        final Message in = exchange.getIn();
        beforeProducer.process(exchange);

        assertThat(in.getBody()).isNull();
    }

    @Test
    public void shouldAllowNullOutput() throws Exception {
        final Message out = exchange.getOut();
        afterProducer.process(exchange);

        assertThat(out.getBody()).isNull();
    }

    @Test
    public void shouldNotConvertFailedExchanges() throws Exception {
        final Message out = exchange.getOut();
        out.setBody("wat");

        exchange.setException(new Exception());
        afterProducer.process(exchange);

        assertThat(out.getBody()).isEqualTo("wat");
    }

    @Test
    public void shouldNotRemoveExistingProcessors() throws Exception {
        final DefaultConnectorEndpoint endpoint = (DefaultConnectorEndpoint) connectorWithExistingProcessors
            .createEndpoint("salesforce-connector");

        final Processor createdBeforeProducer = endpoint.getBeforeProducer();
        assertThat(createdBeforeProducer).isInstanceOf(Pipeline.class);
        final Pipeline beforePipeline = (Pipeline) createdBeforeProducer;
        assertThat(beforePipeline.getProcessors()).isInstanceOf(List.class).hasSize(2);
        assertThat(((List<Processor>) beforePipeline.getProcessors()).get(0)).isInstanceOf(UnmarshallInputProcessor.class);
        assertThat(((List<Processor>) beforePipeline.getProcessors()).get(1)).isSameAs(beforeProcessor);

        final Processor createdAfterProducer = endpoint.getAfterProducer();
        assertThat(createdAfterProducer).isInstanceOf(Pipeline.class);
        final Pipeline afterPipeline = (Pipeline) createdAfterProducer;
        assertThat(afterPipeline.getProcessors()).isInstanceOf(List.class).hasSize(2);
        assertThat(((List<Processor>) afterPipeline.getProcessors()).get(0)).isInstanceOf(UnmarshallOutputProcessor.class);
        assertThat(((List<Processor>) afterPipeline.getProcessors()).get(1)).isSameAs(afterProcessor);
    }

    @Test
    public void shouldUnmarshallToSpecifiedInputType() throws Exception {
        final Message in = exchange.getIn();
        in.setBody("{}");

        beforeProducer.process(exchange);

        assertThat(in.getBody()).isInstanceOf(SalesforceIdentifier.class);
    }

    @Test
    public void shouldUnmarshallToSpecifiedOutputType() throws Exception {
        final Message out = exchange.getOut();
        out.setBody("{}");

        afterProducer.process(exchange);

        assertThat(out.getBody()).isInstanceOf(AbstractDTOBase.class);
    }
}
