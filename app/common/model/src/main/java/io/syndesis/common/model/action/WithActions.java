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
package io.syndesis.common.model.action;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.validation.AllValidations;

public interface WithActions<T extends Action> {

    @NotNull(groups = AllValidations.class)
    List<T> getActions();

    @JsonIgnore
    default <S extends T> List<S> getActions(Class<S> clazz) {
        return getActions().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .collect(Collectors.toList());
    }

    default Optional<T> findActionById(String actionId) {
        if (getActions() == null) {
            return Optional.empty();
        }

        return getActions().stream()
            .filter(WithId.class::isInstance)
            .filter(action -> action.getId().isPresent())
            .filter(action -> actionId.equals(action.getId().get()))
            .findFirst();
    }
}
