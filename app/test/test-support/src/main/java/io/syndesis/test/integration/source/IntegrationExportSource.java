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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 */
public class IntegrationExportSource implements IntegrationSource {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationExportSource.class);

    private final JsonNode model;

    public IntegrationExportSource(InputStream export) {
        this.model = readModelFromZip(export);
    }

    public IntegrationExportSource(Path pathToExport) {
        try {
            LOG.info(String.format("Reading integration export source: '%s'", pathToExport.toAbsolutePath()));

            if (pathToExport.toFile().isDirectory()) {
                this.model = readModel(pathToExport);
            } else {
                this.model = readModelFromZip(Files.newInputStream(pathToExport));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to access integration export", e);
        }
    }

    @Override
    public Integration get() {
        try {
            JsonNode integrations = model.get("integrations");
            if (integrations != null && integrations.fields().hasNext()) {
                return Json.reader().forType(Integration.class).readValue(integrations.fields().next().getValue());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read integrations from export", e);
        }

        throw new IllegalStateException("Unable to import integration from export - no suitable integration source found");
    }

    @Override
    public Map<String, OpenApi> getOpenApis() {
        Map<String, OpenApi> openApis = new HashMap<>();

        try {
            JsonNode apis = model.get("open-apis");
            if (apis != null) {
                Iterator<Map.Entry<String, JsonNode>> fields = apis.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> fieldEntry = fields.next();
                    openApis.put(fieldEntry.getKey(), Json.reader().forType(OpenApi.class).readValue(fieldEntry.getValue()));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read open apis from export", e);
        }

        return openApis;
    }

    private JsonNode readModel(Path exportDirectory) throws IOException {
        return Json.reader().readTree(Files.newInputStream(exportDirectory.resolve("model.json")));
    }

    private JsonNode readModelFromZip(InputStream export) {
        try (ZipInputStream zis = new ZipInputStream(export)) {
            while (true) {
                ZipEntry entry = zis.getNextEntry();
                if( entry == null ) {
                    break;
                }

                if ("model.json".equals(entry.getName())) {
                    return Json.reader().readTree(zis);
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read integration export", e);
        }

        throw new IllegalStateException("Invalid export content - no integration model found");
    }
}
