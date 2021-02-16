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
package io.syndesis.connector.mongo.meta;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilterUtilTest {

    @Test
    public void verifySimpleFilterInputParameterExtraction() {
        // Given
        String filterExpression = "{test: :#test, test2: \":#someParam\", test3: /:#regexParam/}";
        // When
        List<String> result = FilterUtil.extractParameters(filterExpression);
        // Then
        Assertions.assertThat(result).containsExactly("test", "someParam", "regexParam");
    }

    @Test
    public void verifyComplexFilterInputParameterExtraction() {
        // Given
        String filterExpression = "{ '$or' => [ {a => :#p1 }, { b => :#p2 } ] }";
        // When
        List<String> result = FilterUtil.extractParameters(filterExpression);
        // Then
        Assertions.assertThat(result).containsExactly("p1", "p2");
    }

}
