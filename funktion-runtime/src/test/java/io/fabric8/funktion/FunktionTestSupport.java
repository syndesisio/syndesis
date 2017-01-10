/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.funktion;

import io.fabric8.funktion.model.Funktion;
import io.fabric8.funktion.runtime.FunktionRouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.util.List;

/**
 */
public abstract class FunktionTestSupport extends CamelTestSupport {
    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        final Funktion funktion = createFunktion();
        return new FunktionRouteBuilder() {
            @Override
            protected Funktion loadFunktion() throws IOException {
                return funktion;
            }
        };

    }

    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    /**
     * Factory method to create the funktion flows for the test case
     */
    protected Funktion createFunktion() {
        Funktion funktion = new Funktion();
        addFunktionFlows(funktion);
        return funktion;
    }

    protected void logMessagesReceived(MockEndpoint... mockEndpoints) {
        System.out.println();
        for (MockEndpoint mockEndpoint : mockEndpoints) {
            System.out.println("Messages received on endpoint " + mockEndpoint.getEndpointUri());
            List<Exchange> exchanges = mockEndpoint.getExchanges();
            Assertions.assertThat(exchanges).describedAs("exchanges on " + mockEndpoint).isNotNull();
            int count = 0;
            for (Exchange exchange : exchanges) {
                System.out.println("  " + count++ + " = " + exchange.getIn().getBody(String.class));
            }
            System.out.println();
        }
    }

    /**
     * Adds the flows to this funktion
     */
    protected abstract void addFunktionFlows(Funktion funktion);
}
