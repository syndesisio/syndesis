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
package io.syndesis.extension.maven.plugin;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.ArtifactsFilter;

public class ExcludeFilter implements ArtifactsFilter {

    private final String excludedArtifactId;

    private final String excludedGroupId;

    ExcludeFilter(final String excludedGroupId, final String excludedArtifactId) {
        this.excludedGroupId = excludedGroupId;
        this.excludedArtifactId = excludedArtifactId;
    }

    @Override
    public Set<Artifact> filter(final Set<Artifact> artifacts) throws ArtifactFilterException {
        final Set<Artifact> included = new HashSet<>();
        for (final Artifact given : artifacts) {
            if (isArtifactIncluded(given)) {
                included.add(given);
            }
        }

        return included;
    }

    @Override
    public boolean isArtifactIncluded(final Artifact artifact) {
        return !(artifact.getGroupId().equals(excludedGroupId) && artifact.getArtifactId().equals(excludedArtifactId));
    }

}
