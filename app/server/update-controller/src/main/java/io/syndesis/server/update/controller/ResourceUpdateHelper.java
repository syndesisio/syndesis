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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.connection.ConfigurationProperty;

public final class ResourceUpdateHelper {

    private ResourceUpdateHelper() {
    }

    public static List<LeveledMessage> computeBulletinMessages(Map<String, ConfigurationProperty> left, Map<String, ConfigurationProperty> right) {
        final List<LeveledMessage> messages = new ArrayList<>();
        final MapDifference<String, ConfigurationProperty> diff = Maps.difference(left, right);

        for (Map.Entry<String, ConfigurationProperty> entry: diff.entriesOnlyOnLeft().entrySet()) {
            LeveledMessage.Builder builder = new LeveledMessage.Builder();
            builder.level(LeveledMessage.Level.WARN);
            builder.putMetadata("property", entry.getKey());
            builder.putMetadata("status", "deleted");

            messages.add(builder.build());
        }

        for (Map.Entry<String, ConfigurationProperty> entry: diff.entriesOnlyOnRight().entrySet()) {
            LeveledMessage.Builder builder = new LeveledMessage.Builder();
            builder.level(LeveledMessage.Level.INFO);
            builder.putMetadata("property", entry.getKey());
            builder.putMetadata("status", "new");

            if (entry.getValue().isRequired()) {
                // If the new property is mandatory set level to WARN as
                // user needs to configure it
                builder.level(LeveledMessage.Level.WARN);
            }

            messages.add(builder.build());
        }

        for (Map.Entry<String, MapDifference.ValueDifference<ConfigurationProperty>> entry: diff.entriesDiffering().entrySet()) {
            final MapDifference.ValueDifference<ConfigurationProperty> value = entry.getValue();
            final ConfigurationProperty leftValue = value.leftValue();
            final ConfigurationProperty rightValue = value.rightValue();

            LeveledMessage.Builder builder = new LeveledMessage.Builder();
            builder.level(LeveledMessage.Level.INFO);
            builder.putMetadata("property", entry.getKey());
            builder.putMetadata("status", "updated");

            if (leftValue != null && !leftValue.isRequired() && rightValue != null &&rightValue.isRequired()) {
                // If the new property is mandatory but the old one is not,
                // set level to WARN as user may needs to configure it
                builder.level(LeveledMessage.Level.WARN);
            }

            messages.add(builder.build());
        }

        return messages;
    }
}
