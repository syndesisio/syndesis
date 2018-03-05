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
package io.syndesis.server.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class AtlasmapITCase extends BaseITCase {

    @Test
    public void getFieldActions() {
        ResponseEntity<byte[]> responseEntity = get("/api/v1/atlas/fieldActions", byte[].class);
        // System.out.println(new String(responseEntity.getBody(), StandardCharsets.UTF_8));
        assertThat(responseEntity).isNotNull();
    }

    @Test
    public void testJsonInspect() throws IOException {
        String resource = resource("atlasmap-json-inspection.json");
        ResponseEntity<byte[]> responseEntity = post("/api/v1/atlas/json/inspect", resource.getBytes(StandardCharsets.UTF_8), byte[].class);
        // System.out.println(new String(responseEntity.getBody(), StandardCharsets.UTF_8));
        assertThat(responseEntity).isNotNull();
    }

}
