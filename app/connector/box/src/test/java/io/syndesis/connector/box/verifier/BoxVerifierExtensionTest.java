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
package io.syndesis.connector.box.verifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.component.extension.ComponentVerifierExtension.Result;
import org.apache.camel.component.extension.ComponentVerifierExtension.VerificationError;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assume.assumeNotNull;

public class BoxVerifierExtensionTest extends CamelTestSupport {

    private BoxVerifierExtension verifier;

    private static Map<String, Object> parameters = new HashMap<>();

    @BeforeClass
    public static void testConditions() {
        String userName = System.getenv("SYNDESIS_BOX_USERNAME");
        String userPassword = System.getenv("SYNDESIS_BOX_PASSWORD");
        String clientId = System.getenv("SYNDESIS_BOX_CLIENT_ID");
        String clientSecret = System.getenv("SYNDESIS_BOX_CLIENT_SECRET");

        // run tests only when those credentials are provided
        assumeNotNull(userName);
        assumeNotNull(userPassword);
        assumeNotNull(clientId);
        assumeNotNull(clientSecret);

        parameters.put("userName", userName);
        parameters.put("userPassword", userPassword);
        parameters.put("clientId", clientId);
        parameters.put("clientSecret", clientSecret);
    }

    @Before
    public void setupVerifier() throws Exception {
        verifier = new BoxVerifierExtension("box-connector", createCamelContext());
    }

    @Test
    public void verifyParameters() {
        Result result = verifier.verifyParameters(parameters);
        assertEquals(errorDescriptions(result),
            Result.Status.OK, result.getStatus());
    }

    @Test
    public void verifyConnectivity() {
        Result result = verifier.verifyConnectivity(parameters);
        assertEquals(errorDescriptions(result),
            Result.Status.OK, result.getStatus());
    }

    private static String errorDescriptions(Result result) {
        return result.getErrors().stream()
            .map(VerificationError::getDescription)
            .collect(Collectors.joining(", "));
    }
}
