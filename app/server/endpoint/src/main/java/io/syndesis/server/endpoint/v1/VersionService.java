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
package io.syndesis.server.endpoint.v1;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.jcabi.manifests.Manifests;

import org.springframework.stereotype.Service;

@Service
public final class VersionService {

    private final Map<String, String> detail;

    public VersionService() {
        final Map<String, String> detail = new HashMap<>();
        putManifestValueTo(detail, "version", "Implementation-Version", "Project-Version");
        putManifestValueTo(detail, "commit-id", "Git-Commit-Id", "X-BasePOM-Git-Commit-Id");
        putManifestValueTo(detail, "branch", "Git-Branch");
        putManifestValueTo(detail, "build-time", "Build-Time");
        putManifestValueTo(detail, "build-id", "X-BasePOM-Build-Id");
        putManifestValueTo(detail, "camelversion", "camelversion") ;
        putManifestValueTo(detail, "camelkruntimeversion", "camelkruntimeversion");

        this.detail = Collections.unmodifiableMap(detail);
    }

    public Map<String, String> getDetailed() {
        return detail;
    }

    public String getVersion() {
        return Optional.ofNullable(detail.get("version")).orElse("DEVELOPMENT");
    }

    public String getCamelVersion() {
        return Optional.ofNullable(detail.get("camelversion")).orElseThrow(() -> new IllegalStateException("camelversion must be found in a MANIFEST.MF!"));
    }

    public String getCamelkRuntimeVersion() {
        return Optional.ofNullable(detail.get("camelkruntimeversion")).orElseThrow(() -> new IllegalStateException("camelkruntimeversion must be found in a MANIFEST.MF!"));
    }

    static void putManifestValueTo(final Map<String, String> detail, final String key, final String... attributes) {
        for (final String attribute : attributes) {
            if (Manifests.exists(attribute)) {
                detail.put(key, Manifests.read(attribute));
                break;
            }
        }
    }

}
