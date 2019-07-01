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

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Information on the method of credential acquisition, needed for UI to present
 * it to the user.
 */
@Value.Immutable
@JsonDeserialize(builder = AcquisitionMethod.Builder.class)
public interface AcquisitionMethod {

    AcquisitionMethod NONE = new Builder().build();

    final class Builder extends ImmutableAcquisitionMethod.Builder {
        // builder implemented by Immutables, access allowed through this
        // subclass
    }

    String getDescription();

    String getIcon();

    String getLabel();

    Type getType();

    @JsonProperty("configured")
    boolean configured();
}
