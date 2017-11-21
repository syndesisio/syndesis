/**
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
package io.syndesis.credential;

import java.net.URI;

import io.syndesis.model.connection.Connection;

import org.springframework.boot.autoconfigure.social.SocialProperties;

public class TestCredentialProviderFactory implements CredentialProviderFactory {

    /* default */ static class TestCredentialProvider implements CredentialProvider {

        private final SocialProperties properties;

        public TestCredentialProvider(final SocialProperties properties) {
            this.properties = properties;
        }

        @Override
        public AcquisitionMethod acquisitionMethod() {
            return null;
        }

        @Override
        public Connection applyTo(final Connection connection, final CredentialFlowState flowState) {
            return null;
        }

        @Override
        public CredentialFlowState finish(final CredentialFlowState flowState, final URI baseUrl) {
            return null;
        }

        public SocialProperties getProperties() {
            return properties;
        }

        @Override
        public String id() {
            return null;
        }

        @Override
        public CredentialFlowState prepare(final String connectorId, final URI baseUrl, final URI returnUrl) {
            return null;
        }

    }

    @Override
    public CredentialProvider create(final SocialProperties properties) {
        return new TestCredentialProvider(properties);
    }

    @Override
    public String id() {
        return "test-provider";
    }

}
