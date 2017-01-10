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
import io.fabric8.funktion.model.Funktions;
import io.fabric8.funktion.runtime.FunktionRouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 */
public abstract class FunktionTestSupport extends CamelTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(FunktionTestSupport.class);

    public static File getBaseDir() {
        String basedir = System.getProperty("basedir", System.getProperty("user.dir", "."));
        File answer = new File(basedir);
        return answer;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        final Funktion funktion = createFunktion();
        if (isDumpFlowYAML()) {
            String fileNamePrefix = "target/funktion-tests/" + getClassNameAsPath() + "-" + getTestMethodName();
            File file = new File(getBaseDir(), fileNamePrefix + ".yml");
            file.getParentFile().mkdirs();
            Funktions.saveConfig(funktion, file);
            Funktions.saveConfigJSON(funktion, new File(getBaseDir(), fileNamePrefix + ".json"));
        }
        return new FunktionRouteBuilder() {
            @Override
            protected Funktion loadFunktion() throws IOException {
                return funktion;
            }
        };

    }

    protected Funktion loadTestYaml() throws IOException {
        String path = getClassNameAsPath() + "-" + getTestMethodName() + ".yml";
        LOG.info("Loading Funktion flows from classpath at: " + path);
        URL resource = getClass().getClassLoader().getResource(path);
        Assertions.assertThat(resource).describedAs("Could not find " + path + " on the classpath!").isNotNull();
        return Funktions.loadFromURL(resource);
    }

    private String getClassNameAsPath() {
        return getClass().getName().replace('.', '/');
    }


    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    /**
     * Factory method to create the funktion flows for the test case
     */
    protected Funktion createFunktion() throws Exception {
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
     * Adds the flows to this funktion using the Funktion DSL
     */
    protected void addFunktionFlows(Funktion funktion) {
    }

    protected boolean isDumpFlowYAML() {
        return true;
    }
}
