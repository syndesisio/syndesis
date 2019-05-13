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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.Secret;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.server.controller.integration.camelk.crd.ConfigurationSpec;
import io.syndesis.server.controller.integration.camelk.crd.Integration;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationSpec;
import io.syndesis.server.controller.integration.camelk.crd.IntegrationTraitSpec;
import org.springframework.stereotype.Component;

import static io.syndesis.common.util.Optionals.asStream;

/**
 * Enables specific Knative traits if needed
 */
@Component
public class KnativeCamelKIntegrationCustomizer implements CamelKIntegrationCustomizer {

    private static final String KNATIVE_TRAIT = "knative";
    private static final String KNATIVE_SERVICE_TRAIT = "knative-service";
    private static final String DEPLOYER_TRAIT = "deployer";

    private static final String KNATIVE_SOURCE_CHANNEL_TAG = "knative-source-channel";
    private static final String KNATIVE_SINK_CHANNEL_TAG = "knative-sink-channel";
    private static final String KNATIVE_SINK_ENDPOINT_TAG = "knative-sink-endpoint";

    private static final String HTTP_PASSIVE_TAG = "http-passive";

    private static final boolean KNATIVE_SERVING_03_COMPAT = true;
    private static final String APPLICATION_PROPERTIES_FILE = "application.properties";

    @Override
    public Integration customize(IntegrationDeployment deployment, Integration integration, Secret secret) {
        customizeSourceSink(deployment, integration);
        customizeService(deployment, integration);
        customizeProperties(deployment, integration, secret);
        customizePaths(deployment, integration, secret);
        return integration;
    }

    protected Integration customizeSourceSink(IntegrationDeployment deployment, Integration integration) {
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
            spec.putTraits(KNATIVE_TRAIT, new IntegrationTraitSpec.Builder()
                .putConfiguration("enabled", "true")
                .putConfiguration("channel-sources", channelSources)
                .putConfiguration("channel-sinks", channelSinks)
                .putConfiguration("endpoint-sources", "default")
                .putConfiguration("endpoint-sinks", endpointSinks)
                .build()
            ).build()
        );

        return integration;
    }

    protected Integration customizeService(IntegrationDeployment deployment, Integration integration) {
        if (isKnativeServiceNeeded(deployment)) {
            IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
            if (integration.getSpec() != null) {
                spec = spec.from(integration.getSpec());
            }
            integration.setSpec(
                spec.putTraits(KNATIVE_SERVICE_TRAIT,
                    new IntegrationTraitSpec.Builder()
                        .putConfiguration("enabled", "true")
                        .putConfiguration("min-scale", "0")
                        .build()
                ).putTraits(DEPLOYER_TRAIT,
                    new IntegrationTraitSpec.Builder()
                        .putConfiguration("kind", KNATIVE_SERVICE_TRAIT)
                        //.putConfiguration("container-image", KNATIVE_SERVING_03_COMPAT ? "true" : "false") // slower, but needed for knative serving < 0.4
                        .build()
                ).build()
            );
        }

        return integration;
    }

    protected Integration customizeProperties(IntegrationDeployment deployment, Integration integration, Secret secret) {
        /*
        if (KNATIVE_SERVING_03_COMPAT && isKnativeServiceNeeded(deployment)
            && secret.getStringData().containsKey(APPLICATION_PROPERTIES_FILE)) {

            // Only do this if using Knative serving 0.3
            String data = secret.getStringData().get(APPLICATION_PROPERTIES_FILE);
            Properties properties = new Properties();
            try (StringReader reader = new StringReader(data)) {
                properties.load(reader);
            } catch (IOException e) {
                throw SyndesisServerException.launderThrowable("Error while reading properties from secret", e);
            }

            IntegrationSpec.Builder spec = new IntegrationSpec.Builder();
            if (integration.getSpec() != null) {
                spec = spec.from(integration.getSpec());
            }

            for (Map.Entry<Object, Object> kv : properties.entrySet()) {
                spec = spec.addConfiguration(new ConfigurationSpec.Builder()
                    .type("property")
                    .value(kv.getKey() + "=" + kv.getValue())
                    .build());
            }

            integration.setSpec(spec.build());
        }
        */
        return integration;
    }

    protected Integration customizePaths(IntegrationDeployment deployment, Integration integration, Secret secret) {
        if (KNATIVE_SERVING_03_COMPAT && isKnativeServiceNeeded(deployment)
            && integration.getSpec() != null && integration.getSpec().getConfiguration() != null) {

            // Only do this if using Knative serving 0.3
            // Replace absolute path with relative to /deployments
            boolean edited = false;
            String lookupKey = "AB_JMX_EXPORTER_CONFIG=";
            List<ConfigurationSpec> newConf = new ArrayList<>();
            for (ConfigurationSpec conf : integration.getSpec().getConfiguration()) {
                if ("env".equals(conf.getType()) && conf.getValue() != null && conf.getValue().startsWith(lookupKey)) {
                    newConf.add(new ConfigurationSpec.Builder()
                        .type("env")
                        // let's use the default prometheus config (needs to be injected from elsewhere)
                        .value("/opt/prometheus/prometheus-config.yml")
                        .build());
                    edited = true;
                } else {
                    newConf.add(conf);
                }
            }

            if (edited) {
                IntegrationSpec.Builder spec = new IntegrationSpec.Builder()
                    .from(integration.getSpec())
                    .configuration(newConf);
                integration.setSpec(spec.build());
            }
        }
        return integration;
    }

    protected boolean isKnativeServiceNeeded(IntegrationDeployment deployment) {
        return deployment.getSpec().getFlows().stream()
            .flatMap(f -> asStream(f.getSteps().stream().findFirst()))
            .flatMap(s -> asStream(s.getAction()))
            .anyMatch(a -> a.getTags().contains(HTTP_PASSIVE_TAG));
    }
}
