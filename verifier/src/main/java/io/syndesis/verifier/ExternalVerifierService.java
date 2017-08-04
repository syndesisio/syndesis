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
package io.syndesis.verifier;

import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementation of a verifier which uses an external service
 */
@Component
@ConditionalOnProperty(value = "verifier.kind", havingValue = "service", matchIfMissing = true)
public class ExternalVerifierService implements Verifier {

    private final VerificationConfigurationProperties config;

    public ExternalVerifierService(VerificationConfigurationProperties config) {
        this.config = config;
    }

    public List<Result> verify(String connectorId, Map<String, String> options) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(String.format("http://%s/api/v1/verifier/%s", config.getService(), connectorId));
        return target.request(MediaType.APPLICATION_JSON).post(Entity.entity(options, MediaType.APPLICATION_JSON),
                                                               new GenericType<List<Result>>(){});
    }
}
