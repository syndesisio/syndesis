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
package io.syndesis.common.model.connection;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = PropertyRelation.Builder.class)
@SuppressWarnings("immutables")
public interface PropertyRelation extends Serializable {

    class Builder extends ImmutablePropertyRelation.Builder {
        // make ImmutablePropertyRelation.Builder accessible
    }

    @Value.Immutable
    @JsonDeserialize(builder = When.Builder.class)
    interface When {

        @SuppressWarnings("PMD.UseUtilityClass")
        class Builder extends ImmutableWhen.Builder {
        }

        String getId();

        String getValue();
    }

    String getAction();

    List<When> getWhen();
}
