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
package io.syndesis.server.endpoint.v1.handler.setup;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.Credentials;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuthConnectorFilterTest {

    @Test
    public void shouldFilterOutNonOAuthConnectors() {
        final Connector connector1 = new Connector.Builder().build();
        final Connector connector2 = new Connector.Builder()
            .putProperty("clientId", new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_ID_TAG).build())
            .putConfiguredProperty("clientId", "my-client-id").build();
        final Connector connector3 = new Connector.Builder()
            .putProperty("clientId", new ConfigurationProperty.Builder().addTag(Credentials.CLIENT_ID_TAG).build()).build();

        final ListResult<Connector> result = ListResult.of(connector1, connector2, connector3);

        assertThat(OAuthConnectorFilter.INSTANCE.apply(result)).containsOnly(connector2, connector3);
    }
}
