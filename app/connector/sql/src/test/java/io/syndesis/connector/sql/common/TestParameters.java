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
package io.syndesis.connector.sql.common;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public abstract class TestParameters implements TestTemplateInvocationContextProvider {

    private final Stream<Params> cases;

    private static final class CaseInvocationContext implements TestTemplateInvocationContext {
        private final Params params;

        private CaseInvocationContext(final Params params) {
            this.params = params;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Collections.singletonList(new ParameterResolver() {
                @Override
                public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
                    return params;
                }

                @Override
                public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
                    return Params.class.equals(parameterContext.getParameter().getType());
                }
            });
        }

        @Override
        public String getDisplayName(final int invocationIndex) {
            return "[" + invocationIndex + "]: " + params.query;
        }
    }

    public TestParameters(final Stream<Params> cases) {
        this.cases = cases;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(final ExtensionContext context) {
        return cases.map(CaseInvocationContext::new);
    }

    @Override
    public boolean supportsTestTemplate(final ExtensionContext context) {
        return true;
    }

}