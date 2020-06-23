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
package io.syndesis.server.controller.integration.camelk.crd;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest {

    @Test
    public void shouldDeserializeFromAKnownIntegrationJson() throws IOException {
        // update the `last-known-camelk-integration.json` with the CR by doing
        // $ oc get integration i-t2l -o json
        // where i-t2l is the name of your integration running with camel-k
        try (InputStream in = Integration.class.getResourceAsStream("last-known-camelk-integration.json")) {
            final Integration read = new ObjectMapper().readValue(in, Integration.class);
            assertThat(read).isNotNull();
        }
    }
}
