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

import java.util.ArrayList;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

public class TagsITCase extends BaseITCase {

    @Test
    public void getAllTags() {

        // Check the we can list the integrations.
        ResponseEntity<TagListResult> list = get("/api/v1/tags", TagListResult.class);

        assertThat(list.getBody().getTotalCount()).as("total count").isGreaterThan(0);
        assertThat(list.getBody().getItems()).as("items").size().isGreaterThan(0);
        assertThat(list.getBody().getItems()).contains("sampledb");

    }

    public static class TagListResult {
        public int totalCount;
        public ArrayList<String> items;

        public int getTotalCount() {
            return totalCount;
        }

        public ArrayList<String> getItems() {
            return items;
        }
    }

}
