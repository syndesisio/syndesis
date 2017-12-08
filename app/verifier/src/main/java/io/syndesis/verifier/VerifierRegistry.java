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
package io.syndesis.verifier;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * A simple registry holding all the verifiers that are currently supported
 *
 * @author roland
 * @since 28/03/2017
 */
@Component
public class VerifierRegistry {

    private Map<String, Verifier> verifiers;

    public VerifierRegistry(Map<String, Verifier> verifiers) {
        this.verifiers = verifiers;
    }

    /**
     * Get the verifier by the connector id.
     * The connector id is "twitter", not "twitter-mention" (thats an action)
     *
     * @param connectorId connector id to lookup
     * @return the verifier or null if none is registered
     */
    public Verifier getVerifier(String connectorId) {
        return verifiers.get(connectorId);
    }
}
