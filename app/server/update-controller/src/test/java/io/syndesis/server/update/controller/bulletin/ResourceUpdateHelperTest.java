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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.bulletin.IntegrationBulletinBoard;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.ConfigurationProperty;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceUpdateHelperTest {

    @Test
    public void testNoDiff() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().javaType("string").build());
        right.put("property", new ConfigurationProperty.Builder().javaType("string").build());

        List<LeveledMessage> messages = new DummyHandler().computePropertiesDiffMessages(LeveledMessage.Builder::new, left, right);

        assertThat(messages).hasSize(0);
    }

    @Test
    public void testUpdatedProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().javaType("string").build());
        right.put("property", new ConfigurationProperty.Builder().javaType("STRING").build());

        List<LeveledMessage> messages = new DummyHandler().computePropertiesDiffMessages(LeveledMessage.Builder::new, left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.INFO);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("code", LeveledMessage.Code.SYNDESIS001);
    }

    @Test
    public void testNewRequiresProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().javaType("string").build());
        right.put("property", new ConfigurationProperty.Builder().javaType("STRING").build());
        right.put("property2", new ConfigurationProperty.Builder().javaType("string").required(true).build());

        List<LeveledMessage> messages = new DummyHandler().computePropertiesDiffMessages(LeveledMessage.Builder::new, left, right);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.INFO);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("code", LeveledMessage.Code.SYNDESIS001);
        assertThat(messages.get(1)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.WARN);
        assertThat(messages.get(1)).hasFieldOrPropertyWithValue("code", LeveledMessage.Code.SYNDESIS002);
    }

    @Test
    public void testMissingMandatoryProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, String> right = new HashMap<>();

        left.put("property1", new ConfigurationProperty.Builder().javaType("string").build());
        left.put("property2", new ConfigurationProperty.Builder().javaType("string").required(true).build());
        right.put("property1", "value1");

        List<LeveledMessage> messages = new DummyHandler().computeMissingMandatoryPropertiesMessages(LeveledMessage.Builder::new, left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.WARN);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("code", LeveledMessage.Code.SYNDESIS006);
    }

    /**
     * Dummy class to access inner helpers methods.
     */
    private static class DummyHandler extends AbstractResourceUpdateHandler<IntegrationBulletinBoard> {
        protected DummyHandler() {
            super(null, null, null);
        }

        @Override
        protected List<IntegrationBulletinBoard> compute(ChangeEvent event) {
            return Collections.emptyList();
        }

        @Override
        public boolean canHandle(ChangeEvent event) {
            return false;
        }
    }
}
