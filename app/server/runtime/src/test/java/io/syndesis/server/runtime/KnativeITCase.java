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

import io.syndesis.common.model.connection.ConnectionOverview;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test", "test-knative"})
public class KnativeITCase extends BaseITCase {

    @Test
    public void checkKnativeConnectionPresent() {
        final ResponseEntity<ConnectionOverview> response = get("/api/v1/connections/knative", ConnectionOverview.class);
        assertThat(response.getStatusCode()).as("knative connection status code").isEqualTo(HttpStatus.OK);
        final ConnectionOverview connection = response.getBody();
        assertThat(connection).isNotNull();
        assertThat(connection.getConnectorId()).contains("knative");
    }


}
