/*
 * Copyright (C) 2018 Red Hat, Inc.
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
package io.syndesis.runtime;

import io.syndesis.model.ListResult;
import io.syndesis.model.integration.Integration;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Used to test GraphQL based API access.
 */
public class GraphQLTest extends BaseITCase {

    public static class GraphQLRequest {
        public String query;
        public String variables;
        public String operationName;

        public GraphQLRequest(String query) {
            this.query = query;
        }
    }

    public static class IntegrationsResult {
        public ListResult<Integration> integrations;
    }

    public static class GraphQLResponse {
        public IntegrationsResult data;
    }

    @Test
    public void createAndGetIntegration() throws IOException {

        resetDB();
        GraphQLRequest request = new GraphQLRequest(
            "{" +
                "  integrations {" +
                "    totalCount" +
                "    items {" +
                "      id" +
                "      name" +
                "      steps {" +
                "        name" +
                "        configuredProperties {" +
                "          key" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");

        // System.out.println(utf8(post("/graphql", request, byte[].class).getBody()));

        ResponseEntity<GraphQLResponse> response = post("/graphql", request, GraphQLResponse.class);
        IntegrationsResult data = response.getBody().data;
        assertThat(data.integrations.getTotalCount()).isEqualTo(6);

    }
}
