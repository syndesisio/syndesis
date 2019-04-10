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

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import io.syndesis.common.model.integration.Integration;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonHandlingITCase extends BaseITCase {

    private final String id = UUID.randomUUID().toString();

    @After
    public void removeIntegration() {
        dataManager.getDataAccessObject(Integration.class).delete(id);
    }

    @Test
    public void valuesGivenInJsonShouldBeTrimmedToNull() {
        final SortedSet<String> tags = new TreeSet<>();
        tags.add("");
        tags.add(" tag");
        tags.add("\tTaggy McTagface\t");

        final Integration integration = new Integration.Builder().id(id).name("  some-name\t").description("")
            .tags(tags).build();
        post("/api/v1/integrations", integration, Integration.class);

        final ResponseEntity<Integration> result = get("/api/v1/integrations/" + id, Integration.class);
        final Integration created = result.getBody();
        assertThat(created.getName()).isEqualTo("some-name");
        assertThat(created.getDescription()).isNotPresent();
        assertThat(created.getTags()).containsExactly("Taggy McTagface", "tag");

    }
}
