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
package io.syndesis.connector.support.verifier.api;

import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;

/**
 * @author roland
 * @since 28/03/2017
 */
public interface Verifier {

    List<VerifierResponse> verify(CamelContext context, String connectorId, Map<String, Object> parameters);

    /**
     * Scopes to check, in ascending order of sophistication.
     * Each former scope must validate before the next scope can be verified.
     */
    enum Scope {
        /**
         * Validation of given parameter combinations
         */
        PARAMETERS,

        /**
         * Check for the real connectivity to the component endpoint
         */
        CONNECTIVITY;
    }

    enum Status {
        OK,
        ERROR,
        UNSUPPORTED
    }
}
