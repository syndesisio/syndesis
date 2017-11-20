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
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.social.oauth2.AccessGrant;

@Value.Immutable
@JsonDeserialize(builder = OAuth2CredentialFlowState.Builder.class)
@SuppressWarnings("immutables")
public interface OAuth2CredentialFlowState extends CredentialFlowState {

    final class AccessGrantConverter implements Converter<Map<String, String>, AccessGrant> {

        @Override
        public AccessGrant convert(final Map<String, String> value) {
            final String expireTimeString = value.get("expireTime");
            final Long expireTime = Optional.ofNullable(expireTimeString).map(Long::valueOf).orElse(null);

            return new AccessGrant(value.get("accessToken"), value.get("scope"), value.get("refreshToken"), expireTime);
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

    final class Builder extends ImmutableOAuth2CredentialFlowState.Builder implements CredentialFlowState.Builder {
        @Override
        public CredentialFlowState.Builder withAll(final CredentialFlowState state) {
            return createFrom(state);
        }

    }

    @Override
    default Builder builder() {
        return new Builder();
    }

    @JsonDeserialize(converter = AccessGrantConverter.class)
    AccessGrant getAccessGrant();

    String getCode();

    String getState();

    @Override
    default String statePrefix() {
        return OAUTH2_CREDENTIAL_PREFIX;
    }

    @Override
    default Type type() {
        return Type.OAUTH2;
    }

    @Override
    default CredentialFlowState updateFrom(final HttpServletRequest request) {
        final String code = request.getParameter("code");

        if (StringUtils.isEmpty(code)) {
            throw new IllegalArgumentException("Did not receive OAuth code in request parameters");
        }

        return new OAuth2CredentialFlowState.Builder().createFrom(this).code(code).build();
    }

}
