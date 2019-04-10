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

import io.syndesis.server.credential.Applicator;
import io.syndesis.server.credential.OAuth2Applicator;
import io.syndesis.common.model.connection.Connection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.social.SocialProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.salesforce.api.Salesforce;
import org.springframework.social.salesforce.connect.SalesforceConnectionFactory;

@Configuration
@ConditionalOnClass(SalesforceConnectionFactory.class)
public class SalesforceConfiguration {

    protected static final class SalesforceApplicator extends OAuth2Applicator {

        private final SalesforceConnectionFactory salesforce;

        public SalesforceApplicator(final SalesforceConnectionFactory salesforce,
            final SocialProperties socialProperties) {
            super(socialProperties);
            this.salesforce = salesforce;

            setClientIdProperty("clientId");
            setClientSecretProperty("clientSecret");
            setRefreshTokenProperty("refreshToken");
        }

        @Override
        protected void additionalApplication(final Connection.Builder mutableConnection,
            final AccessGrant accessGrant) {

            final org.springframework.social.connect.Connection<Salesforce> salesforceConnection = salesforce
                .createConnection(accessGrant);
            final Salesforce salesforceApi = salesforceConnection.getApi();

            final String instanceUrl = salesforceApi.getInstanceUrl();
            Applicator.applyProperty(mutableConnection, "instanceUrl", instanceUrl);
        }
    }

}
