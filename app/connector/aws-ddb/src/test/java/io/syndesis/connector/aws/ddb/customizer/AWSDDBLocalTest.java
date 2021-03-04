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
package io.syndesis.connector.aws.ddb.customizer;

import org.apache.camel.Exchange;
import org.apache.camel.component.aws.ddb.DdbConstants;
import org.apache.camel.component.aws.ddb.DdbOperations;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AWSDDBLocalTest {

    @Test
    public void putItemCustomizer() {
        DDBConnectorCustomizerPutItem customizer = new DDBConnectorCustomizerPutItem();

        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        customizer.doBeforeProducer(exchange);

        assertThat(exchange.getIn().getHeader(DdbConstants.CONSISTENT_READ)).isEqualTo("true");
        assertThat(exchange.getIn().getHeader(DdbConstants.RETURN_VALUES)).isEqualTo("ALL_OLD");
        assertThat(exchange.getIn().getHeader(DdbConstants.OPERATION)).isEqualTo(DdbOperations.PutItem);
    }

    @Test
    public void queryCustomizer() {
        DDBConnectorCustomizerQuery customizer = new DDBConnectorCustomizerQuery();

        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        customizer.doBeforeProducer(exchange);

        assertThat(exchange.getIn().getHeader(DdbConstants.CONSISTENT_READ)).isEqualTo("true");
        assertThat(exchange.getIn().getHeader(DdbConstants.RETURN_VALUES)).isEqualTo("ALL_OLD");
        assertThat(exchange.getIn().getHeader(DdbConstants.OPERATION)).isEqualTo(DdbOperations.GetItem);
    }

}
