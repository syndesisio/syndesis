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
package io.syndesis.common.model.integration;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import org.immutables.value.Value;

import io.syndesis.common.util.KeyGenerator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Value.Immutable
@JsonDeserialize(builder = ContinuousDeliveryEnvironment.Builder.class)
@SuppressWarnings("immutables")
public interface ContinuousDeliveryEnvironment extends Serializable {

    class Builder extends ImmutableContinuousDeliveryEnvironment.Builder {
        // allow access to ImmutableIntegration.Builder

        public static ContinuousDeliveryEnvironment createFrom(String environmentId, Date lastTaggedAt) {
            // generate new tag and timestamp
            // do we need to create Git style tag??
            return new ContinuousDeliveryEnvironment.Builder()
                    .environmentId(environmentId)
                    .releaseTag(KeyGenerator.createKey())
                    .lastTaggedAt(lastTaggedAt)
                    .build();
        }

        public static ContinuousDeliveryEnvironment createFrom(ContinuousDeliveryEnvironment environment, Date lastTaggedAt) {
            // generate new tag and timestamp
            // do we need to create Git style tag??
            return new ContinuousDeliveryEnvironment.Builder()
                    .createFrom(environment)
                    .releaseTag(KeyGenerator.createKey())
                    .lastTaggedAt(lastTaggedAt)
                    .build();
        }
    }

    /**
     * Associated {@link io.syndesis.common.model.environment.Environment}.
     * @return
     */
    String getEnvironmentId();

    /**
     * Tag ID updated by release tag service. Used to compare tagged 'version' in destination environment.
     * @return
     */
    String getReleaseTag();

    /**
     * Time when last tagged. Used to test whether integration should be updated.
     * @return
     */
    Date getLastTaggedAt();

    /**
     * Time when last exported. If taggedAt is newer, then integration should be exported.
     * @return
     */
    Optional<Date> getLastExportedAt();

    /**
     * Time when last imported.
     * @return
     */
    Optional<Date> getLastImportedAt();

    default ContinuousDeliveryEnvironment.Builder builder() {
        return new ContinuousDeliveryEnvironment.Builder().createFrom(this);
    }
}
