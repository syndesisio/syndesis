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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.After;
import org.junit.Test;
import io.syndesis.common.util.StringConstants;

public class TestVelocityTemplatePreProcessor implements StringConstants {

    private VelocityTemplatePreProcessor processor = new VelocityTemplatePreProcessor();

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
    public void testTemplateInFormal() throws Exception {
        String template = EMPTY_STRING +
            "At $time, $name" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "$text";

        String expected = EMPTY_STRING +
            "At $body.time, $body.name" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "$body.text";

        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals(expected, newTemplate);
    }

    @Test
    public void testTemplateMixedFormalInformal() throws Exception {
        String template = EMPTY_STRING +
            "At $time, ${name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "$text";

        String expected = EMPTY_STRING +
            "At $body.time, ${body.name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "$body.text";

        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals(expected, newTemplate);
    }

    @Test
    public void test2SymbolsTogetherFormal() throws Exception {
        String template = "${name}${description}";
        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals("${body.name}${body.description}", newTemplate);
    }

    @Test
    public void test2SymbolsTogetherInformal() throws Exception {
        String template = "$name$description";
        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals("$body.name$body.description", newTemplate);
    }

    @Test
    public void testTemplateWithOwnAssignment() throws Exception {
        String template = EMPTY_STRING +
            "#set( $footer = \"CompanyX 2018\" )" + NEW_LINE +
            "At ${time}, ${name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "${text}" + NEW_LINE +
            "$footer";

        String expected = EMPTY_STRING +
            "#set( $footer = \"CompanyX 2018\" )" + NEW_LINE +
            "At ${body.time}, ${body.name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "${body.text}" + NEW_LINE +
            "$footer";

        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals(expected, newTemplate);
    }

    @Test
    public void testAlternativeSymbolValue() throws Exception {
        String template = "My name is ${name|'John Doe'}";
        String expected = "My name is ${body.name|'John Doe'}";

        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals(expected, newTemplate);
    }

    @Test
    public void testQuietSymbolTemplate() throws Exception {
        String template = "$!text";
        String newTemplate = processor.preProcess(template);
        assertFalse(template.equals(newTemplate));
        assertEquals("$!body.text", newTemplate);
    }

    /**
     * Velocity does not allow numbers at the start of its symbols
     * @throws Exception
     */
    @Test
    public void testTemplateSymbolBeginningWithNumber() throws Exception {
        String template = EMPTY_STRING +
            "At ${1the time}, ${the name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "${the text}";

        assertThatThrownBy(() -> {
            processor.preProcess(template);
        })
            .isInstanceOf(TemplateProcessingException.class)
            .hasMessageContaining("not valid syntactically");
    }

    /**
     * Velocity does not allow spaces in its symbols
     *
     * @throws Exception
     */
    @Test
    public void testTemplateSymbolsWithSpaces() throws Exception {
        String template = EMPTY_STRING +
            "At ${the time}, ${the name}" + NEW_LINE +
            "submitted the following message:" + NEW_LINE +
            "${the text}";

        assertThatThrownBy(() -> {
            processor.preProcess(template);
        })
            .isInstanceOf(TemplateProcessingException.class)
            .hasMessageContaining("not valid syntactically");
    }
}
