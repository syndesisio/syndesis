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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.camel.language.simple.types.BinaryOperatorType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpTest {

    @Test
    public void allOperationsShouldBeKnownCamelSimpleOperations() {
        assertThat(Stream.of(Op.DEFAULT_OPTS)
            .map(Op::getOperator))
                .allSatisfy(BinaryOperatorType::asOperator);
    }

    @Test
    public void allOperationsShouldHaveDistinctBinaryOperator() {
        assertThat(Stream.of(Op.DEFAULT_OPTS)
            .map(Op::getOperator)
            .collect(Collectors.toSet()))
                .hasSameSizeAs(Op.DEFAULT_OPTS);
    }

    @Test
    public void allOperationsShouldHaveDistinctLabels() {
        assertThat(Stream.of(Op.DEFAULT_OPTS)
            .map(Op::getLabel)
            .collect(Collectors.toSet()))
                .hasSameSizeAs(Op.DEFAULT_OPTS);
    }
}
