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
package io.syndesis.server.credential.salesforce;

import io.syndesis.server.credential.salesforce.SalesforceConfiguration.SalesforceApplicator;
import io.syndesis.common.model.connection.Connection;

import org.junit.Test;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.salesforce.api.Salesforce;
import org.springframework.social.salesforce.connect.SalesforceConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SalesforceApplicatorTest {

    @Test
    public void shouldApplyAdditionalProperties() {
        final SalesforceProperties properties = new SalesforceProperties();
        properties.setAppId("appId");
        properties.setAppSecret("appSecret");

        final AccessGrant accessGrant = new AccessGrant("accessToken", "scope", "refreshToken", 1L);

        final SalesforceConnectionFactory salesforce = mock(SalesforceConnectionFactory.class);
        @SuppressWarnings("unchecked")
        final org.springframework.social.connect.Connection<Salesforce> salesforceConnection = mock(
            org.springframework.social.connect.Connection.class);

        final Salesforce salesforceApi = mock(Salesforce.class);
        when(salesforceConnection.getApi()).thenReturn(salesforceApi);
        when(salesforceApi.getInstanceUrl()).thenReturn("https://instance.salesforce.com");

        when(salesforce.createConnection(accessGrant)).thenReturn(salesforceConnection);

        final Connection.Builder mutableConnection = new Connection.Builder();
        final SalesforceApplicator applicator = new SalesforceApplicator(salesforce, properties);
        applicator.additionalApplication(mutableConnection, accessGrant);

        assertThat(mutableConnection.build().getConfiguredProperties())
            .containsExactly(entry("instanceUrl", "https://instance.salesforce.com"));
    }

}
