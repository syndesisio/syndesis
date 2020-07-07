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
package io.syndesis.integration.runtime.template.velocity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.syndesis.common.model.integration.step.template.TemplateStepConstants;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;
import io.syndesis.integration.runtime.handlers.AbstractTemplateStepHandlerTest;
import org.junit.Test;

public abstract class AbstractVelocityTemplateStepHandlerTest extends AbstractTemplateStepHandlerTest
    implements TemplateStepConstants{

    @Override
    protected TemplateStepLanguage getLanguage() {
        return TemplateStepLanguage.VELOCITY;
    }

    @Test
    public void testTemplateStepBasicWithPrefix() throws Exception {

        Symbol[] symbols = {
            new Symbol("body.time", "string"),
            new Symbol("body.name", "string"),
            new Symbol("body.text", "string")
        };

        testTemplateStepBasic(symbols);
     }

    @Test
    public void testTemplateStepBasicWithoutPrefix() throws Exception {

        Symbol[] symbols = {
            new Symbol("time", "string", getSymbolSyntax()),
            new Symbol("name", "string", getSymbolSyntax()),
            new Symbol("text", "string", getSymbolSyntax())
        };

        testTemplateStepBasic(symbols);
     }

    @Test
    public void testInvalidTemplate() {
        SymbolSyntax mustacheSyntax = TemplateStepLanguage.MUSTACHE.getDefaultSymbolSyntax();
        Symbol[] symbols = {
            new Symbol("time", "string", mustacheSyntax),
            new Symbol("name", "string", mustacheSyntax),
            new Symbol("text", "string", mustacheSyntax)
        };

        assertThatThrownBy(() -> {
            testTemplateStepBasic(symbols);
        })
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("invalid");
    }
}
