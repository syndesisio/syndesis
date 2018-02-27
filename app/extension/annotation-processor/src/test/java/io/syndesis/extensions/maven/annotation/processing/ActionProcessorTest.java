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
package io.syndesis.extensions.maven.annotation.processing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import javax.tools.StandardLocation;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import io.syndesis.extension.maven.annotation.processing.ActionProcessor;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

@Ignore
public class ActionProcessorTest {
    @Test
    public void test() throws URISyntaxException, MalformedURLException {
        Compilation compilation = Compiler.javac()
            .withProcessors(new ActionProcessor())
            .compile(JavaFileObjects.forSourceString(
                "test.AnnotatedClassTest",
                "package test;\n" +
                "\n" +
                "@io.syndesis.extension.api.annotations.Action(\n" +
                "    id = \"action-id\",\n" +
                "    name = \"action-name\",\n" +
                "    description = \"action-description\"\n" +
                ")\n" +
                "public class AnnotatedClassTest {\n" +
                "}"
            )
        );

        assertTrue(compilation.generatedFile(StandardLocation.SOURCE_OUTPUT, "test/AnnotatedClassTest-action-id.json").isPresent());
    }
}
