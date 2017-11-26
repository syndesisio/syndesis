/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import io.syndesis.integration.model.SyndesisModel;
import io.syndesis.integration.model.YamlHelpers;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class SyndesisTestSupport extends CamelTestSupport {
    private static final Logger LOG = LoggerFactory.getLogger(SyndesisTestSupport.class);

    @Override
    public boolean isDumpRouteCoverage() {
        return true;
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new SyndesisRouteBuilder("") {
            @Override
            protected SyndesisModel loadModel() throws Exception {
                return createSyndesis();
            }
        };
    }

    protected SyndesisModel loadTestYaml() throws IOException {
        String path = getClass().getName().replace('.', '/') + "-" + getTestMethodName() + ".yml";
        LOG.info("Loading SyndesisModel flows from classpath at: " + path);

        URL resource = getClass().getClassLoader().getResource(path);
        Assertions.assertThat(resource).describedAs("Could not find " + path + " on the classpath!").isNotNull();

        try (InputStream is = resource.openStream()) {
            return YamlHelpers.load(is);
        }
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
        for (MockEndpoint mockEndpoint : mockEndpoints) {
            LOG.info("Messages received on endpoint " + mockEndpoint.getEndpointUri());
            List<Exchange> exchanges = mockEndpoint.getExchanges();
            Assertions.assertThat(exchanges).describedAs("exchanges on " + mockEndpoint).isNotNull();
            int count = 0;
            for (Exchange exchange : exchanges) {
                LOG.info("  " + count++ + " = " + exchange.getIn().getBody(String.class));
            }
        }
    }

    /**
     * Adds the flows to this syndesis using the SyndesisModel DSL
     */
    protected void addSyndesisFlows(SyndesisModel syndesis) {
    }
}
