/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.openshift;


import org.immutables.value.Value;

import java.util.Optional;
import java.util.Properties;

import io.fabric8.kubernetes.client.RequestConfig;
import io.fabric8.kubernetes.client.RequestConfigBuilder;

@Value.Immutable
public interface OpenShiftDeployment {
    String getName();
    String getUsername();
    Integer getRevisionId();
    Optional<Integer> getReplicas();
    Optional<String> getToken();
    Optional<String> getGitRepository();
    Optional<String> getWebhookSecret();
    Optional<Properties> getApplicationProperties();

    default RequestConfig getRequestConfig() {
        return new RequestConfigBuilder().withOauthToken(getToken().orElse(null)).build();
    }

    static ImmutableOpenShiftDeployment.Builder builder() {
        return ImmutableOpenShiftDeployment.builder();
    }
}
