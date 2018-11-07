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
package io.syndesis.connector.rest.swagger;

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseCustomizerTest {

    @Test
    public void shouldDetermineAddingResponseConverterRobustly() {
        assertThat(ResponseCustomizer.isUnifiedDataShape(null)).isFalse();
        assertThat(ResponseCustomizer.isUnifiedDataShape(new DataShape.Builder().build())).isFalse();
        assertThat(ResponseCustomizer.isUnifiedDataShape(new DataShape.Builder().kind(DataShapeKinds.JAVA).build())).isFalse();
        assertThat(ResponseCustomizer.isUnifiedDataShape(new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA).build())).isFalse();
        assertThat(ResponseCustomizer.isUnifiedDataShape(new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA).specification("xyz").build())).isFalse();
        assertThat(ResponseCustomizer.isUnifiedDataShape(new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA).specification("{}").build())).isFalse();
    }

    @Test
    public void shouldDetermineAddingResponseConverterWithWrappedSchema() {
        assertThat(ResponseCustomizer
            .isUnifiedDataShape(new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA).specification("{\"$id\":\"io:syndesis:wrapped\"}").build())).isTrue();
    }
}
