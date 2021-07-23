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
package io.syndesis.connector.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.syndesis.connector.kafka.service.KafkaBrokerService;
import org.apache.camel.Endpoint;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class KafkaConnectorSASLOAuthBearerTest extends KafkaConnectorTestSupport {

    private static final String SEC_PROTOCOL = "SASL_SSL";
    private static final String USER = "foo";
    private static final String SECRET = "bar";
    private static final String HANDLER = "MyFoo.class";
    private static final String TOKEN_URL = "https://my.test/path1";
    private static final String SASL_LOGIN_CONFIG = String.format("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required " +
        "oauth.client.id=\"%s\" " +
        "oauth.client.secret=\"%s\" " +
        "oauth.token.endpoint.uri=\"%s\" ;", USER, SECRET, TOKEN_URL);

    @Override
    protected Map<String, String> connectorParameters() {
        Map<String, String> params = new HashMap<>();
        // these should be the parameters defined in kafka.json
        params.put(KafkaBrokerService.BROKERS, "test:9092");
        params.put(KafkaBrokerService.TRANSPORT_PROTOCOL, SEC_PROTOCOL);
        params.put(KafkaBrokerService.SASL_MECHANISM, KafkaBrokerService.OAUTHBEARER);
        params.put(KafkaBrokerService.USERNAME, USER);
        params.put(KafkaBrokerService.PASSWORD, SECRET);
        params.put(KafkaBrokerService.SASL_LOGIN_CALLBACK_HANDLER_CLASS, HANDLER);
        params.put(KafkaBrokerService.OAUTH_TOKEN_ENDPOINT_URI, TOKEN_URL);
        return params;
    }

    @Test
    public void testKafkaConnector() throws Exception {
        context().start();
        Optional<Endpoint> endpoint = context().getEndpoints().stream().filter(e -> e instanceof KafkaEndpoint).findFirst();
        Assertions.assertThat(endpoint.isPresent()).isTrue();
        KafkaConfiguration kafkaConfiguration = ((KafkaEndpoint) endpoint.get()).getConfiguration();
        Assertions.assertThat(kafkaConfiguration.getSaslMechanism()).isEqualTo(KafkaBrokerService.OAUTHBEARER);
        Assertions.assertThat(kafkaConfiguration.getSecurityProtocol()).isEqualTo(SEC_PROTOCOL);
        Assertions.assertThat(kafkaConfiguration.getSaslJaasConfig()).isEqualTo(SASL_LOGIN_CONFIG);
        Assertions.assertThat(kafkaConfiguration.getAdditionalProperties().get("sasl.login.callback.handler.class")).isEqualTo(HANDLER);
    }
}
