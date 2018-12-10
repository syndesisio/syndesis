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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
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
/**
 * Tests for velocity formal template step handler, ie. using the formal
 * syntax for velocity symbols, eg. ${xyz}
 *
 */
public class VelocityFormalSyntaxTest extends AbstractVelocityTemplateStepHandlerTest {

    @Test
    public void testTemplateStepNoSpacesInSymbolAllowed() throws Exception {
        Symbol[] symbols = {
            new Symbol("the time", "string"),
            new Symbol("the name", "string"),
            new Symbol("text content", "string")
        };

        testTemplateStepNoSpacesInSymbolAllowed(symbols);
     }
}
