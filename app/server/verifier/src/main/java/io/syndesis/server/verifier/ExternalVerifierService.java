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
package io.syndesis.server.verifier;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Implementation of a verifier which uses an external service
 */
@Component
@ConditionalOnProperty(value = "meta.kind", havingValue = "service", matchIfMissing = true)
public class ExternalVerifierService implements Verifier {
    private final MetadataConfigurationProperties config;

    public ExternalVerifierService(MetadataConfigurationProperties config) {
        this.config = config;
    }

    @Override
    public List<Result> verify(String connectorId, Map<String, String> options) {
        return new VerifierCommand(config, connectorId, options).execute();
    }
}
