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
package io.syndesis.server.endpoint.v1.handler.integration.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.syndesis.common.model.bulletin.ConnectionBulletinBoard;
import io.syndesis.common.model.bulletin.LeveledMessage;
import io.syndesis.common.model.bulletin.WithLeveledMessages;
import io.syndesis.common.model.connection.Connection;

public class ConnectionOverview {
    private final Connection value;
    private final Optional<ConnectionBulletinBoard> board;

    public ConnectionOverview(Connection value) {
        this(value, Optional.empty());
    }

    public ConnectionOverview(Connection value, Optional<ConnectionBulletinBoard> board) {
        this.value = value;
        this.board = board;
    }

    public String getName() {
        return value.getName();
    }

    public Optional<String> getId() {
        return value.getId();
    }

    public String getConnectorId() {
        return value.getConnectorId();
    }

    public String getIcon() {
        return value.getIcon();
    }

    public Connection getConnection() {
        return value;
    }

    public List<LeveledMessage> getMessages() {
        return board.map(WithLeveledMessages::getMessages).orElse(Collections.emptyList());
    }

    public int getWarnings() {
        int count = 0;

        if (board.isPresent()) {
            for (LeveledMessage message : board.get().getMessages()) {
                if (message.getLevel() == LeveledMessage.Level.WARN) {
                    count++;
                }
            }
        }

        return count;
    }

    public int getErrors() {
        int count = 0;

        if (board.isPresent()) {
            for (LeveledMessage message : board.get().getMessages()) {
                if (message.getLevel() == LeveledMessage.Level.ERROR) {
                    count++;
                }
            }
        }

        return count;
    }

    public int getNotices() {
        int count = 0;

        if (board.isPresent()) {
            for (LeveledMessage message : board.get().getMessages()) {
                if (message.getLevel() == LeveledMessage.Level.INFO) {
                    count++;
                }
            }
        }

        return count;
    }
}
