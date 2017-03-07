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
package com.redhat.ipaas.model.integration;

import java.io.Serializable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.redhat.ipaas.model.WithId;
import com.redhat.ipaas.model.connection.Connection;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = IntegrationConnectionStep.Builder.class)
public interface IntegrationConnectionStep extends WithId<IntegrationConnectionStep>, Serializable {

    String KIND = "integration-connection-step";

    @Override
    default String getKind() {
        return KIND;
    }

    Integration getIntegration();

    Connection getConnection();

    Step getStep();

    Step getPreviousStep();

    Step getNextStep();

    String getType();

    @Override
    default IntegrationConnectionStep withId(String id) {
        return new Builder().createFrom(this).id(id).build();
    }

    class Builder extends ImmutableIntegrationConnectionStep.Builder {
    }

}
