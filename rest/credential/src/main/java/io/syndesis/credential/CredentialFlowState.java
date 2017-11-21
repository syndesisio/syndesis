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

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.immutables.value.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = ImmutableOAuth1CredentialFlowState.class, name = "OAUTH1"),
    @JsonSubTypes.Type(value = ImmutableOAuth2CredentialFlowState.class, name = "OAUTH2")})
public interface CredentialFlowState {

    String CREDENTIAL_PREFIX = "cred-o";

    String OAUTH1_CREDENTIAL_PREFIX = CREDENTIAL_PREFIX + "1-";

    String OAUTH2_CREDENTIAL_PREFIX = CREDENTIAL_PREFIX + "2-";

    interface Builder {

        @FunctionalInterface
        interface Restorer
            extends BiFunction<List<javax.ws.rs.core.Cookie>, Class<CredentialFlowState>, Set<CredentialFlowState>> {
            // inherits BiFunction::apply
        }

        CredentialFlowState build();

        Builder key(String key);

        Builder providerId(String providerId);

        Builder redirectUrl(String redirectUrl);

        Builder returnUrl(URI returnUrl);

        Builder withAll(CredentialFlowState state);

        static Set<CredentialFlowState> restoreFrom(final Restorer restorer, final HttpServletRequest request) {
            return CredentialFlowStateHelper.restoreFrom(restorer, request);
        }

    }

    Builder builder();

    @Value.Default
    default String getConnectorId() {
        return getProviderId();
    }

    String getKey();

    String getProviderId();

    @JsonIgnore
    String getRedirectUrl();

    URI getReturnUrl();

    default String persistenceKey() {
        return statePrefix() + getKey();
    }

    String statePrefix();

    Type type();

    CredentialFlowState updateFrom(HttpServletRequest request);
}
