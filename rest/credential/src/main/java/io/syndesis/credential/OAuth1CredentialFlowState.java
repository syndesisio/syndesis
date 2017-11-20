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
package io.syndesis.credential;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;
import org.springframework.social.oauth1.OAuthToken;

@Value.Immutable
@JsonDeserialize(builder = OAuth1CredentialFlowState.Builder.class)
@SuppressWarnings("immutables")
public interface OAuth1CredentialFlowState extends CredentialFlowState {

    final class Builder extends ImmutableOAuth1CredentialFlowState.Builder implements CredentialFlowState.Builder {

        @Override
        public CredentialFlowState.Builder withAll(final CredentialFlowState state) {
            return createFrom(state);
        }

    }

    final class OAuthTokenConverter implements Converter<Map<String, String>, OAuthToken> {

        @Override
        public OAuthToken convert(final Map<String, String> value) {
            return new OAuthToken(value.get("value"), value.get("secret"));
        }

        @Override
        public JavaType getInputType(final TypeFactory typeFactory) {
            return typeFactory.constructMapLikeType(HashMap.class, String.class, String.class);
        }

        @Override
        public JavaType getOutputType(final TypeFactory typeFactory) {
            return typeFactory.constructType(OAuthToken.class);
        }

    }

    @Override
    default Builder builder() {
        return new Builder();
    }

    OAuthToken getAccessToken();

    @JsonDeserialize(converter = OAuthTokenConverter.class)
    OAuthToken getToken();

    String getVerifier();

    @Override
    default String statePrefix() {
        return OAUTH1_CREDENTIAL_PREFIX;
    }

    @Override
    default Type type() {
        return Type.OAUTH1;
    }

    @Override
    default CredentialFlowState updateFrom(final HttpServletRequest request) {
        final String verifier = request.getParameter("oauth_verifier");

        if (StringUtils.isEmpty(verifier)) {
            throw new IllegalArgumentException("Did not receive OAuth oauth_verifier in request parameters");
        }

        return new OAuth1CredentialFlowState.Builder().createFrom(this).verifier(verifier).build();
    }
}
