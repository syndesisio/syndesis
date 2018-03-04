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
package io.syndesis.integration.runtime.util;

import java.util.Objects;

import io.syndesis.common.model.integration.Step;
import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.spi.Language;

public final class JsonSimpleHelpers {
    private JsonSimpleHelpers() {
    }

    public static Predicate getMandatorySimplePredicate(CamelContext context, Step step, String expression) {
        Objects.requireNonNull(expression, "No expression specified for step " + step);
        Predicate answer = new JsonSimplePredicate(expression, context);
        Objects.requireNonNull(answer, "No predicate created from: " + expression);
        return answer;
    }

    public static Expression getMandatoryExpression(CamelContext context, Step step, String expression) {
        Objects.requireNonNull(expression, "No expression specified for step " + step);
        Language jsonpath = getLanguage(context);
        Expression answer = jsonpath.createExpression(expression);
        Objects.requireNonNull(answer, "No expression created from: " + expression);
        return answer;
    }

    public static Language getLanguage(CamelContext context) {
        String languageName = "jsonpath";
        Language answer = context.resolveLanguage(languageName);
        Objects.requireNonNull(answer, "The language `" + languageName + "` cound not be resolved!");
        return answer;
    }
}
