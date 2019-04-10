/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.server.builder.maven;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.fluent.JsonFluentAssert;
import org.junit.Assert;
import org.junit.Test;


public class TooltipExtractorMojoTest {
    @Test
    public void testExecute() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final TooltipExtractorMojo mojo = new TooltipExtractorMojo();
        final URL source = getClass().getResource("/model.adoc");
        final URL reference = getClass().getResource("/model.json");
        final File path = Paths.get(source.toURI()).toFile();

        mojo.sources = Collections.singletonList(path);
        mojo.output = Paths.get("target/syndesis-tooltips.json").toFile();

        mojo.output.delete();
        if (!mojo.output.getParentFile().exists()) {
            mojo.output.getParentFile().mkdirs();
        }

        mojo.execute();

        Assert.assertTrue("Output document was not created", mojo.output.exists());

        JsonFluentAssert.assertThatJson(mapper.readTree(reference))
            .when(Option.IGNORING_ARRAY_ORDER)
            .isEqualTo(mapper.readTree(mojo.output));
    }
}
