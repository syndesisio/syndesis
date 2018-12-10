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

/**
 * Tests for velocity informal template step handler, ie. using the informal
 * syntax for velocity symbols, eg. $xyz
 *
 */
public class VelocityInformalSyntaxTest extends AbstractVelocityTemplateStepHandlerTest {

    @Override
    protected SymbolSyntax getSymbolSyntax() {
        return TemplateStepLanguage.VELOCITY.getSymbolSyntaxes().get(1);
    }
}
