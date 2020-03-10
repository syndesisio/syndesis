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
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExcludeFilterTest {

    final DefaultArtifact excluded = new DefaultArtifact("excluded.group", "excluded.artifact", "1.0", "compile", "jar", "", null);

    final ExcludeFilter filter = new ExcludeFilter("excluded.group", "excluded.artifact");

    final DefaultArtifact included1 = new DefaultArtifact("included.group", "excluded.artifact", "1.0", "compile", "jar", "", null);

    final DefaultArtifact included2 = new DefaultArtifact("excluded.group", "included.artifact", "1.0", "compile", "jar", "", null);

    @Test
    public void shouldExcludeByGroupAndArtifactId() throws ArtifactFilterException {
        assertThat(filter.isArtifactIncluded(excluded)).isFalse();
        assertThat(filter.isArtifactIncluded(included1)).isTrue();
        assertThat(filter.isArtifactIncluded(included2)).isTrue();
    }

    @Test
    public void shouldPerformFiltering() throws ArtifactFilterException {
        final Set<Artifact> given = new HashSet<>();
        given.add(included1);
        given.add(excluded);
        given.add(included2);

        assertThat(filter.filter(given)).containsOnly(included1, included2);
    }
}
