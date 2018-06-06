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
package io.syndesis.server.endpoint.v1.handler.setup;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.server.credential.Credentials;

final class OAuthConnectorFilter implements Function<ListResult<Connector>, ListResult<Connector>> {

    public static final Function<ListResult<Connector>, ListResult<Connector>> INSTANCE = new OAuthConnectorFilter();

    private OAuthConnectorFilter() {
    }

    @Override
    public ListResult<Connector> apply(final ListResult<Connector> result) {
        final List<Connector> oauthConnectors = result.getItems().stream()
            .filter(c -> c.propertyEntryTaggedWith(Credentials.CLIENT_ID_TAG).isPresent()).collect(Collectors.toList());

        return ListResult.of(oauthConnectors);
    }

}
