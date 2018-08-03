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
package io.syndesis.common.model.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.syndesis.common.model.Split;
import io.syndesis.common.model.WithConfiguredProperties;
import io.syndesis.common.model.WithSplit;
import io.syndesis.common.model.connection.ConfigurationProperty;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = ConnectorDescriptor.Builder.class)
@SuppressWarnings("immutables")
public interface ConnectorDescriptor extends ActionDescriptor, WithConfiguredProperties, WithSplit, Serializable {

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

    // This is set to optional for backward compatibility with camel style connectors
    Optional<String> getComponentScheme();

    Optional<String> getConnectorFactory();

    @Value.Default
    default List<String> getConnectorCustomizers() {
        return Collections.emptyList();
    }

    /**
     * A weaker form of equality to {@link #equals(Object)}.
     * Compares a defining subset of properties to {code}another{code}'s
     * and in turn tests those properties for equivalence.
     *<p>
     * An equals test of a null field and an empty {@link Optional}
     * will return false whilst they are equivalent so this method will return true.
     * <p>
     * Items not tested include:
     * <ul>
     * <li>Version id -
     *        this id can be updated yet the rest of the object is still unchanged;
     * <li>Updated Date -
     *        an object can be modified then reverted yet the updated value will be different.
     * </ul>
     * <p>
     * Note
     * Method can result in 2 instances being equivalent even though some
     * properties are different. Thus, this should only be used in appropriate
     * situations.
     *
     * @param another a {@link ConnectorDescriptor} to compare with
     * @return true if this is equivalent to {code}another{code}, false otherwise
     */
    default boolean equivalent(ConnectorDescriptor another) {
        if (this == another) {
            return true;
        }

        if (another == null) {
            return false;
        }

        //
        // The json parser appears to create a new empty split object rather than
        // an empty optional
        //
        Split split = getSplit().orElse(new Split.Builder().build());
        Split anotherSplit = another.getSplit().orElse(new Split.Builder().build());

        return split.equals(anotherSplit)
                        && Objects.equals(getConnectorId(), another.getConnectorId())
                        && Objects.equals(getCamelConnectorGAV(), another.getCamelConnectorGAV())
                        && Objects.equals(getCamelConnectorPrefix(), another.getCamelConnectorPrefix())
                        && Objects.equals(getComponentScheme(), another.getComponentScheme())
                        && Objects.equals(getConnectorFactory(), another.getConnectorFactory())
                        && getConnectorCustomizers().equals(another.getConnectorCustomizers())
                        && Objects.equals(getInputDataShape(), another.getInputDataShape())
                        && Objects.equals(getOutputDataShape(), another.getOutputDataShape())
                        && getPropertyDefinitionSteps().equals(another.getPropertyDefinitionSteps())
                        && getConfiguredProperties().equals(another.getConfiguredProperties());
    }
}
