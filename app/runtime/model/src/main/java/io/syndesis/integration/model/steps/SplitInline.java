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
package io.syndesis.integration.model.steps;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.service.AutoService;

/**
 * Inline Splits the payload into multiple messages
 */
@AutoService(Step.class)
@JsonPropertyOrder({"language", "expression"})
public class SplitInline extends Step {
    public static final String KIND = "split-inline";

    private String language;
    private String expression;

    public SplitInline() {
        this(null, null);
    }

    public SplitInline(String expression) {
        this("simple", expression);
    }

    public SplitInline(String language, String expression) {
        super(KIND);

        this.language = language;
        this.expression = expression;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "SplitInline{"
            + "language='" + language + '\''
            + ", expression='" + expression + '\''
            + '}';
    }
}
