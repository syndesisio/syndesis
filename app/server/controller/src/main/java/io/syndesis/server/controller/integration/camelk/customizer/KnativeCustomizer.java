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
package io.syndesis.server.controller.integration.camelk.customizer;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.TraitSpec;
import io.syndesis.server.openshift.Exposure;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import static io.syndesis.common.util.Optionals.asStream;

/**
 * Enables specific Knative traits if needed
 */
@Component
@ConditionalOnProperty(value = "features.knative.enabled", havingValue = "true")
public class KnativeCustomizer implements CamelKIntegrationCustomizer {

    private static final String KNATIVE_TRAIT = "knative";
    private static final String KNATIVE_SERVICE_TRAIT = "knative-service";
    private static final String DEPLOYER_TRAIT = "deployer";

    private static final String KNATIVE_SOURCE_CHANNEL_TAG = "knative-source-channel";
    private static final String KNATIVE_SINK_CHANNEL_TAG = "knative-sink-channel";
    private static final String KNATIVE_SINK_ENDPOINT_TAG = "knative-sink-endpoint";

    private static final String HTTP_PASSIVE_TAG = "http-passive";

    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, EnumSet<Exposure> exposures) {
        customizeSourceSink(deployment, integration);
        customizeService(deployment, integration);
        return integration;
    }

    protected void customizeSourceSink(IntegrationDeployment deployment, Integration integration) {
        List<String> sourceChannels = deployment.getSpec().getFlows().stream()
            .flatMap(f -> f.getSteps().stream())
            .flatMap(s -> asStream(s.getAction().flatMap(a -> a.propertyTaggedWith(s.getConfiguredProperties(), KNATIVE_SOURCE_CHANNEL_TAG))))
            .collect(Collectors.toList());

        List<String> sinkChannels = deployment.getSpec().getFlows().stream()
            .flatMap(f -> f.getSteps().stream())
            .flatMap(s -> asStream(s.getAction().flatMap(a -> a.propertyTaggedWith(s.getConfiguredProperties(), KNATIVE_SINK_CHANNEL_TAG))))
            .collect(Collectors.toList());

        List<String> sinkEndpoints = deployment.getSpec().getFlows().stream()
            .flatMap(f -> f.getSteps().stream())
            .flatMap(s -> asStream(s.getAction().flatMap(a -> a.propertyTaggedWith(s.getConfiguredProperties(), KNATIVE_SINK_ENDPOINT_TAG))))
            .collect(Collectors.toList());

        String channelSources = sourceChannels.stream().collect(Collectors.joining(","));
        String channelSinks = sinkChannels.stream().collect(Collectors.joining(","));
        String endpointSinks = sinkEndpoints.stream().collect(Collectors.joining(","));

        IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
        if (integration.getSpec() != null) {
            spec = spec.from(integration.getSpec());
        }
        integration.setSpec(
            spec.putTraits(KNATIVE_TRAIT, new TraitSpec.Builder()
                .putConfiguration("enabled", "true")
                .putConfiguration("channel-sources", channelSources)
                .putConfiguration("channel-sinks", channelSinks)
                .putConfiguration("endpoint-sources", "default")
                .putConfiguration("endpoint-sinks", endpointSinks)
                .build()
            ).build()
        );
    }

    protected void customizeService(IntegrationDeployment deployment, Integration integration) {
        if (isKnativeServiceNeeded(deployment)) {
            IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
            if (integration.getSpec() != null) {
                spec = spec.from(integration.getSpec());
            }
            integration.setSpec(
                spec.putTraits(KNATIVE_SERVICE_TRAIT,
                    new TraitSpec.Builder()
                        .putConfiguration("enabled", "true")
                        .putConfiguration("min-scale", "0")
                        .build()
                ).putTraits(DEPLOYER_TRAIT,
                    new TraitSpec.Builder()
                        .putConfiguration("kind", KNATIVE_SERVICE_TRAIT)
                        .build()
                ).build()
            );
        }
    }

    protected boolean isKnativeServiceNeeded(IntegrationDeployment deployment) {
        return deployment.getSpec().getFlows().stream()
            .flatMap(f -> asStream(f.getSteps().stream().findFirst()))
            .flatMap(s -> asStream(s.getAction()))
            .anyMatch(a -> a.getTags().contains(HTTP_PASSIVE_TAG));
    }
}
