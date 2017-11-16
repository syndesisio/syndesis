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
package io.syndesis.model.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.model.connection.ConfigurationProperty;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = ConnectorDescriptor.Builder.class)
public interface ConnectorDescriptor extends ActionDescriptor, Serializable {

    final class Builder extends ImmutableConnectorDescriptor.Builder {

        public ConnectorDescriptor.Builder withActionDefinitionStep(
            final String name,
            final String description,
            final Consumer<ActionDescriptorStep.Builder> stepConfigurator) {

            stepConfigurator.andThen(
                builder -> addPropertyDefinitionStep(builder.build())
            ).accept(
                new ActionDescriptorStep.Builder()
                    .name(name)
                    .description(description)
            );

            return this;
        }

        public Builder replaceConfigurationProperty(
            final String propertyName,
            final Consumer<ConfigurationProperty.Builder> configurationPropertyConfigurator) {

            final ConnectorDescriptor definition = build();
            final List<ActionDescriptorStep> steps = definition.getPropertyDefinitionSteps();

            int stepIdx;
            ActionDescriptorStep step = null;
            for (stepIdx = 0; stepIdx < steps.size(); stepIdx++) {
                final ActionDescriptorStep potentialStep = steps.get(stepIdx);

                final Map<String, ConfigurationProperty> properties = potentialStep.getProperties();
                if (properties.containsKey(propertyName)) {
                    step = potentialStep;
                    break;
                }
            }

            if (step == null) {
                // found no property to replace, lets just ignore it
                return this;
            }

            final ConfigurationProperty configurationProperty = step.getProperties().get(propertyName);
            final ConfigurationProperty.Builder configurationPropertyModifier = new ConfigurationProperty.Builder().createFrom(configurationProperty);

            configurationPropertyConfigurator.accept(configurationPropertyModifier);

            final ActionDescriptorStep.Builder stepModifier = new ActionDescriptorStep.Builder()
                .createFrom(step)
                .putProperty(propertyName, configurationPropertyModifier.build());

            final List<ActionDescriptorStep> modifiedSteps = new ArrayList<>(steps);
            modifiedSteps.set(stepIdx, stepModifier.build());

            return propertyDefinitionSteps(modifiedSteps);
        }
    }


    String getConnectorId();

    String getCamelConnectorGAV();

    String getCamelConnectorPrefix();
}
