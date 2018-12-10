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
package io.syndesis.integration.runtime.template;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;
import io.syndesis.integration.runtime.handlers.AbstractTemplateStepHandlerTest;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {
        AbstractTemplateStepHandlerTest.TestConfiguration.class
    },
    properties = {
        "spring.main.banner-mode = off",
        "logging.level.io.syndesis.integration.runtime = DEBUG"
    }
)
public class FreeMarkerTemplateStepHandlerTest extends AbstractTemplateStepHandlerTest {

    @Override
    protected TemplateStepLanguage getLanguage() {
        return TemplateStepLanguage.FREEMARKER;
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
            new Symbol("time", "string"),
            new Symbol("name", "string"),
            new Symbol("text", "string")
        };

        testTemplateStepBasic(symbols);
     }

    @Test
    public void testTemplateStepNoSpacesInSymbolAllowed() throws Exception {
        Symbol[] symbols = {
            new Symbol("the time", "string", getSymbolSyntax()),
            new Symbol("the name", "string", getSymbolSyntax()),
            new Symbol("text content", "string", getSymbolSyntax())
        };

        testTemplateStepNoSpacesInSymbolAllowed(symbols);
    }

    @Test
    public void testInvalidTemplate() throws Exception {
        SymbolSyntax mustacheSyntax = TemplateStepLanguage.MUSTACHE.getDefaultSymbolSyntax();
        Symbol[] symbols = {
            new Symbol("time", "string", mustacheSyntax),
            new Symbol("name", "string", mustacheSyntax),
            new Symbol("text", "string", mustacheSyntax)
        };

        try {
            testTemplateStepBasic(symbols);
            fail("Should throw exception since wrong language");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().contains("invalid"));
        }
    }
}
