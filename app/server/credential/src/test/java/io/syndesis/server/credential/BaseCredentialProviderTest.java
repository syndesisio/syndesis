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
package io.syndesis.server.credential;

import java.net.URI;

import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseCredentialProviderTest {

    private static final MultiValueMap<String, String> NONE = new LinkedMultiValueMap<>();

    @Test
    public void shouldGenerateCallbackUrlWithoutParameters() {
        assertThat(BaseCredentialProvider.callbackUrlFor(URI.create("https://syndesis.io:8443/api/v1/"), NONE))
            .as("The computed callback URL is not as expected")
            .isEqualTo("https://syndesis.io:8443/api/v1/credentials/callback");

    }

    @Test
    public void shouldGenerateCallbackUrlWithParameters() {
        final MultiValueMap<String, String> some = new LinkedMultiValueMap<>();
        some.set("param1", "value1");
        some.set("param2", "value2");

        assertThat(BaseCredentialProvider.callbackUrlFor(URI.create("https://syndesis.io/api/v1/"), some))
            .as("The computed callback URL is not as expected")
            .isEqualTo("https://syndesis.io/api/v1/credentials/callback?param1=value1&param2=value2");
    }
}
