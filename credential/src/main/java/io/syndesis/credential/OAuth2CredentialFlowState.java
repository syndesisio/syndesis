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

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = OAuth2CredentialFlowState.Builder.class)
public interface OAuth2CredentialFlowState extends CredentialFlowState {

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

        return new OAuth2CredentialFlowState.Builder().createFrom(this).code(code).build();
    }

}
