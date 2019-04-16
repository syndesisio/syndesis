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

package io.syndesis.common.model.expression;

/**
 * @author Christoph Deppisch
 */
public interface RuleBase {

    /**
     * Path expression within the message on which to evaluate on. Can be part of header, body, properties
     * The path is a simple dot notation to the property to evaluate.
     */
    String getPath();

    /**
     * Operator to use for the expression. The value comes from meta data obtained by the UI in
     * a separate call. Example: "contains"
     */
    String getOp();

    /**
     * Value used by operator to decide whether the expression applies
     */
    String getValue();

    default String convertPathToSimple() {
        return String.format("${body.%s}", getPath());
    }

}
