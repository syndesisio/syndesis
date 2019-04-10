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
package io.syndesis.server.credential;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

abstract class BaseCredentialProvider implements CredentialProvider {

    protected static final MultiValueMap<String, String> EMPTY = new LinkedMultiValueMap<>();

    protected static String callbackUrlFor(final URI baseUrl, final MultiValueMap<String, String> additionalParams) {
        final String path = baseUrl.getPath();

        final String callbackPath = path + "credentials/callback";

        try {
            final URI base = new URI(baseUrl.getScheme(), null, baseUrl.getHost(), baseUrl.getPort(), callbackPath,
                null, null);

            return UriComponentsBuilder.fromUri(base).queryParams(additionalParams).build().toUriString();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Unable to generate callback URI", e);
        }
    }

    protected static String descriptionFor(final String providerId) {
        return providerId;
    }

    protected static String iconFor(final String providerId) {
        return providerId;
    }

    protected static String labelFor(final String providerId) {
        return providerId;
    }
}
