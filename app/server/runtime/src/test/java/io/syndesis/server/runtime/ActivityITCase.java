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
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import io.syndesis.server.endpoint.v1.handler.activity.Feature;

@ActiveProfiles("activity-test")
public class ActivityITCase extends BaseITCase {

    @Override
    @Before
    public void clearDB() {
        super.clearDB();
    }


    @Test
    public void requestFeature() throws IOException {
        ResponseEntity<Feature> re = get("/api/v1/activity/feature", Feature.class);
        Feature response = re.getBody();
        assertThat(response.isEnabled()).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    public void requestIntegrationLogs() throws IOException {
        jsondb.update("/", resource("logs-controller-db.json"));

        ResponseEntity<List> re = get("/api/v1/activity/integrations/my-integration", List.class);
        List<Object> response = re.getBody();
        assertThat(response.size()).isEqualTo(4);

    }

}
