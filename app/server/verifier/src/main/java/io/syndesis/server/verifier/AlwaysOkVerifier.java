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
package io.syndesis.server.verifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Verifier which returns always "OK" and can be used simulating a developer
 *
 * @author roland
 * @since 23/02/2017
 */

@Component
@ConditionalOnProperty(value = "meta.kind", havingValue = "always-ok")
public class AlwaysOkVerifier implements Verifier {

    // All good ....
    @Override
    public List<Result> verify(String connectorId, Map<String, String> options) {
        return Collections.singletonList(ImmutableResult.builder().status(Result.Status.OK).build());
    }
}
