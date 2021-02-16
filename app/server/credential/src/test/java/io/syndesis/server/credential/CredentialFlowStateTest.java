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
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.social.oauth1.OAuthToken;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class CredentialFlowStateTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("data")
    public void shouldRoundTripSerialize(final String type, final CredentialFlowState state) throws IOException {
        final String serialized = mapper.writerFor(state.getClass()).writeValueAsString(state);

        final CredentialFlowState deserialized = mapper.readerFor(CredentialFlowState.class).readValue(serialized);

        final CredentialFlowState stateWithoutRedirectUrl = state.builder().withAll(state).redirectUrl(null).build();

        assertThat(deserialized).usingRecursiveComparison().isEqualTo(stateWithoutRedirectUrl);
    }

    public static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of("OAUTH1", new OAuth1CredentialFlowState.Builder().key("key").providerId("providerId").redirectUrl("redirectUrl")
                .returnUrl(URI.create("return")).token(new OAuthToken("value", "secret")).verifier("verifier").build()),
            Arguments.of("OAUTH2", new OAuth2CredentialFlowState.Builder().key("key").providerId("providerId")
                .redirectUrl("redirectUrl").returnUrl(URI.create("return")).code("code").state("state").build()));
    }
}
