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
package io.syndesis.common.model.filter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.expression.RuleBase;
import org.immutables.value.Value;

/**
 * Created by iocanel on 6/29/17.
 */
@Value.Immutable
@JsonDeserialize(builder = FilterRule.Builder.class)
public interface FilterRule extends RuleBase {

    /**
     * Get the simple filter expression for this rule
     */
    default String getFilterExpression() {
        return String.format("%s %s '%s'",
                             convertPathToSimple(),
                             getOp(),
                             getValue());
    }

    class Builder extends ImmutableFilterRule.Builder { }
}
