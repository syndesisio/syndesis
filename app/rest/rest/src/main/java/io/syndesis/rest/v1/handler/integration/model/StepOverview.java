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
package io.syndesis.rest.v1.handler.integration.model;

import java.util.Optional;

import io.syndesis.core.SuppressFBWarnings;
import io.syndesis.model.Kind;
import io.syndesis.model.integration.Step;

@SuppressFBWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", "URF_UNREAD_FIELD"})
public class StepOverview {

    private final Step step;

    public StepOverview(Step step) {
        this.step = step;
    }

    public Optional<String> getId() {
        return step.getId();
    }

    public Kind getKind() {
        return step.getKind();
    }

    public String getStepKind() {
        return step.getStepKind();
    }

    public String getName() {
        return step.getName();
    }

    public Optional<ConnectionOverview> getConnection() {
        return step.getConnection().map(x -> new ConnectionOverview(x));
    }

    public Optional<WithNameOverview> getAction() {
        return step.getAction().map(x -> new WithNameOverview(x));
    }
}
