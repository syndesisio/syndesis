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

package io.syndesis.connector.kudu;

import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.DefaultResultVerificationError;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KuduConnectorVerifierExtensionTest extends AbstractKuduCustomizerTestSupport {
    private KuduConnectorVerifierExtension verifier;

    @Before
    public void setupVerifier() throws Exception {
        this.verifier = new KuduConnectorVerifierExtension(createCamelContext());
    }

    @Test
    public void verifyParametersPort() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("host", "somehost");

        ComponentVerifierExtension.Result result = verifier.verifyParameters(options);
        List<ComponentVerifierExtension.VerificationError> errors = result.getErrors();
        DefaultResultVerificationError portError = (DefaultResultVerificationError) errors.get(0);
        Assert.assertEquals("port should be set", portError.getDescription());
    }

    @Test
    public void verifyParametersHost() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("port", 123);

        ComponentVerifierExtension.Result result = verifier.verifyParameters(options);
        List<ComponentVerifierExtension.VerificationError> errors = result.getErrors();
        DefaultResultVerificationError hostError = (DefaultResultVerificationError) errors.get(0);
        Assert.assertEquals("host should be set", hostError.getDescription());
    }

    @Test
    public void verifyConnectivity() {
        Map<String, Object> options = new HashMap<>();
        options.put("host", "somehost");
        options.put("port", 123);

        ComponentVerifierExtension.Result result = verifier.verifyConnectivity(options);
        List<ComponentVerifierExtension.VerificationError> errors = result.getErrors();
        Assert.assertFalse(errors.isEmpty());
    }
}