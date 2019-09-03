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
package io.syndesis.common.model.choice;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.expression.RuleBase;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = FlowOption.Builder.class)
public interface FlowOption extends RuleBase {

    /**
     * Matching condition expression in form of simple language expression. More powerful way
     * of specifying the condition for this mapping. But user has to know bits of the simple language.
     */
    String getCondition();

    /**
     * Flow that this mapping is pointing to. Usually a flow identifier.
     */
    String getFlow();

    @Override
    String getPath();

    @Override
    String getOp();

    @Override
    String getValue();

    /**
     * Get the final condition expression for this mapping either directly referencing the condition or building a
     * simple expression from given path, operator and value.
     */
    default String getConditionExpression() {
        if (getCondition() != null && getCondition().trim().length() > 0) {
            return getCondition();
        }

        return String.format("%s %s '%s'",
                convertPathToSimple(),
                getOp(),
                getValue());
    }

    class Builder extends ImmutableFlowOption.Builder { }
}
