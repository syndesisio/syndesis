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
package io.syndesis.server.update.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.ConfigurationProperty;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceUpdateHelperTest {

    @Test
    public void testUpdatedProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().description("property").build());
        right.put("property", new ConfigurationProperty.Builder().description("PROPERTY").build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeBulletinMessages(left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.INFO);
        assertThat(messages.get(0).getMetadata()).containsEntry("property", "property");
        assertThat(messages.get(0).getMetadata()).containsEntry("status", "updated");
    }

    @Test
    public void testUpdatedToRequiredProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().description("property").build());
        right.put("property", new ConfigurationProperty.Builder().description("PROPERTY").required(true).build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeBulletinMessages(left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.WARN);
        assertThat(messages.get(0).getMetadata()).containsEntry("property", "property");
        assertThat(messages.get(0).getMetadata()).containsEntry("status", "updated");
    }

    @Test
    public void testNewProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        right.put("property", new ConfigurationProperty.Builder().description("PROPERTY").build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeBulletinMessages(left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.INFO);
        assertThat(messages.get(0).getMetadata()).containsEntry("property", "property");
        assertThat(messages.get(0).getMetadata()).containsEntry("status", "new");
    }

    @Test
    public void testNewRequiresProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        right.put("property", new ConfigurationProperty.Builder().description("PROPERTY").required(true).build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeBulletinMessages(left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.WARN);
        assertThat(messages.get(0).getMetadata()).containsEntry("property", "property");
        assertThat(messages.get(0).getMetadata()).containsEntry("status", "new");
    }

    @Test
    public void testDeletedProperty() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().description("property").build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeBulletinMessages(left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.WARN);
        assertThat(messages.get(0).getMetadata()).containsEntry("property", "property");
        assertThat(messages.get(0).getMetadata()).containsEntry("status", "deleted");
    }

    @Test
    public void testAll() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("p1", new ConfigurationProperty.Builder().description("p1").build());
        left.put("p2", new ConfigurationProperty.Builder().build());
        right.put("p1", new ConfigurationProperty.Builder().description("P1").build());
        right.put("p3", new ConfigurationProperty.Builder().build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeBulletinMessages(left, right);

        assertThat(messages).hasSize(3);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.WARN);
        assertThat(messages.get(0).getMetadata()).containsEntry("property", "p2");
        assertThat(messages.get(0).getMetadata()).containsEntry("status", "deleted");

        assertThat(messages.get(1)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.INFO);
        assertThat(messages.get(1).getMetadata()).containsEntry("property", "p3");
        assertThat(messages.get(1).getMetadata()).containsEntry("status", "new");

        assertThat(messages.get(2)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.INFO);
        assertThat(messages.get(2).getMetadata()).containsEntry("property", "p1");
        assertThat(messages.get(2).getMetadata()).containsEntry("status", "updated");
    }

    @Test
    public void testUpdatedPropertySimple() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().description("property").build());
        right.put("property", new ConfigurationProperty.Builder().description("PROPERTY").build());
        right.put("property2", new ConfigurationProperty.Builder().description("PROPERTY2").build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeSimpleBulletinMessages(left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.INFO);
    }

    @Test
    public void testNewRequiresPropertySimpl() {
        Map<String, ConfigurationProperty> left = new HashMap<>();
        Map<String, ConfigurationProperty> right = new HashMap<>();

        left.put("property", new ConfigurationProperty.Builder().description("property").build());
        right.put("property", new ConfigurationProperty.Builder().description("PROPERTY").build());
        right.put("property2", new ConfigurationProperty.Builder().description("PROPERTY2").required(true).build());

        List<LeveledMessage> messages = ResourceUpdateHelper.computeSimpleBulletinMessages(left, right);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0)).hasFieldOrPropertyWithValue("level", LeveledMessage.Level.WARN);
    }
}
