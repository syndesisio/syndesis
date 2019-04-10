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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = AcquisitionResponse.Builder.class)
public interface AcquisitionResponse {

    @SuppressWarnings("PMD.UseUtilityClass")
    final class Builder extends ImmutableAcquisitionResponse.Builder {

        public static AcquisitionResponse.Builder from(final AcquisitionFlow flow) {
            return new AcquisitionResponse.Builder().type(flow.getType()).redirectUrl(flow.getRedirectUrl());
        }

    }

    @Value.Immutable
    @JsonDeserialize(builder = State.Builder.class)
    interface State {

        @SuppressWarnings("PMD.UseUtilityClass")
        final class Builder extends ImmutableState.Builder {

            public static State cookie(final String spec) {
                return new State.Builder().spec(spec).persist(Persist.COOKIE).build();
            }

        }

        enum Persist {
            COOKIE
        }

        Persist persist();

        String spec();
    }

    String getRedirectUrl();

    Type getType();

    State state();
}
