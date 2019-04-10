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
package io.syndesis.common.model.integration.step.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Test;
import io.syndesis.common.util.StringConstants;

public class TestFreeMarkerTemplatePreProcessor implements StringConstants {

    private FreeMarkerTemplatePreProcessor processor = new FreeMarkerTemplatePreProcessor();

    @After
    public void tearDown() {
        processor.reset();
    }

    @Test
    public void testBasicTemplate() throws Exception {
        String template = "${text}";
        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals("${body.text}", newTemplate);
    }

    @Test
    public void testTemplateFormal() throws Exception {
        String template = EMPTY_STRING +
            "At ${time}, ${name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "${text}";

        String expected = EMPTY_STRING +
            "At ${body.time}, ${body.name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "${body.text}";

        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals(expected, newTemplate);
    }

    @Test
    public void test2SymbolsTogether() throws Exception {
        String template = "${name}${description}";
        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals("${body.name}${body.description}", newTemplate);
    }

    /**
     * Freemarker does not allow spaces in its symbols
     * @throws Exception
     */
    @Test
    public void testTemplateSymbolsWithSpaces() throws Exception {
        String template = EMPTY_STRING +
            "At ${the time}, ${the name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "${the text}";
        try {
            processor.preProcess(template);
            fail("Should not allow spaces in template");
        } catch (TemplateProcessingException ex) {
            assertTrue(ex.getMessage().contains("invalid"));
        }
    }
}
