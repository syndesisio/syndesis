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
package io.syndesis.common.model.integration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = IntegrationOverview.Builder.class)
@SuppressWarnings("immutables")
public interface IntegrationOverview extends WithId<IntegrationOverview>, IntegrationBase {
    @Override
    default Kind getKind() {
        return Kind.IntegrationOverview;
    }

    boolean isDraft();

    @Value.Default
    default IntegrationDeploymentState getCurrentState() {
        return IntegrationDeploymentState.Unpublished;
    }

    @Value.Default
    default IntegrationDeploymentState getTargetState(){
        return IntegrationDeploymentState.Unpublished;
    }

    Optional<Integer> getDeploymentVersion();

    @Value.Default
    default List<IntegrationDeploymentOverview> getDeployments() {
        return Collections.emptyList();
    }

    @Value.Default
    default IntegrationBulletinBoard getBoard() {
        return IntegrationBulletinBoard.emptyBoard();
    }

    Optional<String> getUrl();

    Optional<String> getManagementUrl();

    Set<String> getExposureMeans();

    // ******************
    // Builder
    // ******************

    // allow access to ImmutableIntegrationOverview.Builder
    class Builder extends ImmutableIntegrationOverview.Builder {
    }
}
