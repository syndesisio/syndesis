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
package io.syndesis.model.environment;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.Kind;
import io.syndesis.model.WithId;
import io.syndesis.model.WithName;
import org.immutables.value.Value;

/**
 * An environment represents a set of properties that apply to a
 * deployment of an integration.  For example, an environment might
 * capture test credentials and hostnames for an application as well
 * as a specific OpenShift namespace to use for deployment.
 */
@Value.Immutable
@JsonDeserialize(builder = Environment.Builder.class)
@SuppressWarnings("immutables")
public interface Environment extends WithId<Environment>, WithName, Serializable {

    @Override
    default Kind getKind() {
        return Kind.Environment;
    }

    EnvironmentType environmentType();

    List<Organization> organizations();

    class Builder extends ImmutableEnvironment.Builder {
    }

}
