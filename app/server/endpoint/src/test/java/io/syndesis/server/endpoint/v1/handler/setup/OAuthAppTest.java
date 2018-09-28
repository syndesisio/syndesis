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

import java.util.Collections;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.Credentials;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OAuthAppTest {

    static final ConfigurationProperty CLIENT_ID_PROPERTY = new ConfigurationProperty.Builder()
        .addTag(Credentials.CLIENT_ID_TAG).build();

    static final ConfigurationProperty CLIENT_SECRET_PROPERTY = new ConfigurationProperty.Builder()
        .addTag(Credentials.CLIENT_SECRET_TAG).build();

    final Connector connector = new Connector.Builder()//
        .id("connector-id")//
        .name("Connector")//
        .icon("svg-icon")//
        .putProperty("clientId", CLIENT_ID_PROPERTY)//
        .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
        .putConfiguredProperty("clientId", "client-id")//
        .putConfiguredProperty("clientSecret", "client-secret")//
        .build();

    @Test
    public void shouldClearOAuthProperties() {
        final OAuthApp oauthApp = new OAuthApp.Builder()//
            .id("connector-id")//
            .name("Connector")//
            .icon("svg-icon")//
            .putProperty("clientId", CLIENT_ID_PROPERTY)//
            .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
            .putConfiguredProperty("clientId", "client-id")//
            .putConfiguredProperty("clientSecret", "client-secret")//
            .build();

        final OAuthApp expected = new OAuthApp.Builder()//
            .id("connector-id")//
            .name("Connector")//
            .icon("svg-icon")//
            .putProperty("clientId", CLIENT_ID_PROPERTY)//
            .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
            .build();

        assertThat(oauthApp.clearValues()).isEqualTo(expected);
    }

    @Test
    public void shouldComputeDerived() {
        assertThat(new OAuthApp.Builder().build().isDerived()).isFalse();

        assertThat(new OAuthApp.Builder()//
            .putProperty("clientId", CLIENT_ID_PROPERTY)//
            .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
            .build()//
            .isDerived()).isFalse();

        assertThat(new OAuthApp.Builder()//
            .putProperty("clientId", CLIENT_ID_PROPERTY)//
            .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
            .putConfiguredProperty("clientId", "client-id")//
            .build()//
            .isDerived()).isFalse();

        assertThat(new OAuthApp.Builder()//
            .putProperty("clientId", CLIENT_ID_PROPERTY)//
            .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
            .putConfiguredProperty("clientId", "client-id")//
            .putConfiguredProperty("clientSecret", "client-secret")//
            .build()//
            .isDerived()).isTrue();
    }

    @Test
    public void shouldCreateFromConnector() {
        final OAuthApp oauthApp = OAuthApp.fromConnector(connector);

        final OAuthApp expected = new OAuthApp.Builder()//
            .id("connector-id")//
            .name("Connector")//
            .icon("svg-icon")//
            .putProperty("clientId", CLIENT_ID_PROPERTY)//
            .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
            .putConfiguredProperty("clientId", "client-id")//
            .putConfiguredProperty("clientSecret", "client-secret")//
            .build();

        assertThat(oauthApp).isEqualTo(expected);
    }

    @Test
    public void shouldCreateFromEmptyConnector() {
        final Connector emptyConnector = new Connector.Builder().build();

        final OAuthApp oauthApp = OAuthApp.fromConnector(emptyConnector);

        final OAuthApp expected = new OAuthApp.Builder().build();

        assertThat(oauthApp).isEqualTo(expected);
    }

    @Test
    public void shouldOmitHiddenProperties() {
        final Connector withHiddenProperty = new Connector.Builder().createFrom(connector)
            .putProperty("theHiddenOne",
                new ConfigurationProperty.Builder().type("hidden").addTag(Credentials.AUTHENTICATION_URL_TAG).build())
            .build();
        final OAuthApp oauthApp = OAuthApp.fromConnector(withHiddenProperty);

        final OAuthApp expected = new OAuthApp.Builder()//
            .id("connector-id")//
            .name("Connector")//
            .icon("svg-icon")//
            .putProperty("clientId", CLIENT_ID_PROPERTY)//
            .putProperty("clientSecret", CLIENT_SECRET_PROPERTY)//
            .putConfiguredProperty("clientId", "client-id")//
            .putConfiguredProperty("clientSecret", "client-secret")//
            .build();

        assertThat(oauthApp).isEqualTo(expected);
    }

    @Test
    public void shouldUpdateConnectorWithDefaultValuesIfNoneGiven() {
        final Connector withHiddenProperty = new Connector.Builder().createFrom(connector)
            .putProperty("defaulted", new ConfigurationProperty.Builder().type("hidden")
                .addTag(Credentials.AUTHENTICATION_URL_TAG).defaultValue("I'm a default").build())
            .build();
        final Connector updated = OAuthApp.fromConnector(withHiddenProperty).update(withHiddenProperty);

        assertThat(updated.getConfiguredProperties()).containsEntry("defaulted", "I'm a default");
    }

    @Test
    public void shouldKeepConnectorConfiguredPropertiesIfNoneGiven() {
        final Connector withConfiguredProperty = new Connector.Builder().createFrom(connector)
            .putProperty("configured",
                new ConfigurationProperty.Builder().type("hidden").addTag(Credentials.AUTHENTICATION_URL_TAG).build())
            .putConfiguredProperty("configured", "initial").build();
        final Connector updated = OAuthApp.fromConnector(withConfiguredProperty).update(withConfiguredProperty);

        assertThat(updated.getConfiguredProperties()).containsEntry("configured", "initial");
    }

    @Test
    public void shouldSetConfiguredPropertyIfGivenEvenIfConfiguredPropertyOrDefaultExists() {
        final Connector withHiddenProperty = new Connector.Builder().createFrom(connector)
            .putProperty("prop", new ConfigurationProperty.Builder().type("hidden")
                .addTag(Credentials.AUTHENTICATION_URL_TAG).defaultValue("I'm a default").build())
            .putConfiguredProperty("prop", "initial").build();
        final Connector updated = OAuthApp.fromConnector(withHiddenProperty).update(
            new Connector.Builder().createFrom(withHiddenProperty).putConfiguredProperty("prop", "new-value").build());

        assertThat(updated.getConfiguredProperties()).containsEntry("prop", "new-value");
    }

    @Test
    public void shouldUpdateConnectorKeepingTheSameValues() {
        final Connector updated = OAuthApp.fromConnector(connector).update(connector);

        assertThat(updated).isEqualTo(connector);
    }

    @Test
    public void shouldUpdateConnectorModifyingValues() {
        final OAuthApp app = new OAuthApp.Builder().createFrom(OAuthApp.fromConnector(connector))//
            .putConfiguredProperty("clientId", "new-client-id")//
            .build();

        final Connector updated = app.update(connector);

        final Connector expected = new Connector.Builder().createFrom(connector)//
            .putConfiguredProperty("clientId", "new-client-id")//
            .build();

        assertThat(updated).isEqualTo(expected);
    }

    @Test
    public void shouldUpdateConnectorRemovingValues() {
        final OAuthApp app = new OAuthApp.Builder().createFrom(OAuthApp.fromConnector(connector))//
            .configuredProperties(Collections.emptyMap())//
            .putConfiguredProperty("clientSecret", "client-secret")//
            .build();

        final Connector updated = app.update(connector);

        final Connector expected = new Connector.Builder().createFrom(connector)//
            .configuredProperties(Collections.emptyMap())//
            .putConfiguredProperty("clientSecret", "client-secret")//
            .build();

        assertThat(updated).isEqualTo(expected);
    }
}
