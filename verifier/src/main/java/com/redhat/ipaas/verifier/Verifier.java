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
package com.redhat.ipaas.verifier;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.redhat.ipaas.model.connection.Connector;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The result of a verification of connection parameters.
 * It's format is inspired by the discussion from https://issues.apache.org/jira/browse/CAMEL-10795
 *
 * @author roland
 * @since 23/02/2017
 */
public interface Verifier {

    /**
     * Verify a connector
     *
     * @param connector the connector to verify
     * @param scope scope in which to verify (e.g. whether to check connectivity or validity)
     * @param options options for the connector to verify
     * @return the result of the verification
     */
    Result verify(Connector connector, Scope scope, Map<String, String> options);

    // Scope determines what kind of verification should be performed
    enum Scope {
        NONE,
        PARAMETERS,
        CONNECTIVITY;
    }

    // Detailed error object
    @Value.Immutable
    @JsonDeserialize(builder = ImmutableError.Builder.class)
    interface Error {
        // Connector specific error code
        Optional<String> getCode();
        // A description of the error in plain english
        Optional<String> getDescription();
        // List of parameters which caused this particular verification to fail
        List<String> getParameters();

        default Error withCode(String value) {
            return ImmutableError.builder().from(this).code(value).build();
        }
        default Error withDescription(String value) {
            return ImmutableError.builder().from(this).description(value).build();
        }
        default Error withParameters(List<String> value) {
            return ImmutableError.builder().from(this).parameters(value).build();
        }
    }

    // Result structure to return
    @Value.Immutable
    @JsonDeserialize(builder = ImmutableResult.Builder.class)
    interface Result {
        // overall status of the verification
        Status getStatus();
        // scope with which the verifier has been called
        Scope getScope();
        // list of errors or an empty list if
        List<Error> getErrors();

        // Status of the result
        enum Status {
            // All good
            OK,
            // Check for the given scope failes
            ERROR,
            // Scope not supported
            SCOPE_UNSUPPORTED,
            // Verification not supported.
            UNSUPPORTED,
        }
    }
}
