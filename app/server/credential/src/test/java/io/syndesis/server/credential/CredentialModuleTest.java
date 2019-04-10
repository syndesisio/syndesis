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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.springframework.social.oauth1.OAuthToken;

import static org.assertj.core.api.Assertions.assertThat;

public class CredentialModuleTest {

    @Test
    public void shouldDeserializeFromJson() throws IOException {
        final String json = "{\"type\":\"OAUTH1\",\"accessToken\":{\"value\":\"access-token-value\",\"secret\":\"access-token-secret\"},\"token\":{\"value\":\"token-value\",\"secret\":\"token-secret\"},\"verifier\":\"verifier\",\"key\":\"key\",\"providerId\":\"twitter\",\"returnUrl\":\"https://localhost:4200/connections/create/configure-fields?state=create-connection&connectorId=twitter\"}";

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new CredentialModule());

        final OAuth1CredentialFlowState flowState = mapper.readerFor(CredentialFlowState.class).readValue(json);

        final OAuth1CredentialFlowState expected = new OAuth1CredentialFlowState.Builder()
            .accessToken(new OAuthToken("access-token-value", "access-token-secret"))
            .token(new OAuthToken("token-value", "token-secret")).verifier("verifier").key("key").providerId("twitter")
            .returnUrl(URI.create(
                "https://localhost:4200/connections/create/configure-fields?state=create-connection&connectorId=twitter"))
            .build();

        assertThat(flowState).isEqualToIgnoringGivenFields(expected, "accessToken", "token");
        assertThat(flowState.getAccessToken()).isEqualToComparingFieldByField(expected.getAccessToken());
        assertThat(flowState.getToken()).isEqualToComparingFieldByField(expected.getToken());
    }

}
