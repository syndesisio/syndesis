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

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.social.oauth1.OAuthToken;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class CredentialFlowStateTest {

    @Parameter(1)
    public CredentialFlowState state;

    @Parameter(0)
    public String type;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldRoundTripSerialize() throws IOException {
        final String serialized = mapper.writerFor(state.getClass()).writeValueAsString(state);

        final CredentialFlowState deserialized = mapper.readerFor(CredentialFlowState.class).readValue(serialized);

        final CredentialFlowState stateWithoutRedirectUrl = state.builder().withAll(state).redirectUrl(null).build();

        assertThat(deserialized).isEqualToComparingFieldByFieldRecursively(stateWithoutRedirectUrl);
    }

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{"OAUTH1",
            new OAuth1CredentialFlowState.Builder().key("key").providerId("providerId").redirectUrl("redirectUrl")
                .returnUrl(URI.create("return")).token(new OAuthToken("value", "secret")).verifier("verifier").build()},
            {"OAUTH2", new OAuth2CredentialFlowState.Builder().key("key").providerId("providerId")
                .redirectUrl("redirectUrl").returnUrl(URI.create("return")).code("code").state("state").build()}});
    }
}
