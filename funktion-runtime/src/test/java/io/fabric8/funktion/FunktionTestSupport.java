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
import org.apache.camel.RoutesBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;

import java.io.IOException;

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

    /**
     * Factory method to create the funktion flows for the test case
     */
    protected Funktion createFunktion() {
        Funktion funktion = new Funktion();
        addFunktionFlows(funktion);
        return funktion;
    }

    /**
     * Adds the flows to this funktion
     */
    protected abstract void addFunktionFlows(Funktion funktion);
}
