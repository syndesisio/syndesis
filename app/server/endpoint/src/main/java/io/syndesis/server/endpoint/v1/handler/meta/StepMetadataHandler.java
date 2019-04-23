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

package io.syndesis.server.endpoint.v1.handler.meta;

import java.util.List;

import io.syndesis.common.model.connection.DynamicActionMetadata;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;

/**
 * @author Christoph Deppisch
 */
interface StepMetadataHandler {

    /**
     * Adapt dynamic meta data for given step descriptor;
     * @param metadata
     * @return
     */
    default DynamicActionMetadata handle(DynamicActionMetadata metadata) {
        return metadata;
    }

    /**
     * Determine if this handler can handle the specific step kind.
     * @param kind
     */
    default boolean canHandle(StepKind kind) {
        return false;
    }

    /**
     * Creates dynamic metadata for given step. Is provided with list of previous steps and list of subsequent steps in order
     * to adopt data shapes of these in dynamic metadata.
     * @param step
     * @param previousSteps
     * @param subsequentSteps
     * @return
     */
    default DynamicActionMetadata createMetadata(Step step, List<Step> previousSteps, List<Step> subsequentSteps) {
        return DynamicActionMetadata.NOTHING;
    }
}
