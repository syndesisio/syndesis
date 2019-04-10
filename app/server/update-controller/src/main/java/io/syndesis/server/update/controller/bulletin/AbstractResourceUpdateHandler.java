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
package io.syndesis.server.update.controller.bulletin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.base.Strings;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.update.controller.ResourceUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
abstract class AbstractResourceUpdateHandler<T extends WithId<T>> implements ResourceUpdateHandler {
    private final Logger logger;
    private final AtomicBoolean running;
    private final DataManager dataManager;
    private final EncryptionComponent encryptionComponent;
    private final Validator validator;

    protected AbstractResourceUpdateHandler(DataManager dataManager, EncryptionComponent encryptionComponent, Validator validator) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.running = new AtomicBoolean(false);
        this.dataManager = dataManager;
        this.encryptionComponent = encryptionComponent;
        this.validator = validator;
    }
    @Override
    public void process(ChangeEvent event) {
        if (running.compareAndSet(false, true)) {
            try {
                compute(event).forEach(dataManager::set);
            } catch (@SuppressWarnings("PMD.AvoidCatchingThrowable") Throwable e) {
                logger.warn("Error handling update event {}", event, e);

                throw e;
            } finally {
                running.lazySet(false);
            }
        }
    }

    protected DataManager getDataManager() {
        return this.dataManager;
    }

    protected EncryptionComponent getEncryptionComponent() {
        return encryptionComponent;
    }

    protected Validator getValidator() {
        return validator;
    }

    /**
     * Compute the bulletin boards for the given change.
     *
     * @param event the event.
     * @return a list of boards or an empty collection.
     */
    protected abstract List<T> compute(ChangeEvent event);



    // *********************
    // Helpers
    // *********************

    protected int countMessagesWithLevel(LeveledMessage.Level level, List<LeveledMessage> messages) {
        int count = 0;

        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getLevel() == level) {
                count++;
            }
        }

        return count;
    }

    //
    // So we have dynamic actions that alter the ConfigurationProperty
    // as they add defaultValue and sometimes the enums, we now assume
    // that a difference in defaultValue is because of this dynamic
    // metadata stuffs so don't generate the message.
    //
    // TODO: dynamic metadata should not alter the action definition
    //
    protected boolean equals(ConfigurationProperty left, ConfigurationProperty right) {
        return Objects.equals(left.getDeprecated(), right.getDeprecated())
            && Objects.equals(left.getControlHint(), right.getControlHint())
            && Objects.equals(left.getLabelHint(), right.getLabelHint())
            && Objects.equals(left.getPlaceholder(), right.getPlaceholder())
            && Objects.equals(left.getGroup(), right.getGroup())
            && Objects.equals(left.getJavaType(), right.getJavaType())
            && Objects.equals(left.getKind(), right.getKind())
            && Objects.equals(left.getLabel(), right.getLabel())
            && Objects.equals(left.getRequired(), right.getRequired())
            && Objects.equals(left.getSecret(), right.getSecret())
            && Objects.equals(left.getType(), right.getType())
            && Objects.equals(left.getConnectorValue(), right.getConnectorValue());
    }

    // *********************
    // Simple Bulletin
    // *********************

    protected List<LeveledMessage> computePropertiesDiffMessages(
        Supplier<LeveledMessage.Builder> supplier, Map<String, ConfigurationProperty> left, Map<String, ConfigurationProperty> right) {

        final List<LeveledMessage> messages = new ArrayList<>();
        final MapDifference<String, ConfigurationProperty> diff = Maps.difference(left, right);

        for (Map.Entry<String, MapDifference.ValueDifference<ConfigurationProperty>> entry: diff.entriesDiffering().entrySet()) {
            final MapDifference.ValueDifference<ConfigurationProperty> value = entry.getValue();
            final ConfigurationProperty leftValue = value.leftValue();
            final ConfigurationProperty rightValue = value.rightValue();

            // Special handling because of dynamic metadata
            if (!equals(leftValue, rightValue)) {
                messages.add(
                    supplier.get()
                        .level(LeveledMessage.Level.INFO)
                        .code(LeveledMessage.Code.SYNDESIS001).build()
                );

                break;
            }
        }

        if (!diff.entriesOnlyOnLeft().isEmpty() || !diff.entriesOnlyOnRight().isEmpty()) {
            messages.add(
                supplier.get()
                    .level(LeveledMessage.Level.WARN)
                    .code(LeveledMessage.Code.SYNDESIS002).build()
            );
        }

        return messages;
    }

    protected List<LeveledMessage> computeMissingMandatoryPropertiesMessages(
        Supplier<LeveledMessage.Builder> supplier, Map<String, ConfigurationProperty> configurationProperties, Map<String, String> configuredProperties) {

        for (Map.Entry<String, ConfigurationProperty> entry: configurationProperties.entrySet()) {
            if (entry.getValue().required() && Strings.isNullOrEmpty(entry.getValue().getDefaultValue()) && !configuredProperties.containsKey(entry.getKey())) {
                return Collections.singletonList(
                    supplier.get()
                        .level(LeveledMessage.Level.WARN)
                        .code(LeveledMessage.Code.SYNDESIS006)
                        .build()
                );
            }

        }

        return Collections.emptyList();
    }



    protected List<LeveledMessage> computeSecretsUpdateMessages(
        Supplier<LeveledMessage.Builder> supplier, Map<String, ConfigurationProperty> configurationProperties, Map<String, String> configuredProperties) {

        for (Map.Entry<String, ConfigurationProperty> entry: configurationProperties.entrySet()) {
            String val = configuredProperties.get(entry.getKey());

            // We have a null value if it was an encrypted property that was
            // imported into a different system.
            if (entry.getValue().secret() && val != null && encryptionComponent.decrypt(val) == null) {
                return Collections.singletonList(
                    supplier.get()
                        .level(LeveledMessage.Level.WARN)
                        .code(LeveledMessage.Code.SYNDESIS007)
                        .build()
                );
            }

        }

        return Collections.emptyList();
    }

    protected <V> List<LeveledMessage> computeValidatorMessages(Supplier<LeveledMessage.Builder> supplier, V target) {
        final List<LeveledMessage> messages = new ArrayList<>();
        final Set<ConstraintViolation<V>> constraintViolations = validator.validate(target, AllValidations.class);

        for (ConstraintViolation<V> violation : constraintViolations) {
            messages.add(
                supplier.get()
                    .code(LeveledMessage.Code.SYNDESIS008)
                    .level(LeveledMessage.Level.ERROR)
                    .message(violation.getMessage())
                    .build()
            );
        }

        return messages;
    }
}
