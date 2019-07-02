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
package io.syndesis.connector.twitter;

import java.util.Date;

public class DirectMessage {

    private long id;
    private String text;
    private long senderId;
    private long recipientId;
    private Date createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(long recipientId) {
        this.recipientId = recipientId;
    }

    public Date getCreatedAt() {
        // do not expose the internal representation
        return new Date(createdAt.getTime());
    }

    public void setCreatedAt(Date createdAt) {
        // do not expose the internal representation
        this.createdAt = new Date(createdAt.getTime());
    }

    @Override
    public String toString() {
        return "DirectMessage{" +
            "id=" + id +
            ", createdAt=" + createdAt +
            ", senderId=" + senderId +
            ", recipientId=" + recipientId +
            ", text='" + text + '\'' +
            '}';
    }
}
