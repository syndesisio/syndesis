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
package io.syndesis.common.model.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Violation;
import io.syndesis.common.model.WithConfigurationProperties;
import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.connection.Connector;
import org.immutables.value.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value.Immutable
@JsonDeserialize(builder = APISummary.Builder.class)
@SuppressWarnings("immutables")
public interface APISummary extends WithName, WithConfigurationProperties, WithConfiguredProperties {

    final class Builder extends ImmutableAPISummary.Builder {
        // make ImmutableAPISummary.Builder accessible

        public Builder createFrom(final Connector connector) {
            final ActionsSummary actionsSummary = new ActionsSummary.Builder()//
                .totalActions(connector.getActions().size())//
                .actionCountByTags(
                    connector.getActions().stream()
                        .flatMap(s -> s.getTags().stream().distinct())
                        .collect(
                            Collectors.groupingBy(
                                Function.identity(),
                                Collectors.reducing(0, (e) -> 1, Integer::sum)
                            )
                        )
                )
                .build();

            return new Builder().createFrom((WithConfigurationProperties) connector)//
                .name(connector.getName())//
                .description(connector.getDescription())//
                .icon(Optional.ofNullable(connector.getIcon()))
                .configuredProperties(Collections.emptyMap())//
                .actionsSummary(actionsSummary);
        }

    }

    ActionsSummary getActionsSummary();

    String getDescription();

    List<Violation> getErrors();

    Optional<String> getIcon();

    List<Violation> getWarnings();
}
