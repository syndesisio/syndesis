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
package io.syndesis.connector.twitter;

import java.util.Map;
import java.util.Optional;

import io.syndesis.connector.support.verifier.api.ComponentVerifier;

public class TwitterVerifier extends ComponentVerifier {
    @Override
    protected Optional<String> getConnectorAction() {
        return Optional.of("twitter-timeline");
    }

    @Override
    protected void customize(Map<String, Object> params) {
        params.put("timelineType", "MENTIONS");
    }
}
