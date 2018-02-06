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
package io.syndesis.integration.project.generator.mvn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

@SuppressWarnings("PMD")
public final class MavenGav implements Comparable<MavenGav> {
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?(:([^: ]+))?");
    private static final char SEPARATOR_COORDINATE = ':';
    private static final String EMPTY_STRING = "";
    private static final String DEFAULT_TYPE = "jar";

    private static final int IS_POS_1 = 1;
    private static final int ID_POS_2 = 2;
    private static final int ID_POS_3 = 4;
    private static final int ID_POS_4 = 6;
    private static final int ID_POS_5 = 8;

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String classifier;

    public MavenGav(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.type = DEFAULT_TYPE;
        this.version = version;
        this.classifier = EMPTY_STRING;
    }

    public MavenGav(String coordinates) {
        final Matcher m = DEPENDENCY_PATTERN.matcher(coordinates);
        if (!m.matches()) {
            throw new IllegalArgumentException("Bad artifact coordinates"
                + ", expected format is <groupId>:<artifactId>[:<packagingType>[:<classifier>]]:(<version>|'?'), got: "
                + coordinates);
        }

        String determinedType = DEFAULT_TYPE;
        String determinedClassifier = EMPTY_STRING;
        this.groupId = m.group(IS_POS_1);
        this.artifactId = m.group(ID_POS_2);

        final String position3 = m.group(ID_POS_3);
        final String position4 = m.group(ID_POS_4);
        final String position5 = m.group(ID_POS_5);

        // some logic with numbers of provided groups
        final int noOfColons = numberOfOccurrences(coordinates, SEPARATOR_COORDINATE);

        // Parsing is segment-dependent
        switch (noOfColons) {
        case 2:
            this.version = position3;
            break;
        case 3:
            determinedType = position3 == null || position3.length() == 0 ? DEFAULT_TYPE : position3;
            this.version = position4;
            break;
        default:
            determinedType = position3 == null || position3.length() == 0 ? DEFAULT_TYPE : position3;
            determinedClassifier = position4;
            this.version = position5;
            break;
        }

        this.classifier = determinedClassifier;
        this.type = determinedType;
    }

    public String getPackaging() {
        return type;
    }

    public String getClassifier() {
        return classifier;
    }

    public String getVersion() {
        return version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getId() {
        StringBuilder sb = new StringBuilder();
        sb.append(getGroupId())
            .append(':')
            .append(getArtifactId())
            .append(':')
            .append(getPackaging());

        if (!StringUtils.isEmpty(getClassifier())) {
            sb.append(':')
                .append(getClassifier());
        }

        sb.append(':')
            .append(getVersion());

        return sb.toString();
    }

    @Override
    public String toString() {
        return getId();
    }

    @SuppressWarnings("PMD.NPathComplexity")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MavenGav gav = (MavenGav) o;

        if (groupId != null ? !groupId.equals(gav.groupId) : gav.groupId != null) {
            return false;
        }
        if (artifactId != null ? !artifactId.equals(gav.artifactId) : gav.artifactId != null) {
            return false;
        }
        if (type != null ? !type.equals(gav.type) : gav.type != null) {
            return false;
        }
        if (classifier != null ? !classifier.equals(gav.classifier) : gav.classifier != null) {
            return false;
        }
        return version != null ? version.equals(gav.version) : gav.version == null;
    }

    @SuppressWarnings("PMD.NPathComplexity")
    @Override
    public int hashCode() {
        int result = groupId != null ? groupId.hashCode() : 0;
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(MavenGav o) {
        return new CompareToBuilder()
            .append(this.groupId, o.groupId)
            .append(this.artifactId, o.artifactId)
            .append(this.type, o.type)
            .append(this.classifier, o.classifier)
            .append(this.version, o.version)
            .toComparison();
    }


    // *****************************
    // Helpers
    // *****************************

    private static int numberOfOccurrences(final CharSequence haystack, char needle) {
        int counter = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                counter++;
            }
        }
        return counter;
    }
}
