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
package io.syndesis.integration;

import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.SyndesisHelpers;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
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
public abstract class SyndesisTestSupport extends CamelTestSupport {
    private static final transient Logger LOG = LoggerFactory.getLogger(SyndesisTestSupport.class);

    public static File getBaseDir() {
        String basedir = System.getProperty("basedir", System.getProperty("user.dir", "."));
        File answer = new File(basedir);
        return answer;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        final SyndesisModel syndesis = createSyndesis();
        if (isDumpFlowYAML()) {
            String fileNamePrefix = "target/syndesis-tests/" + getClassNameAsPath() + "-" + getTestMethodName();
            File file = new File(getBaseDir(), fileNamePrefix + ".yml");
            file.getParentFile().mkdirs();
            SyndesisHelpers.saveConfig(syndesis, file);
            SyndesisHelpers.saveConfigJSON(syndesis, new File(getBaseDir(), fileNamePrefix + ".json"));
        }
        return new SyndesisRouteBuilder() {
            @Override
            protected SyndesisModel loadSyndesis() throws IOException {
                return syndesis;
            }
        };

    }

    protected SyndesisModel loadTestYaml() throws IOException {
        String path = getClassNameAsPath() + "-" + getTestMethodName() + ".yml";
        LOG.info("Loading SyndesisModel flows from classpath at: " + path);
        URL resource = getClass().getClassLoader().getResource(path);
        Assertions.assertThat(resource).describedAs("Could not find " + path + " on the classpath!").isNotNull();
        return SyndesisHelpers.loadFromURL(resource);
    }

    private String getClassNameAsPath() {
        return getClass().getName().replace('.', '/');
    }


    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    /**
     * Factory method to create the syndesis flows for the test case
     */
    protected SyndesisModel createSyndesis() throws Exception {
        SyndesisModel syndesis = new SyndesisModel();
        addSyndesisFlows(syndesis);
        return syndesis;
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
     * Adds the flows to this syndesis using the SyndesisModel DSL
     */
    protected void addSyndesisFlows(SyndesisModel syndesis) {
    }

    protected boolean isDumpFlowYAML() {
        return true;
    }
}
