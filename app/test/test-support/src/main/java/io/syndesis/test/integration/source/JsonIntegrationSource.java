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

package io.syndesis.test.integration.source;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.util.IOStreams;
import io.syndesis.common.util.Json;

/**
 * @author Christoph Deppisch
 */
public class JsonIntegrationSource implements IntegrationSource {

    private final byte[] json;
    private final Charset defaultCharset = Charset.forName("utf-8");

    public JsonIntegrationSource(String json) {
        this.json = json.getBytes(defaultCharset);
    }

    public JsonIntegrationSource(String json, Charset charset) {
        this.json = json.getBytes(charset);
    }

    public JsonIntegrationSource(InputStream is) {
        try {
            this.json = IOStreams.readBytes(is);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to access integration file path", e);
        }
    }

    public JsonIntegrationSource(Path pathToJson) {
        try {
            this.json = Files.readAllBytes(pathToJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to access integration file path", e);
        }
    }

    @Override
    public Integration get() {
        try {
            return Json.reader().forType(Integration.class).readValue(json);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read integration json", e);
        }
    }
}
