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
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

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
     * @param connectorId id of the connector to verify
     * @param parameters options for the connector to verify
     * @return the result of the verification as a list, one element for each scope checked
     */
    List<Result> verify(String connectorId,  Map<String, String> parameters);

    // Scope determines what kind of verification should be performed
    enum Scope {
        // Checked parameters without reaching out to the backend
        PARAMETERS,

        // Checked the connectivity. This happens only when the parameter check succeeded.
        CONNECTIVITY
    }

    // Detailed error object
    @Value.Immutable
    @JsonDeserialize(builder = ImmutableVerifierError.Builder.class)
    interface VerifierError {
        // Connector specific error code
        String getCode();

        // A description of the error in plain english
        Optional<String> getDescription();

        // List of parameters which caused this particular verification to fail
        List<String> getParameters();

        // Map of attributes with detailed error messages
        Map<String, Object> getAttributes();
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
        List<VerifierError> getErrors();

        // Status of the result
        enum Status {
            // All good
            OK,
            // Check for the given scope failes
            ERROR,
            // Verification not supported.
            UNSUPPORTED
        }
    }
}
